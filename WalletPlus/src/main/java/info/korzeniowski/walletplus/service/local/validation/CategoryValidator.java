package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;

import static com.google.common.base.Preconditions.checkNotNull;

public class CategoryValidator implements Validator<Category> {
    private final CategoryService categoryService;

    public CategoryValidator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void validateInsert(Category category) {
        checkNotNull(category);
        validateIfNameIsNotNullOrEmpty(category);
        validateIfIdIsUnique(category);
    }

    @Override
    public void validateDelete(Long id) {

    }

    @Override
    public void validateUpdate(Category newCategory) {
        checkNotNull(newCategory);
        Category oldCategory = categoryService.findById(newCategory.getId());
        validateIfNameIsNotNullOrEmpty(newCategory);
        validateIfNewIdIsUnique(newCategory, oldCategory);
    }

    /**
     * ****************************
     * Unit validations
     * *****************************
     */

    private void validateIfNameIsNotNullOrEmpty(Category category) {
        if (Strings.isNullOrEmpty(category.getName())) {
            throw new EntityPropertyCannotBeNullOrEmptyException(Category.class.getSimpleName(), "Name");
        }
    }

    private void validateIfIdIsUnique(Category category) {
        if (category.getId() != null && categoryService.findById(category.getId()) != null) {
            throw new EntityAlreadyExistsException(Category.class.getSimpleName(), category.getId());
        }
    }

    private void validateIfNewIdIsUnique(Category newValue, Category toUpdate) {
        if (!Objects.equal(newValue.getId(), toUpdate.getId())) {
            validateIfIdIsUnique(newValue);
        }
    }
}
