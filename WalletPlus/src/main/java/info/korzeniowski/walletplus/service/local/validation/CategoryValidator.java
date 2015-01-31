package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryCantHaveTypeException;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.ParentCategoryIsNotMainCategoryException;

import static com.google.common.base.Preconditions.checkNotNull;

public class CategoryValidator implements Validator<Category> {
    private final CategoryService categoryService;

    public CategoryValidator(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Override
    public void validateInsert(Category category) {
        checkNotNull(category);
        validateIfCategoryHaveNoType(category);
        validateIfNameIsNotNullOrEmpty(category);
        validateIfIdIsUnique(category);
        if (category.getParent() == null) {
            validateInsertMain(category);
        } else {
            validateInsertSub(category);
        }
    }

    private void validateInsertMain(Category category) {

    }

    private void validateInsertSub(Category category) {
        validateIfParentCategoryIsMain(category);
    }

    @Override
    public void validateDelete(Long id) {
        Category category = categoryService.findById(id);
        if (category.getParent() == null) {
            validateDeleteMain(category);
        } else {
            validateDeleteSub(category);
        }
    }

    private void validateDeleteMain(Category category) {
        validateIfCategoryHaveNoChildren(category);
    }

    private void validateDeleteSub(Category category) {

    }

    @Override
    public void validateUpdate(Category newCategory) {
        checkNotNull(newCategory);
        validateIfCategoryHaveNoType(newCategory);
        Category oldCategory = categoryService.findById(newCategory.getId());
        validateIfNameIsNotNullOrEmpty(newCategory);
        validateIfNewIdIsUnique(newCategory, oldCategory);
        if (newCategory.getParent() == null && oldCategory.getParent() == null) {
            validateUpdateMainToMain(newCategory);
        } else if (oldCategory.getParent() == null) {
            validateUpdateMainToSub(newCategory, oldCategory);
        } else if (newCategory.getParent() == null) {
            validateUpdateSubToMain(newCategory, oldCategory);
        } else {
            validateSubToSub(newCategory);
        }
    }

    private void validateUpdateMainToMain(Category newValue) throws EntityPropertyCannotBeNullOrEmptyException {
        validateInsertMain(newValue);
    }

    private void validateUpdateMainToSub(Category newValue, Category toUpdate) {
        validateIfCategoryHaveNoChildren(toUpdate);
    }

    private void validateUpdateSubToMain(Category newValue, Category toUpdate) {
    }

    private void validateSubToSub(Category newValue) {
        validateIfParentCategoryIsMain(newValue);
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

    private void validateIfCategoryHaveNoType(Category category) {
        if (category.getSpecialType() == null) {
            return;
        }
        throw new CategoryCantHaveTypeException();
    }

    private void validateIfParentCategoryIsMain(Category category) {
        if (!isMainCategory(category.getParent().getId())) {
            throw new ParentCategoryIsNotMainCategoryException();
        }
    }

    private boolean isMainCategory(final Long id) {
        return categoryService.findById(id).getParent() == null;
    }

    private void validateIfCategoryHaveNoChildren(Category category) {
        if (!categoryService.getSubCategoriesOf(category.getId()).isEmpty()) {
            throw new CategoryHaveSubsException();
        }
    }

    private void validateIfNewIdIsUnique(Category newValue, Category toUpdate) {
        if (!Objects.equal(newValue.getId(), toUpdate.getId())) {
            validateIfIdIsUnique(newValue);
        }
    }
}
