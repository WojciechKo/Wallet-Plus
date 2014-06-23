package info.korzeniowski.walletplus.test.robolectric.datamanager;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.model.Category;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCategoryDataManagerTest {

    CategoryDataManager categoryDataManager;

    @Before
    public void setUp() {
        // ((TestWalletPlus) Robolectric.application).injectMocks(this);
        categoryDataManager = null;
    }

    @Test
    public void databaseShouldBeEmpty() {
        List<Category> categories = categoryDataManager.getMainCategories();
        assertThat(categories.size()).isEqualTo(0);
    }

    @Test
    public void testSetOfEncodeCategoryType() {
        testEncodeCategoryType(Category.Type.INCOME);
        testEncodeCategoryType(Category.Type.EXPENSE);
        testEncodeCategoryType(Category.Type.INCOME_EXPENSE);
    }

    private void testEncodeCategoryType(Category.Type type) {
        Category category = new Category().setName("Category Test Name").setType(type);
        categoryDataManager.insert(category);
        category = categoryDataManager.findById(category.getId());

        assertThat(category.getType()).isEqualTo(type);

        categoryDataManager.deleteById(category.getId());
    }

    /*************************************
     *           TEST INSERT             *
     *************************************/
    @Test
    public void shouldInsertMainAndTwoSubCategoryOfIncomeType() {
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.INCOME);
    }

    @Test
    public void shouldInsertMainAndTwoSubCategoryOfExpenseType() {
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.EXPENSE);
    }

    @Test
    public void shouldInsertMainAndTwoSubCategoryOfBothTypes() {
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.INCOME_EXPENSE);
    }

    private void shouldInsertMainTwoSubCategoriesOfType(Category.Type types) {
        Integer numberOfIncomeMain = 0;
        Integer numberOfExpenseMain = 0;
        Category main = new Category().setName("Main").setType(types);

        main.setId(categoryDataManager.insert(main));

        if (main.isIncomeType()) {
            numberOfIncomeMain++;
        }
        if (main.isExpenseType()) {
            numberOfExpenseMain++;
        }
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(0);

        categoryDataManager.insert(new Category().setParent(categoryDataManager.findById(main.getId())).setName("Sub 1 of Main"));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(1);

        categoryDataManager.insert(new Category().setParent(categoryDataManager.findById(main.getId())).setName("Sub 2 of Main").setType(main.getType()));

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryDataManager.getMainCategories().get(0).getChildren()).hasSize(2);
    }

    @Test
    public void shouldInhertTypeFromParent() {
        Category.Type parentType = Category.Type.INCOME;

        Long parentCategoryId = categoryDataManager.insert(new Category().setName("Main").setType(parentType));
        Long subcategoryId = categoryDataManager.insert(new Category().setName("Sub").setParent(categoryDataManager.findById(parentCategoryId)));

        assertThat(categoryDataManager.findById(subcategoryId).getType()).isEqualTo(parentType);
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
        Category.Type categoryType = Category.Type.INCOME;
        Category inserted = new Category().setName(categoryName).setType(categoryType);
        Long id = categoryDataManager.insert(inserted);

        Category read = categoryDataManager.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    @Test
    public void shouldBeAbleToReadSubCategory() {
        Category parentCategory = new Category().setName("Main").setType(Category.Type.INCOME);
        Long parentCategoryId = categoryDataManager.insert(parentCategory);
        Category subCategory = new Category().setName("Sub").setParent(categoryDataManager.findById(parentCategoryId));
        Long subCategoryId = categoryDataManager.insert(subCategory);

        Category readSubCategory = categoryDataManager.getMainCategories().get(0).getChildren().iterator().next();

        assertThat(readSubCategory).isEqualTo(subCategory);
        assertThat(readSubCategory).isEqualTo(categoryDataManager.findById(subCategoryId));
        assertThat(readSubCategory.getType()).isEqualTo(parentCategory.getType());
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
        Category oldCategory = new Category().setName("Main 1").setType(Category.Type.EXPENSE);
        categoryDataManager.insert(oldCategory);

        Category newCategory = new Category().setId(oldCategory.getId()).setName("Main 1 Fix").setType(Category.Type.INCOME);
        categoryDataManager.update(newCategory);

        Category readed = categoryDataManager.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(readed).isEqualTo(newCategory);
    }

    @Test
    public void editMainTypeShouldEditTypeOfSubs() {
        Category oldMain= new Category().setName("Main 1").setType(Category.Type.EXPENSE);
        categoryDataManager.insert(oldMain);
        Category oldSub = new Category().setName("Sub Main 1").setParent(categoryDataManager.findById(oldMain.getId()));
        categoryDataManager.insert(oldSub);
        Category.Type newType = Category.Type.INCOME;

        categoryDataManager.update(categoryDataManager.findById(oldMain.getId()).setType(newType));

        assertThat(categoryDataManager.findById(oldSub.getId()).getType()).isEqualTo(newType);
    }

    public Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryDataManager.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryDataManager.insert(new Category().setName(getSubName(i, category.getName())).setParent(categoryDataManager.findById(category.getId())));
        }
        return category;
    }

    @Test
    public void editNameAndTypeInMainCategoryWithChildren() {
        Category main1 = insertMainAndSubs(new Category().setName("Main 1").setType(Category.Type.INCOME), 2);
        Category main2 = insertMainAndSubs(new Category().setName("Main 2").setType(Category.Type.INCOME), 3);

        Long oldCategoryCount = categoryDataManager.count();
        Integer oldMainSize = categoryDataManager.getMainCategories().size();
        Integer oldMainIncomeSize = categoryDataManager.getMainIncomeTypeCategories().size();
        Integer oldMainExpenseSize = categoryDataManager.getMainExpenseTypeCategories().size();

        Category read = categoryDataManager.findById(main2.getId());
        categoryDataManager.update(read.setName("Main 2 Fix").setType(Category.Type.EXPENSE));

        assertThat(categoryDataManager.findById(main2.getId())).isEqualTo(read);
        assertThat(categoryDataManager.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryDataManager.getMainCategories()).hasSize(oldMainSize);
        assertThat(categoryDataManager.getMainIncomeTypeCategories()).hasSize(oldMainIncomeSize - 1);
        assertThat(categoryDataManager.getMainExpenseTypeCategories()).hasSize(oldMainExpenseSize + 1);
    }

    @Test
    public void editSubName() {
        Category.Type category = Category.Type.INCOME_EXPENSE;
        Long mainId = categoryDataManager.insert(new Category().setName("Main").setType(category));
        Long subId = categoryDataManager.insert(new Category().setName("Sub").setParent(categoryDataManager.findById(mainId)));

        String newSubName = "Sub Fix";
        categoryDataManager.update(categoryDataManager.findById(subId).setName(newSubName));

        Category readedGetBy = categoryDataManager.findById(subId);
        assertThat(readedGetBy.getName()).isEqualTo(newSubName);
        assertThat(readedGetBy.getType()).isEqualTo(category);
        assertThat(categoryDataManager.count()).isEqualTo(2);
        assertThat(categoryDataManager.getMainCategories()).hasSize(1);

        assertThat(categoryDataManager.getMainCategories().get(0).getChildren().iterator().next()).isEqualTo(readedGetBy);
    }

    /*************************************
     *           TEST DELETE             *
     *************************************/
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));

        categoryDataManager.deleteById(categoryId);

        assertThat(categoryDataManager.getMainCategories()).hasSize(0);
        assertThat(categoryDataManager.count()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteSubCategory() {
        Long mainCategoryId = categoryDataManager.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));
        Long subCategoryId = categoryDataManager.insert(new Category().setName("Sub 1").setParent(categoryDataManager.findById(mainCategoryId)));
        categoryDataManager.insert(new Category().setName("Sub 2").setParent(categoryDataManager.findById(mainCategoryId)));

        categoryDataManager.deleteById(subCategoryId);

        assertThat(categoryDataManager.getMainCategories()).hasSize(1);
        assertThat(categoryDataManager.count()).isEqualTo(2);
    }

    @Test
    public void deleteMainCategoryWithSubs() {
        Long otherMainCategory = categoryDataManager.insert(new Category().setName("Main 1").setType(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub 1 of Main 1").setParent(categoryDataManager.findById(otherMainCategory)));
        categoryDataManager.insert(new Category().setName("Sub 2 of Main 1").setParent(categoryDataManager.findById(otherMainCategory)));

        Integer oldMainCategorySize = categoryDataManager.getMainCategories().size();
        Long oldCount = categoryDataManager.count();

        Long mainCategoryToDelete = categoryDataManager.insert(new Category().setName("Main 2").setType(Category.Type.EXPENSE));
        categoryDataManager.insert(new Category().setName("Sub 1 of Main 2").setParent(categoryDataManager.findById(mainCategoryToDelete)));
        categoryDataManager.insert(new Category().setName("Sub 2 of Main 2").setParent(categoryDataManager.findById(mainCategoryToDelete)));

        categoryDataManager.deleteByIdWithSubcategories(mainCategoryToDelete);

        assertThat(categoryDataManager.count()).isEqualTo(oldCount);
        assertThat(categoryDataManager.getMainCategories().size()).isEqualTo(oldMainCategorySize);
    }
}
