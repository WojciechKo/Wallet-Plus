package info.korzeniowski.walletplus.test.robolectric.datamanager;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CannotDeleteCategoryWithChildrenException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryWithGivenNameAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.ParentIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

@RunWith(RobolectricTestRunner.class)
public class LocalCategoryDataManagerTest {

    //TODO: inject this!
    CategoryDataManager categoryDataManager;

    GreenCategoryDao greenCategoryDao;
    @Before
    public void setUp() {
        // ((TestWalletPlus) Robolectric.application).injectMocks(this);
        SQLiteOpenHelper dbHelper = new DaoMaster.DevOpenHelper(Robolectric.application, null, null);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        greenCategoryDao = daoSession.getGreenCategoryDao();
        categoryDataManager = new LocalCategoryDataManager(greenCategoryDao);
    }

    @Test
    public void databaseIsEmpty() {
        List<Category> categories = categoryDataManager.getMainCategories();

        assertThat(categories.size()).isEqualTo(0);
    }

    @Test
    public void setOfencodingCategoryType() {
        Set<EnumSet<Category.Type>> testCases = new HashSet<EnumSet<Category.Type>>();
        testCases.add(EnumSet.of(Category.Type.EXPENSE));
        testCases.add(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE));
        testCases.add(EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME));
        testCases.add(EnumSet.of(Category.Type.INCOME));
        int index = 1;
        for (EnumSet<Category.Type> testCase : testCases) {
            Category category = new Category().setName("Main " + index++).setTypes(testCase);
            Long categoryId = categoryDataManager.insert(category);
            category = categoryDataManager.getById(categoryId);

            assertThat(category.getTypes()).containsAll(testCase);

            for (Category.Type type : Category.Type.values()) {
                if (!testCase.contains(type)) {
                    assertThat(category.getTypes()).doesNotContain(type);
                }
            }
        }
    }
    /*************************************
     *           TEST INSERT             *
     *************************************/
    @Test
    public void insertMainIncomeType() {
        assertThat(categoryDataManager.getMainCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setName("Main 1").setType(Category.Type.INCOME));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(1);

        categoryDataManager.insert(new Category().setName("Main 2").setType(Category.Type.INCOME));

        assertThat(categoryDataManager.getMainCategories()).hasSize(2);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(2);
    }

    @Test
    public void insertMainExpenseType() {
        assertThat(categoryDataManager.getMainCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setName("Main 1").setType(Category.Type.EXPENSE));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setName("Main 2").setType(Category.Type.EXPENSE));

        assertThat(categoryDataManager.getMainCategories()).hasSize(2);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(2);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(0);
    }

    @Test
    public void insertMainBothType() {
        assertThat(categoryDataManager.getMainCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setName("Main 1").setTypes(EnumSet.allOf(Category.Type.class)));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(1);

        categoryDataManager.insert(new Category().setName("Main 2").setTypes(EnumSet.allOf(Category.Type.class)));

        assertThat(categoryDataManager.getMainCategories()).hasSize(2);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(2);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(2);
    }

    @Test
    public void insertSubs() {
        Long mainId = categoryDataManager.insert(new Category().setName("Main"));
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(0);
        assertThat(categoryDataManager.getMainExpenseTypeCategories().get(0).getChildren()).hasSize(0);

        categoryDataManager.insert(new Category().setParentId(mainId).setName("Sub 1 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories().get(0).getChildren()).hasSize(1);

        categoryDataManager.insert(new Category().setParentId(mainId).setName("Sub 2 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(2);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(2);
        assertThat(categoryDataManager.getMainExpenseTypeCategories().get(0).getChildren()).hasSize(2);
    }

    @Test
    public void insertSubsToIncomeCategory() {
        Long mainId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.INCOME));
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(0);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(0);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setParentId(mainId).setName("Sub 1 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);

        categoryDataManager.insert(new Category().setParentId(mainId).setName("Sub 2 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(2);
        assertThat(categoryDataManager.getMainIncomeTypeCategories().get(0).getChildren()).hasSize(2);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(0);
    }


    @Test
    public void insertWithDuplicatedIdShouldThrowException() {
        Long id = categoryDataManager.insert(new Category().setName("Main 1"));

        try {
            categoryDataManager.insert(new Category().setId(id).setName("Main 2"));
            failBecauseExceptionWasNotThrown(EntityAlreadyExistsException.class);
        } catch (EntityAlreadyExistsException e) {
            assertThat(categoryDataManager.count()).isEqualTo(1);
        }
    }

    @Test
    public void insertMainWithDuplicatedNameShouldThrowException() {
        categoryDataManager.insert(new Category().setName("Main"));

        try {
            categoryDataManager.insert(new Category().setName("Main"));
            failBecauseExceptionWasNotThrown(CategoryWithGivenNameAlreadyExistsException.class);
        } catch (CategoryWithGivenNameAlreadyExistsException e) {
            assertThat(categoryDataManager.count()).isEqualTo(1);
        }

    }

    @Test(expected = ParentIsNotMainCategoryException.class)
    public void insertSubOfSubShouldThrowException() {
        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.INCOME));
        Long subcategoryId = categoryDataManager.insert(new Category().setParentId(parentCategoryId).setName("Sub"));

        categoryDataManager.insert(new Category().setParentId(subcategoryId).setName("Sub of Sub"));
    }

    @Test
    public void insertSubWithSetTypeShouldInhertTypeFromParent() {
        Category.Type parentType = Category.Type.INCOME;
        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setType(parentType));
        Long subcategoryId = categoryDataManager.insert(new Category().setParentId(parentCategoryId).setName("Sub").setType(Category.Type.EXPENSE));

        assertThat(categoryDataManager.getById(subcategoryId).getTypes()).containsExactly(parentType);
    }

    /*************************************
     *            TEST READ              *
     *************************************/
    private String getMainName(int number, String type) {
        return "Main " + number + " " + type;
    }

    private String getSubName(int number, String mainName) {
        return "Sub " + number + " of " + mainName;
    }

    @Test
    public void readIncomeTypeOnly() {
        // Insert I only
        String main1IName = getMainName(1, "I");
        String main2IName = getMainName(2, "I");
        Long main1IId = categoryDataManager.insert(new Category().setName(main1IName).setType(Category.Type.INCOME));
        categoryDataManager.insert(new Category().setName(getSubName(1, main1IName)).setParentId(main1IId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main1IName)).setParentId(main1IId));
        Long main2IId = categoryDataManager.insert(new Category().setName(main2IName).setType(Category.Type.INCOME));
        categoryDataManager.insert(new Category().setName(getSubName(1, main2IName)).setParentId(main2IId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main2IName)).setParentId(main2IId));

        // Insert IO
        String main1IOName = getMainName(1, "IO");
        String main2IOName = getMainName(2, "IO");
        Long main1IOId = categoryDataManager.insert(new Category().setName(main1IOName).setTypes(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE)));
        categoryDataManager.insert(new Category().setName(getSubName(1, main1IOName)).setParentId(main1IOId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main1IOName)).setParentId(main1IOId));
        categoryDataManager.insert(new Category().setName(getSubName(3, main1IOName)).setParentId(main1IOId));
        Long main2IOId = categoryDataManager.insert(new Category().setName(main2IOName).setTypes(EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME)));
        categoryDataManager.insert(new Category().setName(getSubName(1, main2IOName)).setParentId(main2IOId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main2IOName)).setParentId(main2IOId));
        categoryDataManager.insert(new Category().setName(getSubName(3, main2IOName)).setParentId(main2IOId));

        // Insert O only
        String main1OName = getMainName(1, "O");
        String main2OName = getMainName(2, "O");
        Long main1OId = categoryDataManager.insert(new Category().setName(main1OName).setTypes(EnumSet.of(Category.Type.EXPENSE)));
        categoryDataManager.insert(new Category().setName(getSubName(1, main1OName)).setParentId(main1OId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main1OName)).setParentId(main1OId));
        categoryDataManager.insert(new Category().setName(getSubName(3, main1OName)).setParentId(main1OId));
        categoryDataManager.insert(new Category().setName(getSubName(4, main1OName)).setParentId(main1OId));
        Long main2OId = categoryDataManager.insert(new Category().setName(main2OName).setTypes(EnumSet.of(Category.Type.EXPENSE)));
        categoryDataManager.insert(new Category().setName(getSubName(1, main2OName)).setParentId(main2OId));
        categoryDataManager.insert(new Category().setName(getSubName(2, main2OName)).setParentId(main2OId));
        categoryDataManager.insert(new Category().setName(getSubName(3, main2OName)).setParentId(main2OId));
        categoryDataManager.insert(new Category().setName(getSubName(4, main2OName)).setParentId(main2OId));

        List<Category> incomeCategories = categoryDataManager.getMainIncomeTypeCategories();

        assertThat(incomeCategories).hasSize(4);
        testIfContainsCategoryWithName(incomeCategories, main1IName);
        testIfContainsCategoryWithName(incomeCategories, main2IName);
        testIfContainsCategoryWithName(incomeCategories, main1IOName);
        testIfContainsCategoryWithName(incomeCategories, main2IOName);

        assertThat(getCategoryFromList(incomeCategories,main1IName).getChildren()).hasSize(2);
        assertThat(getCategoryFromList(incomeCategories,main2IName).getChildren()).hasSize(2);
        assertThat(getCategoryFromList(incomeCategories,main1IOName).getChildren()).hasSize(3);

        List<Category> expenseCategories = categoryDataManager.getMainExpenseTypeCategories();

        assertThat(expenseCategories).hasSize(4);
        testIfContainsCategoryWithName(expenseCategories, main1OName);
        testIfContainsCategoryWithName(expenseCategories, main2OName);
        testIfContainsCategoryWithName(expenseCategories, main1IOName);
        testIfContainsCategoryWithName(expenseCategories, main2IOName);

        assertThat(getCategoryFromList(expenseCategories,main1OName).getChildren()).hasSize(4);
        assertThat(getCategoryFromList(expenseCategories,main1OName).getChildren()).hasSize(4);
        assertThat(getCategoryFromList(expenseCategories,main1IOName).getChildren()).hasSize(3);
        assertThat(getCategoryFromList(expenseCategories,main2IOName).getChildren()).hasSize(3);
    }

    private Category getCategoryFromList(List<Category> categoryList, String name) {
        for (Category category : categoryList) {
            if (category.getName().equals(name)) {
                return category;
            }
        }
        return null;
    }

    private void testIfContainsCategoryWithName(List<Category> categoryList, String name) {
        for (Category category : categoryList) {
            if (category.getName().equals(name)) {
                return;
            }
        }
        fail("Categories: " + categoryList.toString() + "should contain: " + name + ".");
    }

    @Test(expected = NoSuchElementException.class)
    public void readNonexistentCategoryShouldThrowException() {
        Long categoryId = (long) 5326432;

        categoryDataManager.getById(categoryId);
    }

    @Test
    public void readMain() {
        String categoryName = "Main";
        EnumSet<Category.Type> categoryType = EnumSet.of(Category.Type.INCOME);
        Category inserted = new Category().setName(categoryName).setTypes(categoryType);
        Long id = categoryDataManager.insert(inserted);

        Category readed = categoryDataManager.getById(id);

        assertThat(readed.getName()).isEqualTo(categoryName);
        assertThat(readed.getTypes()).containsAll(categoryType);
        assertThat(categoryType).containsAll(readed.getTypes());
    }

    @Test
    public void readSub() {
        List<Category> categories = categoryDataManager.getMainCategories();
        EnumSet<Category.Type> mainCategoryType = EnumSet.of(Category.Type.INCOME);
        Long parentCategoryId = categoryDataManager.insert(
                new Category().setName("Main").setTypes(mainCategoryType)
        );
        String subCategoryName = "Sub";
        EnumSet<Category.Type> subCategoryType = EnumSet.of(Category.Type.EXPENSE);
        Long subCategoryId = categoryDataManager.insert(
                new Category().setName(subCategoryName).setTypes(subCategoryType).setParentId(parentCategoryId)
        );

        Category readSubCategory = categoryDataManager.getMainCategories().get(0).getChildren().get(0);

        assertThat(readSubCategory).isEqualTo(categoryDataManager.getById(subCategoryId));
        assertThat(readSubCategory.getName()).isEqualTo(subCategoryName);
        assertThat(readSubCategory.getTypes()).isEqualTo(mainCategoryType);
    }


    /*************************************
     *            TEST EDIT              *
     *************************************/
    @Test
    public void editMainCategory() {
        String oldName = "Main 1";
        EnumSet<Category.Type> oldType = EnumSet.of(Category.Type.EXPENSE);
        Long id = categoryDataManager.insert(new Category().setName(oldName).setTypes(oldType));

        String newName = "Main 2";
        EnumSet<Category.Type> newType = EnumSet.of(Category.Type.INCOME);
        categoryDataManager.update(new Category().setId(id).setName(newName).setTypes(newType));

        Category readed = categoryDataManager.getById(id);

        assertThat(readed.getName()).isEqualTo(newName);
        assertThat(readed.getTypes()).isEqualTo(newType);
    }

    @Test
    public void editSubName() {
        EnumSet<Category.Type> categories = EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME);
        Long mainId = categoryDataManager.insert(new Category().setName("Main").setTypes(categories));
        Long subId = categoryDataManager.insert(new Category().setName("Sub").setParentId(mainId));

        String newSubName = "Sub" + " New";
        categoryDataManager.update(new Category().setId(subId).setName(newSubName).setParentId(mainId));

        Category readedGetBy = categoryDataManager.getById(subId);
        assertThat(readedGetBy.getName()).isEqualTo(newSubName);
        assertThat(readedGetBy.getTypes()).isEqualTo(categories);
        assertThat(categoryDataManager.count()).isEqualTo(2);
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);

        assertThat(categoryDataManager.getMainCategories().get(0).getChildren().get(0)).isEqualTo(readedGetBy);
    }

    /*************************************
     *           TEST DELETE             *
     *************************************/
    @Test
    public void deleteMainWithoutSubs() {
        List<Category> categories = categoryDataManager.getMainCategories();
        Long categoryId = categoryDataManager.insert(new Category().setName("Main"));

        categoryDataManager.deleteById(categoryId);

        assertThat(categories).hasSize(0);
    }

    @Test(expected = CannotDeleteCategoryWithChildrenException.class)
    public void deleteNonEmptyMainCategorShouldThrowException() {

        Long parentId = categoryDataManager.insert(new Category().setName("Main"));
        categoryDataManager.insert(new Category().setName("Sub").setParentId(parentId));

        categoryDataManager.deleteById(parentId);
    }
}
