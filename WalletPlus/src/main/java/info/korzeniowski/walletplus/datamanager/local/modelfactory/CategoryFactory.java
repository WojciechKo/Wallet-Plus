package info.korzeniowski.walletplus.datamanager.local.modelfactory;

import com.google.common.collect.Lists;

import java.util.EnumSet;
import java.util.List;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.greendao.GreenCategory;

import static info.korzeniowski.walletplus.model.Category.findById;

public class CategoryFactory {

    public static Category createCategory(Category category) {
        if (category == null) {
            return null;
        }

        Category newCategory = new Category();
        newCategory.setId(category.getId());
        newCategory.setParentId(category.getParentId());
        newCategory.setName(category.getName());
        newCategory.setTypes(EnumSet.copyOf(category.getTypes()));

        return newCategory;
    }

    public static Category createCategory(GreenCategory greenCategory) {
        if (greenCategory == null) {
            return null;
        }

        Category result = new Category();
        result.setId(greenCategory.getId());
        result.setParentId(greenCategory.getParentId());
        result.setName(greenCategory.getName());
        result.setTypes(Category.Type.convertBitwiseToEnumSet(greenCategory.getType()));

        return result;
    }

    public static GreenCategory createGreenCategory(Category category) {
        if (category == null) {
            return null;
        }

        GreenCategory greenCategory = new GreenCategory();
        greenCategory.setId(category.getId());
        greenCategory.setParentId(category.getParentId());
        greenCategory.setName(category.getName());
        greenCategory.setType(Category.Type.convertEnumToBitwise(category.getTypes()));

        return  greenCategory;
    }

    public static List<Category> createCategoryList(List<GreenCategory> greenCategoryList) {
        List<Category> result = Lists.newArrayList();

        for (GreenCategory original : greenCategoryList) {
            result.add(createCategory(original));
        }

        for (Category copy : result) {
            Category copyParent = findById(result, copy.getParentId());
            if (copyParent != null) {
                copy.setTypes(copyParent.getTypes());
                copyParent.addChild(copy);
            }
            copy.setParent(copyParent);
        }
        return result;
    }


    public static List<Category> copyOfCategoriesWithParentAndChildren(List<Category> originals) {
        List<Category> result = Lists.newArrayList();

        for (Category original : originals) {
            result.add(createCategory(original));
        }

        for (Category copy : result) {
            Category copyParent = findById(result, copy.getParentId());
            if (copyParent != null) {
                copy.setTypes(copyParent.getTypes());
                copyParent.addChild(copy);
            }
            copy.setParent(copyParent);
        }

        return result;
    }

    public static List<Category> copyOfMainCategoriesWithChildren(List<Category> originals) {
        List<Category> copies = Lists.newArrayList();

        for (Category original : originals) {
            Category copy = copyOfMainCategoryWithChildren(original);
            copies.add(copy);
        }

        return copies;
    }

    public static Category copyOfMainCategoryWithChildren(Category original) {
        Category copy = createCategory(original);
        copy.setChildren(copyOfCategoriesWithoutChildren(original.getChildren()));
        for (Category copyChild : copy.getChildren()) {
            copyChild.setParent(copy);
        }
        return copy;
    }

    private static List<Category> copyOfCategoriesWithoutChildren(List<Category> categories) {
        List<Category> copies = Lists.newArrayList();
        for (Category original : categories) {
            copies.add(createCategory(original));
        }
        return copies;
    }
}
