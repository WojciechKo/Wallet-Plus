package info.korzeniowski.walletplus.datamanager.local.validation;

import com.google.common.base.Objects;
import com.google.common.base.Strings;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveSubsException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryNameMustBeUniqueException;
import info.korzeniowski.walletplus.datamanager.exception.EntityAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.EntityPropertyCannotBeEmptyException;
import info.korzeniowski.walletplus.datamanager.exception.ParentCategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.exception.SubCategoryCantHaveTypeDifferentThanParentException;
import info.korzeniowski.walletplus.model.Category;

public class CategoryValidator implements Validator<Category>{
    private final CategoryDataManager categoryDataManager;

    public CategoryValidator(CategoryDataManager categoryDataManager) {
        this.categoryDataManager = categoryDataManager;
    }

    @Override
    public void validateInsert(Category category) {
        validateIfNameIsNotNullOrEmpty(category);
        validateIfNameIsUnique(category);
        validateIfIdIsUnique(category);
        if (category.getParentId() == null) {
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
        validateIfCategoryHaveTypeLikeParentOrNone(category);
    }

    @Override
    public void validateUpdate(Category newValue, Category toUpdate) {
        validateIfNameIsNotNullOrEmpty(newValue);
        validateIfNewNameIsUnique(newValue, toUpdate);
        validateIfNewIdIsUnique(newValue, toUpdate);
        if(newValue.getParentId() == null && toUpdate.getParentId() == null) {
            validateUpdateMainToMain(newValue);
        } else if (toUpdate.getParentId() == null) {
            validateUpdateMainToSub(newValue, toUpdate);
        } else if(newValue.getParentId() == null) {
            validateUpdateSubToMain(newValue, toUpdate);
        } else {
            validateSubToSub(newValue);
        }
    }

    private void validateUpdateMainToMain(Category newValue) {
        validateInsertMain(newValue);
    }

    private void validateUpdateMainToSub(Category newValue, Category toUpdate) {
        validateIfCategoryHaveTypeLikeParentOrNone(newValue);
        validateIfCategoryHaveNoChildren(toUpdate);
    }

    private void validateUpdateSubToMain(Category newValue, Category toUpdate) {
        validateIfCategoryTypeIsNotNullOrEmpty(newValue);
    }

    private void validateSubToSub(Category newValue) {
        validateIfCategoryHaveTypeLikeParentOrNone(newValue);
        validateIfParentCategoryIsMain(newValue);
    }

    @Override
    public void validateDelete(Category category) {
        if (category.getParentId() == null) {
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

    /*******************************
     * Unit validations
     *******************************/

    private void validateIfNameIsNotNullOrEmpty(Category category) {
        if (Strings.isNullOrEmpty(category.getName())) {
            throw new EntityPropertyCannotBeEmptyException(Category.class.getSimpleName(), "Name");
        }
    }

    private void validateIfNameIsUnique(Category category) {
        if (categoryDataManager.findByName(category.getName()) != null) {
            throw new CategoryNameMustBeUniqueException();
        }
    }

    private void validateIfIdIsUnique(Category category) {
        if (categoryDataManager.findById(category.getId()) != null) {
            throw new EntityAlreadyExistsException(Category.class.getSimpleName(), category.getId());
        }
    }

    private void validateIfNewNameIsUnique(Category newValue, Category toUpdate) {
        if (!Objects.equal(newValue.getName(), toUpdate.getName())) {
            validateIfNameIsUnique(newValue);
        }
    }

    private void validateIfCategoryHaveNoChildren(Category category) {
        if(!categoryDataManager.getSubCategoriesOf(category.getId()).isEmpty()) {
            throw new CategoryHaveSubsException();
        }
    }

    private void validateIfCategoryHaveTypeLikeParentOrNone(Category category) {
        if (category.getTypes().isEmpty()) {
            return;
        }
        Category parent = categoryDataManager.findById(category.getParentId());
        if (!parent.getTypes().equals(category.getTypes())) {
            throw new SubCategoryCantHaveTypeDifferentThanParentException();
        }
    }

    private void validateIfCategoryTypeIsNotNullOrEmpty(Category category) {
        if (category.getTypes().isEmpty()) {
            throw new EntityPropertyCannotBeEmptyException(category.getClass().getSimpleName(), "Type");
        }
    }

    private void validateIfNewIdIsUnique(Category newValue, Category toUpdate) {
        if (!Objects.equal(newValue.getId(), toUpdate.getId())) {
            validateIfIdIsUnique(newValue);
        }
    }

    private boolean isMainCategory(final Long id) {
        return Category.findById(categoryDataManager.getMainCategories(), id) != null;
    }

    private void validateIfParentCategoryIsMain(Category category) {
        if (!isMainCategory(category.getParentId())) {
            throw new ParentCategoryIsNotMainCategoryException();
        }
    }
}
