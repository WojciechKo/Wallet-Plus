package info.korzeniowski.walletplus.test.robolectric.service.validator;

import com.google.common.collect.Lists;
import com.j256.ormlite.dao.Dao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.UUID;

import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.service.exception.SubCategoryCantHaveTypeException;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.validation.CategoryValidator;
import info.korzeniowski.walletplus.model.Category;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryValidatorTest {
    private CategoryService categoryService;
    private CategoryService validatorDM;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Dao<Category, Long> categoryDao = mock(Dao.class);
        validatorDM = mock(CategoryService.class);
        categoryService = new LocalCategoryService(categoryDao, new CategoryValidator(validatorDM));
    }

    /*************************************
     *         INSERT VALIDATION         *
     *************************************/

    @Test
    public void shouldInsertMainCategory() {
        categoryService.insert(getSimpleMainIncomeCategory());
    }

    @Test
    public void shouldInsertSubOfMainCategory() {
        Category main = getSimpleMainIncomeCategory();
        when(validatorDM.findById(main.getId())).thenReturn(main);

        categoryService.insert(getSimpleMainIncomeCategory().setParent(main).setType(null));
    }

    @Test
    public void shouldThrowExceptionWhenInsertCategoryWithoutName() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Name");
        categoryService.insert(getSimpleMainIncomeCategory().setName(null));
    }

    @Test
    public void shouldThrowExceptionWhenInsertMainCategoryWithoutType() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Type");
        categoryService.insert(getSimpleMainIncomeCategory().setType(null));
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedId() {
        Category toInsert = getSimpleMainIncomeCategory();
        when(validatorDM.findById(toInsert.getId())).thenReturn(toInsert);

        exception.expect(EntityAlreadyExistsException.class);
        categoryService.insert(getSimpleMainIncomeCategory().setId(toInsert.getId()));
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedName() {
        Category toInsert = getSimpleMainIncomeCategory();
        when(validatorDM.findByName(toInsert.getName())).thenReturn(toInsert);

        exception.expect(CategoryNameMustBeUniqueException.class);
        categoryService.insert(getSimpleMainIncomeCategory().setName(toInsert.getName()));
    }

    @Test
    public void shouldThrowExceptionWhenInsertSubOfSub() {
        Category subOfMain = getSimpleMainIncomeCategory().setParent(getSimpleMainIncomeCategory());
        Category subOfSub = getSimpleMainIncomeCategory().setParent(subOfMain);
        when(validatorDM.findById(subOfMain.getId())).thenReturn(subOfMain);

        exception.expect(ParentCategoryIsNotMainCategoryException.class);
        categoryService.insert(subOfSub);
    }

    @Test
    public void shouldThrowExceptionWhenInsertSubWithTypeDifferentThanParent() {
        Category parent = getSimpleMainIncomeCategory();
        when(validatorDM.findById(parent.getId())).thenReturn(parent);

        exception.expect(SubCategoryCantHaveTypeException.class);
        categoryService.insert(getSimpleMainIncomeCategory().setParent(parent));
    }

    /*************************************
     *         DELETE VALIDATION         *
     *************************************/
    @Test
    public void shouldDeleteMainCategory() {
        Category main = getSimpleMainIncomeCategory();
        when(validatorDM.findById(main.getId())).thenReturn(main);

        categoryService.deleteById(main.getId());
    }

    @Test
    public void shouldDeleteSubCategory() {
        Category main = getSimpleMainIncomeCategory();
        Category sub = getSimpleMainIncomeCategory().setParent(main);
        when(validatorDM.findById(sub.getId())).thenReturn(sub);

        categoryService.deleteById(sub.getId());
    }

    @Test
    public void shouldThrowExceptionWhenDeleteMainCategoryWithSub() {
        Category main = getSimpleMainIncomeCategory();
        when(validatorDM.findById(main.getId())).thenReturn(main);
        when(validatorDM.getSubCategoriesOf(main.getId())).thenReturn(Lists.newArrayList(getSimpleMainIncomeCategory().setParent(main)));

        exception.expect(CategoryHaveSubsException.class);
        categoryService.deleteById(main.getId());
    }

    /*************************************
     *         UPDATE VALIDATION         *
     *************************************/
    @Test
    public void shouldUpdateNameAndTypeOfMainCategory() {
        Category category = getSimpleMainIncomeCategory();
        String newName = "NewName";
        when(validatorDM.findById(category.getId())).thenReturn(category);
        when(validatorDM.findByName(newName)).thenReturn(null);

        categoryService.update(category.setName(newName).setType(Category.Type.EXPENSE));
    }

    @Test
    public void shouldUpdateCategoryFromSubToMain() {
        Category main = getSimpleMainIncomeCategory();
        Category sub = getSimpleMainIncomeCategory().setType(null).setParent(main);
        when(validatorDM.findById(main.getId())).thenReturn(main);
        when(validatorDM.findById(sub.getId())).thenReturn(sub);

        sub.setType(sub.getParent().getType());
        sub.setParent(null);

        categoryService.update(sub);
    }

    @Test
    public void shouldUpdateCategoryFromMainToSub() {
        Category main1 = getSimpleMainIncomeCategory();
        Category main2 = getSimpleMainIncomeCategory();
        when(validatorDM.findById(main1.getId())).thenReturn(main1);
        when(validatorDM.findById(main2.getId())).thenReturn(main2);

        main1.setParent(main2);
        categoryService.update(main2);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateMainCategoryWithChildrenToSub() {
        Category mainWithSub = getSimpleMainIncomeCategory();
        Category subOfMain = getSimpleMainIncomeCategory().setParent(mainWithSub);
        Category main2 = getSimpleMainIncomeCategory();
        when(validatorDM.findById(mainWithSub.getId())).thenReturn(mainWithSub);
        when(validatorDM.findById(main2.getId())).thenReturn(main2);

        mainWithSub.setParent(main2);
        categoryService.update(main2);
    }

    private Category getSimpleMainIncomeCategory() {
        UUID id = UUID.randomUUID();
        return new Category("Simple category-" + id.getLeastSignificantBits()).setType(Category.Type.INCOME).setId(id.getLeastSignificantBits());
    }
}
