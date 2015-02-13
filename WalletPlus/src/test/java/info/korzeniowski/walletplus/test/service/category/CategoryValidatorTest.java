package info.korzeniowski.walletplus.test.service.category;

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
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.local.LocalCategoryService;
import info.korzeniowski.walletplus.service.local.validation.CategoryValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class CategoryValidatorTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private CategoryService categoryService;

    private CategoryService validatorService;

    @Before
    public void setUp() {
        @SuppressWarnings("unchecked")
        Dao<Category, Long> categoryDao = mock(Dao.class);
        validatorService = mock(CategoryService.class);
        categoryService = new LocalCategoryService(categoryDao, new CategoryValidator(validatorService));
    }

    /**
     * *******************
     * INSERT VALIDATION *
     * *******************
     */
    @Test
    public void shouldInsertMainCategory() {
        categoryService.insert(getSimpleMainIncomeCategory());
    }

    @Test
    public void shouldThrowExceptionWhenInsertCategoryWithoutName() {
        exception.expect(EntityPropertyCannotBeNullOrEmptyException.class);
        exception.expectMessage("Name");
        categoryService.insert(getSimpleMainIncomeCategory().setName(null));
    }

    @Test
    public void shouldThrowExceptionWhenInsertWithDuplicatedId() {
        Category toInsert = getSimpleMainIncomeCategory();
        when(validatorService.findById(toInsert.getId())).thenReturn(toInsert);

        exception.expect(EntityAlreadyExistsException.class);
        categoryService.insert(getSimpleMainIncomeCategory().setId(toInsert.getId()));
    }

    /**
     * *******************
     * DELETE VALIDATION *
     * *******************
     */
    @Test
    public void shouldDeleteMainCategory() {
        Category main = getSimpleMainIncomeCategory();
        when(validatorService.findById(main.getId())).thenReturn(main);

        categoryService.deleteById(main.getId());
    }

    /**
     * *******************
     * UPDATE VALIDATION *
     * *******************
     */
    @Test
    public void shouldUpdateNameOfCategory() {
        Category category = getSimpleMainIncomeCategory();
        String newName = "NewName";
        when(validatorService.findById(category.getId())).thenReturn(category);
        when(validatorService.findByName(newName)).thenReturn(null);

        categoryService.update(category.setName(newName));
    }

    private Category getSimpleMainIncomeCategory() {
        UUID id = UUID.randomUUID();
        return new Category()
                .setName("Simple category-" + id.getMostSignificantBits())
                .setId(id.getMostSignificantBits());
    }
}
