package info.korzeniowski.walletplus.service.local.validation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.service.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.service.exception.EntityPropertyCannotBeNullOrEmptyException;
import info.korzeniowski.walletplus.service.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.service.exception.SubCategoryCantHaveTypeException;

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
        if (category.getParent() == null) {
            validateInsertMain(category);
        } else {
            validateInsertSub(category);
        }
    }

    private void validateInsertMain(Category category) {
        validateIfCategoryTypeIsNotNullOrEmpty(category);
    }

    private void validateInsertSub(Category category) {
        validateIfParentCategoryIsMain(category);
        validateIfCategoryHaveNoType(category);
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
        validateIfCategoryHaveNoType(newValue);
        validateIfCategoryHaveNoChildren(toUpdate);
    }

    private void validateUpdateSubToMain(Category newValue, Category toUpdate) {
        validateIfCategoryTypeIsNotNullOrEmpty(newValue);
    }

    private void validateSubToSub(Category newValue) {
        validateIfCategoryHaveNoType(newValue);
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
        if (category.getType() == null) {
            return;
        }
        throw new SubCategoryCantHaveTypeException();
    }

    private void validateIfCategoryTypeIsNotNullOrEmpty(Category category) {
        if (category.getType() == null) {
            throw new EntityPropertyCannotBeNullOrEmptyException(Category.class.getSimpleName(), "Type");
        }
    }

    private boolean isMainCategory(final Long id) {
        return categoryService.findById(id).getParent() == null;
    }

    private void validateIfParentCategoryIsMain(Category category) {
        if (!isMainCategory(category.getParent().getId())) {
            throw new ParentCategoryIsNotMainCategoryException();
        }
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
