package info.korzeniowski.walletplus.test.robolectric.datamanager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeEmptyException;
import info.korzeniowski.walletplus.datamanager.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.exception.SubCategoryCantHaveTypeDifferentThanParentException;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryValidatorTest {
    private CategoryDataManager categoryDataManager;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() {
        categoryDataManager = mock(LocalCategoryDataManager.class);
    }

    /*************************************
     *         INSERT VALIDATION         *
     *************************************/
    @Test
    public void insertCategoryWithNullNameShouldThrowException() {
        try {
            categoryDataManager.insert(getSimpleCategory().setName(null));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeEmptyException.class);
        } catch (EntityPropertyCannotBeEmptyException e) {
        }
    }

    @Test
    public void insertWithDuplicatedIdShouldThrowException() {
        Category toInsert = getSimpleCategory();
        when(categoryDataManager.findById(toInsert.getId())).thenReturn(toInsert);

        categoryDataManager.insert(getSimpleCategory().setId(toInsert.getId()));

        exception.expect(EntityAlreadyExistsException.class);
    }

    @Test
    public void insertCategoryWithDuplicatedNameShouldThrowException() {
        Category toInsert = getSimpleCategory();
        when(categoryDataManager.findByName(toInsert.getName())).thenReturn(toInsert);

        try {
            categoryDataManager.insert(toInsert);
            failBecauseExceptionWasNotThrown(CategoryNameMustBeUniqueException.class);
        } catch (CategoryNameMustBeUniqueException e) {
        }
    }

    @Test
    public void insertMainCategoryWithoutTypeShouldThrowException() {
        try {
            categoryDataManager.insert(new Category().setName("Main"));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeEmptyException.class);
        } catch (EntityPropertyCannotBeEmptyException e) {
            assertThat(e.getProperty()).isEqualTo("Type");
        }
    }

    @Test
    public void insertSubOfSubShouldThrowException() {
        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.INCOME));
        Long subcategoryId = categoryDataManager.insert(new Category().setParent(categoryDataManager.findById(parentCategoryId)).setName("Sub"));

        try {
            categoryDataManager.insert(new Category().setParent(categoryDataManager.findById(subcategoryId)).setName("Sub of Sub"));
            failBecauseExceptionWasNotThrown(ParentCategoryIsNotMainCategoryException.class);
        } catch (ParentCategoryIsNotMainCategoryException e) {
        }
    }

    @Test
    public void insertSubWithTypeDifferentThanParentShouldThrowException() {
        Long parentId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.INCOME));
        try {
            categoryDataManager.insert(new Category().setName("Sub").setType(Category.Type.INCOME_EXPENSE).setParent(categoryDataManager.findById(parentId)));
            failBecauseExceptionWasNotThrown(SubCategoryCantHaveTypeDifferentThanParentException.class);
        } catch (SubCategoryCantHaveTypeDifferentThanParentException e) {
        }
    }

    //DELETE
    @Test
    public void deleteMainCategoryWithSubShouldThrowException() {
        Long parentId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub").setParent(categoryDataManager.findById(parentId)));

        try {
            categoryDataManager.deleteById(parentId);
            failBecauseExceptionWasNotThrown(CategoryHaveSubsException.class);
        } catch (CategoryHaveSubsException e) {

        }
    }

    private Category getSimpleCategory() {
        return new Category("Simple category-" + System.currentTimeMillis(), Category.Type.INCOME);
    }

}
