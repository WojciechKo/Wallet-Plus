package info.korzeniowski.walletplus.datamanager.local.validation;

import com.google.common.base.Objects;

import java.util.NoSuchElementException;

import info.korzeniowski.walletplus.datamanager.CategoryDataManager;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveChildrenException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveNoParentException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveNoTypesSetException;
import info.korzeniowski.walletplus.datamanager.exception.SubCategoryHaveDifferentTypeThanParentException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryWithGivenIdAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryWithGivenNameAlreadyExistsException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryIsNotMainCategoryException;
import info.korzeniowski.walletplus.datamanager.exception.CategoryHaveParentException;
import info.korzeniowski.walletplus.model.Category;

public class CategoryValidator {
    private final CategoryDataManager categoryDataManager;

    public CategoryValidator(CategoryDataManager categoryDataManager) {
        this.categoryDataManager = categoryDataManager;
    }

    public void validateInsert(Category category) {
        new InsertApplier() {
            @Override
            protected void commonApply(Category category) {
                validateIfIdIsUnique(category.getId());
                validateIfNameIsUnique(category.getName());
            }

            @Override
            protected void toMainApply(Category category) {
                validateIfCategoryHaveType(category);
            }

            @Override
            protected void toSubApply(Category category) {
                validateIfCategoryIsMain(category.getParentId());
                validateIfCategoryHaveNoTypeOrParentTypes(category);
            }
        }.apply(category);
    }

    public void validateUpdate(Category newValue, Category toUpdate) {
        new UpdateApplier() {
            @Override
            protected void commonApply(Category newValue, Category toUpdate) {
                validateIfNewNameIsUnique(newValue, toUpdate);
            }

            @Override
            protected void mainToMainApply(Category newValue, Category toUpdate) {
                validateIfCategoryHaveType(newValue);
                validateIfCategoryHaveNoParent(newValue);
            }

            @Override
            protected void mainToSubApply(Category newValue, Category toUpdate) {
                validateIfCategoryHaveNoTypeOrParentTypes(newValue);
                validateIfCategoryHaveNoChildren(toUpdate);
                validateIfCategoryIsMain(newValue.getParentId());
            }

            @Override
            protected void subToMainApply(Category newValue, Category toUpdate) {
                validateIfCategoryHaveType(newValue);
                validateIfCategoryHaveNoChildren(toUpdate);
                validateIfCategoryHaveNoParent(newValue);
            }

            @Override
            protected void subToSubApply(Category newValue, Category toUpdate) {
                validateIfCategoryHaveNoTypeOrParentTypes(newValue);
                validateIfCategoryIsMain(newValue.getParentId());
            }
        }.apply(newValue, toUpdate);
    }

    private void validateIfCategoryHaveNoChildren(Category category) {
        if(!category.getChildren().isEmpty()) {
            throw new CategoryHaveChildrenException("Category id: " + category.getId());
        }
    }

    private void validateIfCategoryHaveNoTypeOrParentTypes(Category category) {
        if (category.getTypes().isEmpty()) {
            return;
        }
        try {
            Category parent = categoryDataManager.findById(category.getParentId());
            if (!parent.getTypes().equals(category.getTypes())) {
                throw new SubCategoryHaveDifferentTypeThanParentException(
                        "Category name: " + category.getName() + " have different Types than parent " + parent.getName());
            }
        } catch (NoSuchElementException e) {
            throw new CategoryHaveNoParentException("Category name: " + category.getName());
        }

    }

    private void validateIfCategoryHaveType(Category category) {
        if (category.getTypes().isEmpty()) {
            throw new CategoryHaveNoTypesSetException("Category id: " + category.getId());
        }
    }

    private void validateIfCategoryHaveNoParent(Category category) {
        if (category.getParentId() != null) {
            throw new CategoryHaveParentException("Category id: " + category.getId() + "; Parent id:" + category.getParentId());
        }
    }

    private void validateIfCategoryIsMain(Long id) {
        if (!isMainCategory(id)) {
            throw new CategoryIsNotMainCategoryException("Category id: " + id);
        }
    }

    private void validateIfNewNameIsUnique(Category newValue, Category toUpdate) {
        if (!Objects.equal(newValue.getName(), toUpdate.getName())) {
            validateIfNameIsUnique(newValue.getName());
        }
    }

    private void validateIfNameIsUnique(String name) {
        if (name == null) return;
        try {
            categoryDataManager.findByName(name);
        } catch (NoSuchElementException e) {
            return;
        }
        throw new CategoryWithGivenNameAlreadyExistsException(name);
    }

    private void validateIfIdIsUnique(Long id) {
        if (id == null) return;
        try {
            categoryDataManager.findById(id);
        } catch (NoSuchElementException e) {
            return;
        }
        throw new CategoryWithGivenIdAlreadyExistsException("Category id: " + id);
    }

    private boolean isMainCategory(final Long id) {
        return Category.tryFindById(categoryDataManager.getMainCategories(), id) != null;
    }

    protected abstract class UpdateApplier {
        public void apply(Category newValue, Category toUpdate) {
            commonApply(newValue, toUpdate);
            if(newValue.getParentId() == null && toUpdate.getParentId() == null) {
                mainToMainApply(newValue, toUpdate);
            } else if (toUpdate.getParentId() == null) {
                mainToSubApply(newValue, toUpdate);
            } else if(newValue.getParentId() == null) {
                subToMainApply(newValue, toUpdate);
            } else {
                subToSubApply(newValue, toUpdate);
            }
        }

        protected abstract void commonApply(Category newValue, Category toUpdate);
        protected abstract void mainToMainApply(Category newValue, Category toUpdate);
        protected abstract void mainToSubApply(Category newValue, Category toUpdate);
        protected abstract void subToMainApply(Category newValue, Category toUpdate);
        protected abstract void subToSubApply(Category newValue, Category toUpdate);
    }

    protected abstract class InsertApplier {
        public void apply(Category category) {
            commonApply(category);
            if (category.getParentId() == null) {
                toMainApply(category);
            } else {
                toSubApply(category);
            }
        }
        protected abstract void commonApply(Category category);
        protected abstract void toMainApply(Category category);
        protected abstract void toSubApply(Category category);
    }
}
