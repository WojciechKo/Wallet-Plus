package info.korzeniowski.walletplus.test.service.category;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.sql.SQLException;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCategoryServiceTest {

    CategoryService categoryService;

    @Before
    public void setUp() {
        try {
            DatabaseHelper helper = new DatabaseHelper(Robolectric.application, null);
            categoryService = new LocalCategoryService(helper.getCategoryDao());
        } catch (SQLException e) {
            e.printStackTrace();
            fail("Setup failed.");
        }
    }

    @Test
    public void testSetOfEncodeCategoryType() {
        testEncodeCategoryType(Category.Type.INCOME);
        testEncodeCategoryType(Category.Type.EXPENSE);
        testEncodeCategoryType(Category.Type.INCOME_EXPENSE);
    }

    private void testEncodeCategoryType(Category.Type type) {
        Category category = new Category.Builder().setName("Category Test Name").setType(type).build();
        categoryService.insert(category);
        category = categoryService.findById(category.getId());

        assertThat(category.getType()).isEqualTo(type);

        categoryService.deleteById(category.getId());
    }

    /**
     * *************************
     * TEST INSERT             *
     * *************************
     */
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
        Category main = new Category.Builder().setName("Main").setType(types).build();
        categoryService.insert(main);

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

        categoryService.insert(new Category.Builder().setParent(categoryService.findById(main.getId())).setName("Sub of Main").build());

        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.getMainCategories().get(0).getChildren()).hasSize(1);
    }

    /**
     * ************************
     * TEST READ              *
     * ************************
     */
    @Test
    public void readNonexistingCategoryShouldReturnNull() {
        Long categoryId = (long) 5326432;
        assertThat(categoryService.findById(categoryId)).isNull();
    }

    @Test
    public void shouldBeAbleToReadMainCategory() {
        String categoryName = "Main";
        Category.Type categoryType = Category.Type.INCOME;
        Category inserted = new Category.Builder().setName(categoryName).setType(categoryType).build();
        Long id = categoryService.insert(inserted);

        Category read = categoryService.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    @Test
    public void shouldBeAbleToReadSubCategory() {
        Category parentCategory = new Category.Builder().setName("Main").setType(Category.Type.INCOME).build();
        Long parentCategoryId = categoryService.insert(parentCategory);
        Category subCategory = new Category.Builder().setName("Sub").setParent(categoryService.findById(parentCategoryId)).build();
        Long subCategoryId = categoryService.insert(subCategory);

        Category readSubCategory = categoryService.getMainCategories().get(0).getChildren().iterator().next();

        assertThat(readSubCategory).isEqualTo(subCategory);
        assertThat(readSubCategory).isEqualTo(categoryService.findById(subCategoryId));
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

    /**
     * ************************
     * TEST EDIT              *
     * ************************
     */
    @Test
    public void editNameAndTypeInMainCategoryWithoutChildren() {
        Category oldCategory = new Category.Builder().setName("Main 1").setType(Category.Type.EXPENSE).build();
        categoryService.insert(oldCategory);

        Category newCategory = new Category.Builder().setId(oldCategory.getId()).setName("Main 1 Fix").setType(Category.Type.INCOME).build();
        categoryService.update(newCategory);

        Category readed = categoryService.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(readed).isEqualTo(newCategory);
    }

    public Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryService.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryService.insert(new Category.Builder().setName(getSubName(i, category.getName())).setParent(categoryService.findById(category.getId())).build());
        }
        return category;
    }

    @Test
    public void editNameAndTypeInMainCategoryWithChildren() {
        Category main1 = insertMainAndSubs(new Category.Builder().setName("Main 1").setType(Category.Type.INCOME).build(), 2);
        Category main2 = insertMainAndSubs(new Category.Builder().setName("Main 2").setType(Category.Type.INCOME).build(), 3);

        Long oldCategoryCount = categoryService.count();
        Integer oldMainSize = categoryService.getMainCategories().size();
        Integer oldMainIncomeSize = categoryService.getMainIncomeTypeCategories().size();
        Integer oldMainExpenseSize = categoryService.getMainExpenseTypeCategories().size();

        Category read = categoryService.findById(main2.getId());
        categoryService.update(new Category.Builder(read).setName("Main 2 Fix").setType(Category.Type.EXPENSE).build());

        assertThat(categoryService.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryService.getMainCategories()).hasSize(oldMainSize);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(oldMainIncomeSize - 1);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(oldMainExpenseSize + 1);
    }

    @Test
    public void editSubName() {
        Category.Type category = Category.Type.INCOME_EXPENSE;
        Long mainId = categoryService.insert(new Category.Builder().setName("Main").setType(category).build());
        Long subId = categoryService.insert(new Category.Builder().setName("Sub").setParent(categoryService.findById(mainId)).build());

        String newSubName = "Sub Fix";
        categoryService.update(new Category.Builder(categoryService.findById(subId)).setName(newSubName).build());

        Category subFoundById = categoryService.findById(subId);
        assertThat(subFoundById.getName()).isEqualTo(newSubName);
        assertThat(categoryService.count()).isEqualTo(2);
        assertThat(categoryService.getMainCategories()).hasSize(1);

        assertThat(categoryService.getMainCategories().get(0).getChildren().iterator().next()).isEqualTo(subFoundById);
    }

    /**
     * **********************************
     * TEST DELETE             *
     * ***********************************
     */
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryService.insert(new Category.Builder().setName("Main").setType(Category.Type.EXPENSE).build());

        categoryService.deleteById(categoryId);

        assertThat(categoryService.getMainCategories()).hasSize(0);
        assertThat(categoryService.count()).isEqualTo(0);
    }

    @Test
    public void shouldDeleteSubCategory() {
        Long mainCategoryId = categoryService.insert(new Category.Builder().setName("Main").setType(Category.Type.EXPENSE).build());
        Long subCategoryId = categoryService.insert(new Category.Builder().setName("Sub 1").setParent(categoryService.findById(mainCategoryId)).build());
        categoryService.insert(new Category.Builder().setName("Sub 2").setParent(categoryService.findById(mainCategoryId)).build());

        categoryService.deleteById(subCategoryId);

        assertThat(categoryService.getMainCategories()).hasSize(1);
        assertThat(categoryService.count()).isEqualTo(2);
    }

    @Test
    public void deleteMainCategoryWithSubs() {
        Long otherMainCategory = categoryService.insert(new Category.Builder().setName("Main 1").setType(Category.Type.EXPENSE).build());
        categoryService.insert(new Category.Builder().setName("Sub 1 of Main 1").setParent(categoryService.findById(otherMainCategory)).build());
        categoryService.insert(new Category.Builder().setName("Sub 2 of Main 1").setParent(categoryService.findById(otherMainCategory)).build());

        Integer oldMainCategorySize = categoryService.getMainCategories().size();
        Long oldCount = categoryService.count();

        Long mainCategoryToDelete = categoryService.insert(new Category.Builder().setName("Main 2").setType(Category.Type.EXPENSE).build());
        categoryService.insert(new Category.Builder().setName("Sub 1 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)).build());
        categoryService.insert(new Category.Builder().setName("Sub 2 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)).build());

        categoryService.deleteByIdWithSubcategories(mainCategoryToDelete);

        assertThat(categoryService.count()).isEqualTo(oldCount);
        assertThat(categoryService.getMainCategories().size()).isEqualTo(oldMainCategorySize);
    }
}
