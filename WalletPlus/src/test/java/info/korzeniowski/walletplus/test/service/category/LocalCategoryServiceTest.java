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

    /**
     * *************
     * TEST INSERT *
     * *************
     */
    @Test
    public void shouldInsertMainAndTwoSubCategories() {
        Integer mainCategorySize = categoryService.getAll().size();

        categoryService.insert(new Category().setName("Main"));

        assertThat(categoryService.getAll()).hasSize(mainCategorySize + 1);
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
        Category inserted = new Category().setName(categoryName);
        Long id = categoryService.insert(inserted);

        Category read = categoryService.findById(id);

        assertThat(inserted).isEqualTo(read);
    }

    /**
     * ***********
     * TEST EDIT *
     * ***********
     */
    @Test
    public void shouldEditNameInMainCategoryWithoutChildren() {
        Category oldCategory = new Category().setName("Main 1");
        categoryService.insert(oldCategory);

        Category newCategory = new Category().setId(oldCategory.getId()).setName("Main 1 Fix");
        categoryService.update(newCategory);

        Category read = categoryService.findById(newCategory.getId());
        assertThat(oldCategory.getId()).isEqualTo(newCategory.getId());
        assertThat(read).isEqualTo(newCategory);
    }

    @Test
    public void shouldEditNameInMainCategoryWithChildren() {
        insertMainAndSubs(new Category().setName("Main 1"), 2);
        Category main2 = insertMainAndSubs(new Category().setName("Main 2"), 3);

        Long oldCategoryCount = categoryService.count();
        Integer oldMainSize = categoryService.getAll().size();

        Category read = categoryService.findById(main2.getId());
        categoryService.update(read.setName("Main 2 Fix"));

        assertThat(categoryService.count()).isEqualTo(oldCategoryCount);
        assertThat(categoryService.getAll()).hasSize(oldMainSize);
    }

    private Category insertMainAndSubs(Category category, Integer numberOfChildren) {
        categoryService.insert(category);
        for (int i = 0; i < numberOfChildren; i++) {
            categoryService.insert(new Category().setName(getSubName(i, category.getName())));
        }
        return category;
    }

    private String getSubName(int number, String mainName) {
        return "Sub " + number + " of " + mainName;
    }

    /**
     * *************
     * TEST DELETE *
     * *************
     */
    @Test
    public void shouldDeleteMainCategoryWithoutSubs() {
        Long categoryId = categoryService.insert(new Category().setName("Main"));

        int mainCategories = categoryService.getAll().size();
        Long count = categoryService.count();
        categoryService.deleteById(categoryId);

        assertThat(categoryService.getAll()).hasSize(mainCategories - 1);
        assertThat(categoryService.count()).isEqualTo(count - 1);
    }
}
