package info.korzeniowski.walletplus.test.robolectric.datamanager;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.EnumSet;
import java.util.List;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.local.LocalCategoryDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.DaoMaster;
import info.korzeniowski.walletplus.model.greendao.DaoSession;
import info.korzeniowski.walletplus.model.greendao.GreenCategoryDao;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCategoryDataManagerTest {

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
    public void databaseShouldBeEmpty() {
        List<Category> categories = categoryDataManager.getMainCategories();
        assertThat(categories.size()).isEqualTo(0);
    }

    @Test
    public void testSetOfEncodeCategoryType() {
        testEncodeCategoryType(EnumSet.of(Category.Type.INCOME));
        testEncodeCategoryType(EnumSet.of(Category.Type.EXPENSE));
        testEncodeCategoryType(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE));
        testEncodeCategoryType(EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME));
    }

    private void testEncodeCategoryType(EnumSet<Category.Type> types) {
        Category category = new Category().setName("Category Test Name").setTypes(types);
        categoryDataManager.insert(category);
        category = categoryDataManager.findById(category.getId());

        assertThat(category.getTypes()).containsAll(types);
        assertThat(types).containsAll(category.getTypes());

        categoryDataManager.deleteById(category.getId());
    }

    /*************************************
     *           TEST INSERT             *
     *************************************/
    @Test
    public void shouldInsertMainAndTwoSubCategoryOfIncomeType() {
        shouldInsertMainTwoSubCategoriesOfType(EnumSet.of(Category.Type.INCOME));
    }

    @Test
    public void shouldInsertMainAndTwoSubCategoryOfExpenseType() {
        shouldInsertMainTwoSubCategoriesOfType(EnumSet.of(Category.Type.EXPENSE));
    }

    @Test
    public void shouldInsertMainAndTwoSubCategoryOfBothTypes() {
        shouldInsertMainTwoSubCategoriesOfType(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE));
    }

    private void shouldInsertMainTwoSubCategoriesOfType(EnumSet<Category.Type> types) {
        Integer numberOfIncomeMain = 0;
        Integer numberOfExpenseMain = 0;
        Category main = new Category().setName("Main").setTypes(types);

        main.setId(categoryDataManager.insert(main));

        if (types.contains(Category.Type.INCOME)) {
            numberOfIncomeMain++;
        }
        if (types.contains(Category.Type.EXPENSE)) {
            numberOfExpenseMain++;
        }
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(0);

        categoryDataManager.insert(new Category().setParentId(main.getId()).setName("Sub 1 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(1);

        categoryDataManager.insert(new Category().setParentId(main.getId()).setName("Sub 2 of Main").setTypes(main.getTypes()));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(2);
    }

    @Test
    public void insertSubWithSetTypeShouldInhertTypeFromParent() {
        Category.Type parentType = Category.Type.INCOME;

        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setTypes(parentType));
        Long subcategoryId = categoryDataManager.insert(new Category().setName("Sub").setParentId(parentCategoryId));

        assertThat(categoryDataManager.findById(subcategoryId).getTypes()).containsExactly(parentType);
    }

    /*************************************
     *            TEST READ              *
     *************************************/
    @Test
    public void readNonexistingCategoryShouldReturnNull() {
        Long categoryId = (long) 5326432;
        assertThat(categoryDataManager.findById(categoryId)).isNull();
    }

    @Test
    public void shouldBeAbleToReadMainCategory() {
        String categoryName = "Main";
        EnumSet<Category.Type> categoryType = EnumSet.of(Category.Type.INCOME);
        Category inserted = new Category().setName(categoryName).setTypes(categoryType);
        Long id = categoryDataManager.insert(inserted);

        Category readed = categoryDataManager.findById(id);

        assertThat(inserted).isEqualTo(readed);
    }

    @Test
    public void shouldBeAbleToReadSubCategory() {
        Category parentCategory = new Category().setName("Main").setTypes(Category.Type.INCOME);
        Long parentCategoryId = categoryDataManager.insert(parentCategory);
        Category subCategory = new Category().setName("Sub").setParentId(parentCategoryId);
        Long subCategoryId = categoryDataManager.insert(subCategory);

        Category readSubCategory = categoryDataManager.getMainCategories().get(0).getChildren().get(0);

        assertThat(readSubCategory).isEqualTo(subCategory);
        assertThat(readSubCategory).isEqualTo(categoryDataManager.findById(subCategoryId));
        assertThat(readSubCategory.getTypes()).isEqualTo(parentCategory.getTypes());
    }

    @Test
    public void uberTestReadCategoriesWithSubs() {

        Category mainI1 = insertMainAndSubs(new Category().setName("Main I 1").setTypes(Category.Type.INCOME), 3);
        Category mainI2 = insertMainAndSubs(new Category().setName("Main I 2").setTypes(Category.Type.INCOME), 3);

        Category mainE1 = insertMainAndSubs(new Category().setName("Main E 1").setTypes(Category.Type.EXPENSE), 4);
        Category mainE2 = insertMainAndSubs(new Category().setName("Main E 2").setTypes(Category.Type.EXPENSE), 4);

        Category mainIE1 = insertMainAndSubs(new Category().setName("Main IE 1").setTypes(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE)), 5);
        Category mainIE2 = insertMainAndSubs(new Category().setName("Main IE 2").setTypes(EnumSet.of(Category.Type.INCOME, Category.Type.EXPENSE)), 5);

        /** Test Income list **/
        List<Category> incomeCategories = categoryDataManager.getMainIncomeTypeCategories();

        assertThat(incomeCategories).hasSize(4);
        testIfContainsCategoryWithName(incomeCategories, mainI1.getName());
        testIfContainsCategoryWithName(incomeCategories, mainI2.getName());
        testIfContainsCategoryWithName(incomeCategories, mainIE1.getName());
        testIfContainsCategoryWithName(incomeCategories, mainIE2.getName());

        assertThat(Category.findByName(incomeCategories, mainI1.getName()).getChildren()).hasSize(3);
        assertThat(Category.findByName(incomeCategories, mainI2.getName()).getChildren()).hasSize(3);
        assertThat(Category.findByName(incomeCategories, mainIE1.getName()).getChildren()).hasSize(5);
        assertThat(Category.findByName(incomeCategories, mainIE2.getName()).getChildren()).hasSize(5);

        /** Test Expense list **/
        List<Category> expenseCategories = categoryDataManager.getMainExpenseTypeCategories();

        assertThat(expenseCategories).hasSize(4);
        testIfContainsCategoryWithName(expenseCategories, mainE1.getName());
        testIfContainsCategoryWithName(expenseCategories, mainE2.getName());
        testIfContainsCategoryWithName(expenseCategories, mainIE1.getName());
        testIfContainsCategoryWithName(expenseCategories, mainIE2.getName());

        assertThat(Category.findByName(expenseCategories, mainE1.getName()).getChildren()).hasSize(4);
        assertThat(Category.findByName(expenseCategories, mainE1.getName()).getChildren()).hasSize(4);
        assertThat(Category.findByName(expenseCategories, mainIE1.getName()).getChildren()).hasSize(5);
        assertThat(Category.findByName(expenseCategories, mainIE2.getName()).getChildren()).hasSize(5);

        /** Test Income/Expense list **/
        List<Category> mainCategories= categoryDataManager.getMainCategories();

        assertThat(mainCategories).hasSize(6);
        testIfContainsCategoryWithName(incomeCategories, mainI1.getName());
        testIfContainsCategoryWithName(incomeCategories, mainI2.getName());
        testIfContainsCategoryWithName(expenseCategories, mainE1.getName());
        testIfContainsCategoryWithName(expenseCategories, mainE2.getName());
        testIfContainsCategoryWithName(expenseCategories, mainIE1.getName());
        testIfContainsCategoryWithName(expenseCategories, mainIE2.getName());

        assertThat(Category.findByName(incomeCategories, mainI1.getName()).getChildren()).hasSize(3);
        assertThat(Category.findByName(incomeCategories, mainI2.getName()).getChildren()).hasSize(3);
        assertThat(Category.findByName(expenseCategories, mainE1.getName()).getChildren()).hasSize(4);
        assertThat(Category.findByName(expenseCategories, mainE1.getName()).getChildren()).hasSize(4);
        assertThat(Category.findByName(expenseCategories, mainIE1.getName()).getChildren()).hasSize(5);
        assertThat(Category.findByName(expenseCategories, mainIE2.getName()).getChildren()).hasSize(5);

    }

    private String getMainName(int number, String type) {
        return "Main " + number + " " + type;
    }

    private String getSubName(int number, String mainName) {
        return "Sub " + number + " of " + mainName;
    }

    private void testIfContainsCategoryWithName(List<Category> categoryList, final String name) {
        if (!isCategoryListContainsCategoryWithName(categoryList, name)) {
            fail("Categories: " + categoryList.toString() + "should contain: " + name + ".");
        }
    }

    private boolean isCategoryListContainsCategoryWithName(List<Category> categoryList, final String name) {
        return Iterables.any(categoryList, new Predicate<Category>() {
            @Override
            public boolean apply(Category category) {
                return category.getName().equals(name);
            }
        });
    }

    /*************************************
     *            TEST EDIT              *
     *************************************/
    @Test
    public void editNameAndTypeInMainCategoryWithoutChildren() {
        Category oldCategory = new Category().setName("Main 1").setTypes(Category.Type.EXPENSE);
        categoryDataManager.insert(oldCategory);

        Category newCategory = new Category().setId(oldCategory.getId()).setName("Main 1 Fix").setTypes(Category.Type.INCOME);
        categoryDataManager.update(newCategory);

        Category readed = categoryDataManager.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(readed).isEqualTo(newCategory);
    }

    @Test
    public void editMainTypeShouldEditTypeOfSubs() {
        Category oldMain= new Category().setName("Main 1").setTypes(Category.Type.EXPENSE);
        categoryDataManager.insert(oldMain);
        Category oldSub = new Category().setName("Sub Main 1").setParentId(oldMain.getId());
        categoryDataManager.insert(oldSub);
        Category.Type newType = Category.Type.INCOME;

        categoryDataManager.update(categoryDataManager.findById(oldMain.getId()).setTypes(newType));

        assertThat(categoryDataManager.findById(oldSub.getId()).getTypes()).isEqualTo(EnumSet.of(newType));
    }

    public Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryDataManager.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryDataManager.insert(new Category().setName(getSubName(i, category.getName())).setParentId(category.getId()));
        }
        return category;
    }

    @Test
    public void editNameAndTypeInMainCategoryWithChildren() {
        Category main1 = insertMainAndSubs(new Category().setName("Main 1").setTypes(Category.Type.INCOME), 2);
        Category main2 = insertMainAndSubs(new Category().setName("Main 2").setTypes(Category.Type.INCOME), 3);

        Long oldCategoryCount = categoryDataManager.count();
        Integer oldMainSize = categoryDataManager.getMainCategories().size();
        Integer oldMainIncomeSize = categoryDataManager.getMainIncomeTypeCategories().size();
        Integer oldMainExpenseSize = categoryDataManager.getMainExpenseTypeCategories().size();

        Category read = categoryDataManager.findById(main2.getId());
        categoryDataManager.update(read.setName("Main 2 Fix").setTypes(Category.Type.EXPENSE));

        assertThat(categoryDataManager.findById(main2.getId())).isEqualTo(read);
        assertThat(categoryDataManager.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryDataManager.getMainCategories()).hasSize(oldMainSize);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(oldMainIncomeSize - 1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(oldMainExpenseSize + 1);
    }

    @Test
    public void editSubName() {
        EnumSet<Category.Type> categories = EnumSet.of(Category.Type.EXPENSE, Category.Type.INCOME);
        Long mainId = categoryDataManager.insert(new Category().setName("Main").setTypes(categories));
        Long subId = categoryDataManager.insert(new Category().setName("Sub").setParentId(mainId));

        String newSubName = "Sub Fix";
        categoryDataManager.update(categoryDataManager.findById(subId).setName(newSubName));

        Category readedGetBy = categoryDataManager.findById(subId);
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
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryDataManager.insert(new Category().setName("Main").setTypes(Category.Type.EXPENSE));

        categoryDataManager.deleteById(categoryId);

        assertThat(categoryDataManager.getMainCategories()).hasSize(0);
        assertThat(categoryDataManager.count()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteSubCategory() {
        Long mainCategoryId = categoryDataManager.insert(new Category().setName("Main").setTypes(Category.Type.EXPENSE));
        Long subCategoryId = categoryDataManager.insert(new Category().setName("Sub 1").setParentId(mainCategoryId));
        categoryDataManager.insert(new Category().setName("Sub 2").setParentId(mainCategoryId));

        categoryDataManager.deleteById(subCategoryId);

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.count()).isEqualTo(2);
    }

    @Test
    public void deleteMainCategoryWithSubs() {
        Long otherMainCategory = categoryDataManager.insert(new Category().setName("Main 1").setTypes(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub 1 of Main 1").setParentId(otherMainCategory));
        categoryDataManager.insert(new Category().setName("Sub 2 of Main 1").setParentId(otherMainCategory));

        Integer oldMainCategorySize = categoryDataManager.getMainCategories().size();
        Long oldCount = categoryDataManager.count();

        Long mainCategoryToDelete = categoryDataManager.insert(new Category().setName("Main 2").setTypes(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub 1 of Main 2").setParentId(mainCategoryToDelete));
        categoryDataManager.insert(new Category().setName("Sub 2 of Main 2").setParentId(mainCategoryToDelete));

        categoryDataManager.deleteByIdWithSubcategories(mainCategoryToDelete);

        assertThat(categoryDataManager.count()).isEqualTo(oldCount);
        assertThat(categoryDataManager.getMainCategories().size()).isEqualTo(oldMainCategorySize);
    }
}
