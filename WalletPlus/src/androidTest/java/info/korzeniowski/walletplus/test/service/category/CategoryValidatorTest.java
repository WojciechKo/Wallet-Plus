package info.korzeniowski.walletplus.test.service.category;

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

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.service.exception.SubCategoryCantHaveTypeException;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.validation.CategoryValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryValidatorTest {
    private CategoryService categoryService;
    private CategoryService validatorService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        Dao<Category, Long> categoryDao = mock(Dao.class);
        validatorService = mock(CategoryService.class);
        categoryService = new LocalCategoryService(categoryDao, new CategoryValidator(validatorService));
    }

    /**
     * **********************************
     * INSERT VALIDATION         *
     * ***********************************
     */

    @Test
    public void shouldInsertMainCategory() {
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().build());
    }

    @Test
    public void shouldInsertSubOfMainCategory() {
        Category main = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(main.getId())).thenReturn(main);

        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setParent(main).setType(null).build());
    }

    @Test
    public void shouldThrowExceptionWhenInsertCategoryWithoutName() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Name");
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setName(null).build());
    }

    @Test
    public void shouldThrowExceptionWhenInsertMainCategoryWithoutType() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Type");
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setType(null).build());
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedId() {
        Category toInsert = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(toInsert.getId())).thenReturn(toInsert);

        exception.expect(EntityAlreadyExistsException.class);
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setId(toInsert.getId()).build());
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedName() {
        Category toInsert = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findByName(toInsert.getName())).thenReturn(toInsert);

        exception.expect(CategoryNameMustBeUniqueException.class);
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setName(toInsert.getName()).build());
    }

    @Test
    public void shouldThrowExceptionWhenInsertSubOfSub() {
        Category subOfMain = getSimpleMainIncomeCategoryBuilder().setParent(getSimpleMainIncomeCategoryBuilder().build()).build();
        Category subOfSub = getSimpleMainIncomeCategoryBuilder().setParent(subOfMain).build();
        when(validatorService.findById(subOfMain.getId())).thenReturn(subOfMain);

        exception.expect(ParentCategoryIsNotMainCategoryException.class);
        categoryService.insert(subOfSub);
    }

    @Test
    public void shouldThrowExceptionWhenInsertSubWithTypeDifferentThanParent() {
        Category parent = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(parent.getId())).thenReturn(parent);

        exception.expect(SubCategoryCantHaveTypeException.class);
        categoryService.insert(getSimpleMainIncomeCategoryBuilder().setParent(parent).build());
    }

    /**
     * **********************************
     * DELETE VALIDATION         *
     * ***********************************
     */
    @Test
    public void shouldDeleteMainCategory() {
        Category main = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(main.getId())).thenReturn(main);

        categoryService.deleteById(main.getId());
    }

    @Test
    public void shouldDeleteSubCategory() {
        Category main = getSimpleMainIncomeCategoryBuilder().build();
        Category sub = getSimpleMainIncomeCategoryBuilder().setParent(main).build();
        when(validatorService.findById(sub.getId())).thenReturn(sub);

        categoryService.deleteById(sub.getId());
    }

    @Test
    public void shouldThrowExceptionWhenDeleteMainCategoryWithSub() {
        Category main = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(main.getId())).thenReturn(main);
        when(validatorService.getSubCategoriesOf(main.getId())).thenReturn(Lists.newArrayList(getSimpleMainIncomeCategoryBuilder().setParent(main).build()));

        exception.expect(CategoryHaveSubsException.class);
        categoryService.deleteById(main.getId());
    }

    /**
     * **********************************
     * UPDATE VALIDATION         *
     * ***********************************
     */
    @Test
    public void shouldUpdateNameAndTypeOfMainCategory() {
        Category category = getSimpleMainIncomeCategoryBuilder().build();
        String newName = "NewName";
        when(validatorService.findById(category.getId())).thenReturn(category);
        when(validatorService.findByName(newName)).thenReturn(null);

        categoryService.update(new Category.Builder(category).setName(newName).setType(Category.Type.EXPENSE).build());
    }

    @Test
    public void shouldUpdateCategoryFromSubToMain() {
        Category main = getSimpleMainIncomeCategoryBuilder().build();
        Category sub = getSimpleMainIncomeCategoryBuilder().setType(null).setParent(main).build();
        when(validatorService.findById(main.getId())).thenReturn(main);
        when(validatorService.findById(sub.getId())).thenReturn(sub);

        sub = new Category.Builder(sub).setType(sub.getParent().getType()).setParent(null).build();

        categoryService.update(sub);
    }

    @Test
    public void shouldUpdateCategoryFromMainToSub() {
        Category main1 = getSimpleMainIncomeCategoryBuilder().build();
        Category main2 = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(main1.getId())).thenReturn(main1);
        when(validatorService.findById(main2.getId())).thenReturn(main2);

        main1 = new Category.Builder(main1).setParent(main2).build();
        categoryService.update(main2);
    }

    @Test
    public void shouldThrowExceptionWhenUpdateMainCategoryWithChildrenToSub() {
        Category mainWithSub = getSimpleMainIncomeCategoryBuilder().build();
        Category subOfMain = getSimpleMainIncomeCategoryBuilder().setParent(mainWithSub).build();
        Category main2 = getSimpleMainIncomeCategoryBuilder().build();
        when(validatorService.findById(mainWithSub.getId())).thenReturn(mainWithSub);
        when(validatorService.findById(main2.getId())).thenReturn(main2);

        mainWithSub = new Category.Builder(mainWithSub).setParent(main2).build();
        categoryService.update(main2);
    }

    private Category.Builder getSimpleMainIncomeCategoryBuilder() {
        UUID id = UUID.randomUUID();
        return new Category.Builder()
                .setName("Simple category-" + id.getLeastSignificantBits())
                .setType(Category.Type.INCOME)
                .setId(id.getLeastSignificantBits());
    }
}
