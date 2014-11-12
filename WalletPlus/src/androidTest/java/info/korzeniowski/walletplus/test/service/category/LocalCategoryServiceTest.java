package info.korzeniowski.walletplus.test.service.category;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.TestWalletPlus;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LocalCategoryServiceTest {

    @Inject
    @Named("local")
    CategoryService categoryService;

    @Before
    public void setUp() {
        ((TestWalletPlus) Robolectric.application).inject(this);
    }

    @Test
    public void shouldReadSetOfSoredCategoryType() {
        shouldReadSoredCategoryType(Category.Type.INCOME);
        shouldReadSoredCategoryType(Category.Type.EXPENSE);
        shouldReadSoredCategoryType(Category.Type.INCOME_EXPENSE);
    }

    private void shouldReadSoredCategoryType(Category.Type type) {
        Category category = new Category().setName("Category Test Name").setType(type);
        categoryService.insert(category);
        category = categoryService.findById(category.getId());

        assertThat(category.getType()).isEqualTo(type);
    }

    /**
     * *************
     * TEST INSERT *
     * *************
     */
    @Test
    public void shouldInsertMainAndTwoSubCategory() {
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.INCOME);
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.EXPENSE);
        shouldInsertMainTwoSubCategoriesOfType(Category.Type.INCOME_EXPENSE);
    }

    private void shouldInsertMainTwoSubCategoriesOfType(Category.Type type) {
        Integer numberOfIncomeMain = categoryService.getMainIncomeTypeCategories().size();
        Integer numberOfExpenseMain = categoryService.getMainExpenseTypeCategories().size();

        Category main = new Category().setName("Main").setType(type);
        categoryService.insert(main);

        Integer mainCategoriesSize = categoryService.getMainCategories().size();
        if (main.isIncomeType()) {
            numberOfIncomeMain++;
        }
        if (main.isExpenseType()) {
            numberOfExpenseMain++;
        }
        assertThat(categoryService.getMainCategories()).hasSize(mainCategoriesSize);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.findById(main.getId()).getChildren()).hasSize(0);

        categoryService.insert(new Category().setParent(categoryService.findById(main.getId())).setName("1. Sub of Main"));
        categoryService.insert(new Category().setParent(categoryService.findById(main.getId())).setName("2. Sub of Main"));

        assertThat(categoryService.getMainCategories()).hasSize(mainCategoriesSize);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(numberOfIncomeMain);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(numberOfExpenseMain);
        assertThat(categoryService.findById(main.getId()).getChildren()).hasSize(2);
    }

    /**
     * ***********
     * TEST READ *
     * ***********
     */
    @Test
    public void shouldReturnNullWhenTryingToFindNonExistingCategoryById() {
        Long categoryId = (long) 5326432;
        assertThat(categoryService.findById(categoryId)).isNull();
    }

    @Test
    public void shouldReadMainCategory() {
        String categoryName = "Main";
        Category.Type categoryType = Category.Type.INCOME;
        Category inserted = new Category().setName(categoryName).setType(categoryType);
        Long id = categoryService.insert(inserted);

        Category read = categoryService.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    @Test
    public void shouldReadSubCategory() {
        Category parentCategory = new Category().setName("Main").setType(Category.Type.INCOME);
        Long parentCategoryId = categoryService.insert(parentCategory);
        Category subCategory = new Category().setName("Sub").setParent(categoryService.findById(parentCategoryId));
        Long subCategoryId = categoryService.insert(subCategory);

        Category readSubCategory = categoryService.getMainCategories().get(0).getChildren().iterator().next();

        assertThat(readSubCategory).isEqualTo(subCategory);
        assertThat(readSubCategory).isEqualTo(categoryService.findById(subCategoryId));
    }

    /**
     * ***********
     * TEST EDIT *
     * ***********
     */
    @Test
    public void shouldEditNameAndTypeInMainCategoryWithoutChildren() {
        Category oldCategory = new Category().setName("Main 1").setType(Category.Type.EXPENSE);
        categoryService.insert(oldCategory);

        Category newCategory = new Category().setId(oldCategory.getId()).setName("Main 1 Fix").setType(Category.Type.INCOME);
        categoryService.update(newCategory);

        Category read = categoryService.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(read).isEqualTo(newCategory);
    }

    @Test
    public void shouldEditNameAndTypeInMainCategoryWithChildren() {
        Category main1 = insertMainAndSubs(new Category().setName("Main 1").setType(Category.Type.INCOME), 2);
        Category main2 = insertMainAndSubs(new Category().setName("Main 2").setType(Category.Type.INCOME), 3);

        Long oldCategoryCount = categoryService.count();
        Integer oldMainSize = categoryService.getMainCategories().size();
        Integer oldMainIncomeSize = categoryService.getMainIncomeTypeCategories().size();
        Integer oldMainExpenseSize = categoryService.getMainExpenseTypeCategories().size();

        Category read = categoryService.findById(main2.getId());
        categoryService.update(read.setName("Main 2 Fix").setType(Category.Type.EXPENSE));

        assertThat(categoryService.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryService.getMainCategories()).hasSize(oldMainSize);
        assertThat(categoryService.getMainIncomeTypeCategories()).hasSize(oldMainIncomeSize - 1);
        assertThat(categoryService.getMainExpenseTypeCategories()).hasSize(oldMainExpenseSize + 1);
    }

    private Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryService.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryService.insert(new Category().setName(getSubName(i, category.getName())).setParent(categoryService.findById(category.getId())));
        }
        return category;
    }

    private String getSubName(int number, String mainName) {
        return "Sub " + number + " of " + mainName;
    }

    @Test
    public void shouldEditSubName() {
        Category.Type category = Category.Type.INCOME_EXPENSE;
        Long mainId = categoryService.insert(new Category().setName("Main").setType(category));
        Long subId = categoryService.insert(new Category().setName("Sub").setParent(categoryService.findById(mainId)));

        String newSubName = "Sub Fix";
        int mainCategories = categoryService.getMainCategories().size();
        Long count = categoryService.count();

        categoryService.update(categoryService.findById(subId).setName(newSubName));

        assertThat(categoryService.getMainCategories()).hasSize(mainCategories);
        assertThat(categoryService.count()).isEqualTo(count);
        assertThat(categoryService.findById(subId).getName()).isEqualTo(newSubName);
    }

    /**
     * *************
     * TEST DELETE *
     * *************
     */
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryService.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));

        int mainCategories = categoryService.getMainCategories().size();
        Long count = categoryService.count();
        categoryService.deleteById(categoryId);

        assertThat(categoryService.getMainCategories()).hasSize(mainCategories - 1);
        assertThat(categoryService.count()).isEqualTo(count - 1);
    }

    @Test
    public void shouldDeleteSubCategory() {
        Long mainCategoryId = categoryService.insert(new Category().setName("Main").setType(Category.Type.EXPENSE));
        Long subCategoryId = categoryService.insert(new Category().setName("Sub 1").setParent(categoryService.findById(mainCategoryId)));
        categoryService.insert(new Category().setName("Sub 2").setParent(categoryService.findById(mainCategoryId)));

        int mainCategories = categoryService.getMainCategories().size();
        Long count = categoryService.count();

        categoryService.deleteById(subCategoryId);

        assertThat(categoryService.getMainCategories()).hasSize(mainCategories);
        assertThat(categoryService.count()).isEqualTo(count - 1);
    }

    @Test
    public void shouldDeleteMainCategoryWithSubs() {
        Long otherMainCategory = categoryService.insert(new Category().setName("Main 1").setType(Category.Type.EXPENSE));
        categoryService.insert(new Category().setName("Sub 1 of Main 1").setParent(categoryService.findById(otherMainCategory)));
        categoryService.insert(new Category().setName("Sub 2 of Main 1").setParent(categoryService.findById(otherMainCategory)));

        Integer oldMainCategoryCount = categoryService.getMainCategories().size();
        Long oldCount = categoryService.count();

        Long mainCategoryToDelete = categoryService.insert(new Category().setName("Main 2").setType(Category.Type.EXPENSE));
        categoryService.insert(new Category().setName("Sub 1 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)));
        categoryService.insert(new Category().setName("Sub 2 of Main 2").setParent(categoryService.findById(mainCategoryToDelete)));

        categoryService.deleteByIdWithSubcategories(mainCategoryToDelete);

        assertThat(categoryService.count()).isEqualTo(oldCount);
        assertThat(categoryService.getMainCategories().size()).isEqualTo(oldMainCategoryCount);
    }
}
