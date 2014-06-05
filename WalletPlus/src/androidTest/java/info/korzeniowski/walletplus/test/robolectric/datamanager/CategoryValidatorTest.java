package info.korzeniowski.walletplus.test.robolectric.datamanager;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeEmptyException;
import info.korzeniowski.walletplus.datamanager.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.exception.SubCategoryCantHaveTypeDifferentThanParentException;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryValidatorTest {

    CategoryDataManager categoryDataManager;

    GreenCategoryDao greenCategoryDao;

    @Before
    public void setUp() {
        SQLiteOpenHelper dbHelper = new DaoMaster.DevOpenHelper(Robolectric.application, null, null);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        greenCategoryDao = daoSession.getGreenCategoryDao();
        categoryDataManager = new LocalCategoryDataManager(greenCategoryDao);
    }

    /*************************************
     *         INSERT VALIDATION         *
     *************************************/
    @Test
    public void insertCategoryWithNullNameShouldThrowException() {
        try {
            categoryDataManager.insert(new Category().setTypes(Category.Type.INCOME));
            failBecauseExceptionWasNotThrown(EntityPropertyCannotBeEmptyException.class);
        } catch (EntityPropertyCannotBeEmptyException e) {
        }
    }

    @Test
    public void insertWithDuplicatedIdShouldThrowException() {
        Long id = categoryDataManager.insert(new Category().setName("Main 1").setTypes(Category.Type.INCOME));

        try {
            categoryDataManager.insert(new Category().setId(id).setName("Main 2"));
            failBecauseExceptionWasNotThrown(EntityAlreadyExistsException.class);
        } catch (EntityAlreadyExistsException e) {
            assertThat(categoryDataManager.count()).isEqualTo(1);
        }
    }

    @Test
    public void insertCategoryWithDuplicatedNameShouldThrowException() {
        String main1Name = "Main 1";
        String main2Name = "Main 2";
        String sub1OfMain1 = "Sub 1 Of Main 1";

        Long incomeParentId = categoryDataManager.insert(new Category().setName(main1Name).setTypes(Category.Type.INCOME));
        Long expenseParentId = categoryDataManager.insert(new Category().setName(main2Name).setTypes(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName(sub1OfMain1).setParentId(incomeParentId));
        Long categoryCountBeforeInsert = categoryDataManager.count();

        try {
            categoryDataManager.insert(new Category().setName(main1Name).setTypes(Category.Type.INCOME));
            failBecauseExceptionWasNotThrown(CategoryNameMustBeUniqueException.class);
        } catch (CategoryNameMustBeUniqueException e) {
            assertThat(categoryDataManager.count()).isEqualTo(categoryCountBeforeInsert);
        }

        try {
            categoryDataManager.insert(new Category().setName(main1Name).setTypes(Category.Type.EXPENSE));
            failBecauseExceptionWasNotThrown(CategoryNameMustBeUniqueException.class);
        } catch (CategoryNameMustBeUniqueException e) {
            assertThat(categoryDataManager.count()).isEqualTo(categoryCountBeforeInsert);
        }

        try {
            categoryDataManager.insert(new Category().setName(sub1OfMain1).setParentId(incomeParentId));
            failBecauseExceptionWasNotThrown(CategoryNameMustBeUniqueException.class);
        } catch (CategoryNameMustBeUniqueException e) {
            assertThat(categoryDataManager.count()).isEqualTo(categoryCountBeforeInsert);
        }

        try {
            categoryDataManager.insert(new Category().setName(sub1OfMain1).setParentId(expenseParentId));
            failBecauseExceptionWasNotThrown(CategoryNameMustBeUniqueException.class);
        } catch (CategoryNameMustBeUniqueException e) {
            assertThat(categoryDataManager.count()).isEqualTo(categoryCountBeforeInsert);
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
        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setTypes(Category.Type.INCOME));
        Long subcategoryId = categoryDataManager.insert(new Category().setParentId(parentCategoryId).setName("Sub"));

        try {
            categoryDataManager.insert(new Category().setParentId(subcategoryId).setName("Sub of Sub"));
            failBecauseExceptionWasNotThrown(ParentCategoryIsNotMainCategoryException.class);
        } catch (ParentCategoryIsNotMainCategoryException e) {
        }
    }

    @Test
    public void insertSubWithTypeDifferentThanParentShouldThrowException() {
        Long parentId = categoryDataManager.insert(new Category().setName("Main").setTypes(Category.Type.INCOME));
        try {
            categoryDataManager.insert(new Category().setName("Sub").setTypes(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE)).setParentId(parentId));
            failBecauseExceptionWasNotThrown(SubCategoryCantHaveTypeDifferentThanParentException.class);
        } catch (SubCategoryCantHaveTypeDifferentThanParentException e) {
        }
    }

    //DELETE
    @Test
    public void deleteMainCategoryWithSubShouldThrowException() {
        Long parentId = categoryDataManager.insert(new Category().setName("Main").setTypes(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub").setParentId(parentId));

        try {
            categoryDataManager.deleteById(parentId);
            failBecauseExceptionWasNotThrown(CategoryHaveSubsException.class);
        } catch (CategoryHaveSubsException e) {

        }
    }
}
