package info.korzeniowski.walletplus.daogenerator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.io.File;
import java.io.IOException;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Property;
import de.greenrobot.daogenerator.Schema;

public class WalletPlusDaoGenerator {
    private static Schema schema;
    private static final String OUTPUT_SRC_DIR = "../WalletPlus/src/main/java/";
    private static final String MAIN_PACKAGE = "info.korzeniowski.walletplus";
    private static final String SRC_PACKAGE = MAIN_PACKAGE + ".model.greendao";
    private static final int SCHEMA_VERSION = 1;

    private static final String ENTITY_ID = "id";
    // Category table
    private static final String CATEGORY_TABLE_NAME = "Category";
    private static final String CATEGORY_CLASS_NAME = "CategoryG";
    private static final String CATEGORY_PROPERTY_PARENT_ID = "parentId";
    private static final String CATEGORY_PROPERTY_NAME = "name";
    private static final String CATEGORY_PROPERTY_TYPE = "type";

    public static void main(String[] args) throws Exception {
        schema = new Schema(SCHEMA_VERSION, SRC_PACKAGE);

        addEntities();
        addRelations();

        File srcDir = new File(OUTPUT_SRC_DIR + SRC_PACKAGE.replace('.', '/'));
//        for (File file : srcDir.listFiles()) file.delete();
//        File testDir = new File(OUTPUT_TEST_DIR + TEST_PACKAGE.replace('.', '/'));
//        for (File file : testDir.listFiles()) file.delete();

        schema.setDefaultJavaPackageDao(SRC_PACKAGE);
//        schema.setDefaultJavaPackageTest(TEST_PACKAGE);

        new DaoGenerator().generateAll(schema, OUTPUT_SRC_DIR);
//        new DaoGenerator().generateAll(schema, OUTPUT_SRC_DIR, OUTPUT_TEST_DIR);
    }

    // Entities //
    private static void addEntities() {
        addCategory();
    }

    private static void addCategory() {
        Entity category = schema.addEntity(CATEGORY_CLASS_NAME);
        category.setTableName(CATEGORY_TABLE_NAME);
//        category.setClassNameDao(CATEGORY_TABLE_NAME + "Dao");
        category.addIdProperty();
        category.addLongProperty(CATEGORY_PROPERTY_PARENT_ID);
        category.addStringProperty(CATEGORY_PROPERTY_NAME).notNull().unique();
        category.addIntProperty(CATEGORY_PROPERTY_TYPE);
        category.setHasKeepSections(true);
//        category.implementsInterface(CATEGORY_TABLE_NAME);
        //category.setSuperclass("CategoryBase");
    }

    // Relations //
    private static void addRelations() {
        addCategoryToParentCategory();
    }

    private static void addCategoryToParentCategory() {
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Property parentCategoryId = getPropertyByName(category, CATEGORY_PROPERTY_PARENT_ID);
        category.addToMany(category, parentCategoryId).setName("children");
        category.addToOne(category, parentCategoryId).setName("parent");
    }

    private static Entity getEntityByName(final String name) {
        return Iterables.find(schema.getEntities(),
                new Predicate<Entity>() {
                    @Override
                    public boolean apply(Entity entity) {
                        return name.equals(entity.getClassName());
                    }
                }
        );
    }

    private static Property getPropertyByName(final String entity, final String name) {
        return getPropertyByName(getEntityByName(entity), name);
    }

    private static Property getPropertyByName(final Entity entity, final String name) {
        return Iterables.find(entity.getProperties(),
                new Predicate<Property>() {
                    @Override
                    public boolean apply(Property property) {
                        return name.equals(property.getPropertyName());
                    }
                }
        );
    }

    private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            if (file.list().length==0) {
                file.delete();
            } else {
                String files[] = file.list();

                for (String temp : files) {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }

                if (file.list().length==0) {
                    file.delete();
                }
            }
        } else {
            file.delete();
        }
    }
}
