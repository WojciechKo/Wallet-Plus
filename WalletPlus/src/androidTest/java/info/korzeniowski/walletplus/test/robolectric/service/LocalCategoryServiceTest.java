package info.korzeniowski.walletplus.test.robolectric.service;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.model.Category;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;

@Ignore
@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCategoryServiceTest {

    CategoryService categoryService;

    @Before
    public void setUp() {
        // ((TestWalletPlus) Robolectric.application).injectMocks(this);
        categoryService = null;
    }

    @Test
    public void databaseShouldBeEmpty() {
        List<Category> categories = categoryService.getMainCategories();
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
        categoryService.insert(category);
        category = categoryService.findById(category.getId());

        assertThat(category.getType()).isEqualTo(type);

        categoryService.deleteById(category.getId());
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

        main.setId(categoryService.insert(main));

        if (main.isIncomeType()) {
            numberOfIncomeMain++;
        }
        if (main.isExpenseType()) {
            numberOfExpenseMain++;
        }
        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.getMainCategories().get(0).getChildren()).hasSize(0);

        categoryService.insert(new Category().setParent(categoryService.findById(main.getId())).setName("Sub 1 of Main"));

        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.getMainCategories().get(0).getChildren()).hasSize(1);

        categoryService.insert(new Category().setParent(categoryService.findById(main.getId())).setName("Sub 2 of Main").setType(main.getType()));

        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.getMainCategories().get(0).getChildren()).hasSize(2);
    }

    @Test
    public void shouldInhertTypeFromParent() {
        Category.Type parentType = Category.Type.INCOME;

        Long parentCategoryId = categoryService.insert(new Category().setName("Main").setType(parentType));
        Long subcategoryId = categoryService.insert(new Category().setName("Sub").setParent(categoryService.findById(parentCategoryId)));

        assertThat(categoryService.findById(subcategoryId).getType()).isEqualTo(parentType);
    }

    /*************************************
     *            TEST READ              *
     *************************************/
    @Test
    public void readNonexistingCategoryShouldReturnNull() {
        Long categoryId = (long) 5326432;
        assertThat(categoryService.findById(categoryId)).isNull();
    }

    @Test
    public void shouldBeAbleToReadMainCategory() {
        String categoryName = "Main";
        Category.Type categoryType = Category.Type.INCOME;
        Category inserted = new Category().setName(categoryName).setType(categoryType);
        Long id = categoryService.insert(inserted);

        Category read = categoryService.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    @Test
    public void shouldBeAbleToReadSubCategory() {
        Category parentCategory = new Category().setName("Main").setType(Category.Type.INCOME);
        Long parentCategoryId = categoryService.insert(parentCategory);
        Category subCategory = new Category().setName("Sub").setParent(categoryService.findById(parentCategoryId));
        Long subCategoryId = categoryService.insert(subCategory);

        Category readSubCategory = categoryService.getMainCategories().get(0).getChildren().iterator().next();

        assertThat(readSubCategory).isEqualTo(subCategory);
        assertThat(readSubCategory).isEqualTo(categoryService.findById(subCategoryId));
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
        categoryService.insert(oldCategory);

        Category newCategory = new Category().setId(oldCategory.getId()).setName("Main 1 Fix").setType(Category.Type.INCOME);
        categoryService.update(newCategory);

        Category readed = categoryService.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(readed).isEqualTo(newCategory);
    }

    @Test
    public void editMainTypeShouldEditTypeOfSubs() {
        Category oldMain= new Category().setName("Main 1").setType(Category.Type.EXPENSE);
        categoryService.insert(oldMain);
        Category oldSub = new Category().setName("Sub Main 1").setParent(categoryService.findById(oldMain.getId()));
        categoryService.insert(oldSub);
        Category.Type newType = Category.Type.INCOME;

        categoryService.update(categoryService.findById(oldMain.getId()).setType(newType));

        assertThat(categoryService.findById(oldSub.getId()).getType()).isEqualTo(newType);
    }

    public Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryService.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryService.insert(new Category().setName(getSubName(i, category.getName())).setParent(categoryService.findById(category.getId())));
        }
        return category;
    }

    @Test
    public void editNameAndTypeInMainCategoryWithChildren() {
        Category main1 = insertMainAndSubs(new Category().setName("Main 1").setType(Category.Type.INCOME), 2);
        Category main2 = insertMainAndSubs(new Category().setName("Main 2").setType(Category.Type.INCOME), 3);

        Long oldCategoryCount = categoryService.count();
        Integer oldMainSize = categoryService.getMainCategories().size();
        Integer oldMainIncomeSize = categoryService.getMainIncomeTypeCategories().size();
        Integer oldMainExpenseSize = categoryService.getMainExpenseTypeCategories().size();

        Category read = categoryService.findById(main2.getId());
        categoryService.update(read.setName("Main 2 Fix").setType(Category.Type.EXPENSE));

        assertThat(categoryService.findById(main2.getId())).isEqualTo(read);
        assertThat(categoryService.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryService.getMainCategories()).hasSize(oldMainSize);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(oldMainIncomeSize - 1);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(oldMainExpenseSize + 1);
    }

    @Test
    public void editSubName() {
        Category.Type category = Category.Type.INCOME_EXPENSE;
        Long mainId = categoryService.insert(new Category().setName("Main").setType(category));
        Long subId = categoryService.insert(new Category().setName("Sub").setParent(categoryService.findById(mainId)));

        String newSubName = "Sub Fix";
        categoryService.update(categoryService.findById(subId).setName(newSubName));

        Category readedGetBy = categoryService.findById(subId);
        assertThat(readedGetBy.getName()).isEqualTo(newSubName);
        assertThat(readedGetBy.getType()).isEqualTo(category);
        assertThat(categoryService.count()).isEqualTo(2);
        assertThat(categoryService.getMainCategories()).hasSize(1);

        assertThat(categoryService.getMainCategories().get(0).getChildren().iterator().next()).isEqualTo(readedGetBy);
    }

    /*************************************
     *           TEST DELETE             *
     *************************************/
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryService.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));

        categoryService.deleteById(categoryId);

        assertThat(categoryService.getMainCategories()).hasSize(0);
        assertThat(categoryService.count()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteSubCategory() {
        Long mainCategoryId = categoryService.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));
        Long subCategoryId = categoryService.insert(new Category().setName("Sub 1").setParent(categoryService.findById(mainCategoryId)));
        categoryService.insert(new Category().setName("Sub 2").setParent(categoryService.findById(mainCategoryId)));

        categoryService.deleteById(subCategoryId);

        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.count()).isEqualTo(2);
    }

    @Test
    public void deleteMainCategoryWithSubs() {
        Long otherMainCategory = categoryService.insert(new Category().setName("Main 1").setType(Category.Type.EXPENSE));
        categoryService.insert(new Category().setName("Sub 1 of Main 1").setParent(categoryService.findById(otherMainCategory)));
        categoryService.insert(new Category().setName("Sub 2 of Main 1").setParent(categoryService.findById(otherMainCategory)));

        Integer oldMainCategorySize = categoryService.getMainCategories().size();
        Long oldCount = categoryService.count();

        Long mainCategoryToDelete = categoryService.insert(new Category().setName("Main 2").setType(Category.Type.EXPENSE));
        categoryService.insert(new Category().setName("Sub 1 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)));
        categoryService.insert(new Category().setName("Sub 2 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)));

        categoryService.deleteByIdWithSubcategories(mainCategoryToDelete);

        assertThat(categoryService.count()).isEqualTo(oldCount);
        assertThat(categoryService.getMainCategories().size()).isEqualTo(oldMainCategorySize);
    }
}
