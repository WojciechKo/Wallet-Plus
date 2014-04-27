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
    private static final String CATEGORY_CLASS_NAME = "GreenCategory";
    private static final String CATEGORY_PROPERTY_PARENT_ID = "parentId";
    private static final String CATEGORY_PROPERTY_NAME = "name";
    private static final String CATEGORY_PROPERTY_TYPE = "type";

    // Record table
    private static final String RECORD_TABLE_NAME = "Record";
    private static final String RECORD_CLASS_NAME = "GreenRecord";
    private static final String RECORD_PROPERTY_AMOUNT = "amount";
    private static final String RECORD_PROPERTY_CATEGORY_ID = "categoryId";
    private static final String RECORD_PROPERTY_DESCRIPTION = "description";
    private static final String RECORD_PROPERTY_DATETIME = "dateTime";

    public static void main(String[] args) throws Exception {
        schema = new Schema(SCHEMA_VERSION, SRC_PACKAGE);
        addEntities();
        addRelations();
        schema.setDefaultJavaPackageDao(SRC_PACKAGE);
        new DaoGenerator().generateAll(schema, OUTPUT_SRC_DIR);
    }

    private static void addEntities() {
        addCategory();
        addRecord();
    }

    private static void addCategory() {
        Entity category = schema.addEntity(CATEGORY_CLASS_NAME);
        category.setTableName(CATEGORY_TABLE_NAME);
        category.addIdProperty();
        category.addLongProperty(CATEGORY_PROPERTY_PARENT_ID);
        category.addStringProperty(CATEGORY_PROPERTY_NAME).notNull().unique();
        category.addIntProperty(CATEGORY_PROPERTY_TYPE);
        category.setHasKeepSections(true);
    }

    private static void addRecord() {
        Entity record = schema.addEntity(RECORD_CLASS_NAME);
        record.setTableName(RECORD_TABLE_NAME);
        record.addIdProperty();
        record.addFloatProperty(RECORD_PROPERTY_AMOUNT).notNull();
        record.addLongProperty(RECORD_PROPERTY_CATEGORY_ID).notNull();
        record.addStringProperty(RECORD_PROPERTY_DESCRIPTION);
        record.addDateProperty(RECORD_PROPERTY_DATETIME).notNull();
        record.setHasKeepSections(true);
    }

    // Relations //
    private static void addRelations() {
        addCategoryToParentCategory();
        addRecordToCategory();
    }

    private static void addCategoryToParentCategory() {
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Property parentCategoryId = getPropertyByName(category, CATEGORY_PROPERTY_PARENT_ID);
        category.addToMany(category, parentCategoryId).setName("children");
        category.addToOne(category, parentCategoryId).setName("parent");
    }

    private static void addRecordToCategory() {
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Entity record = getEntityByName(RECORD_CLASS_NAME);
        Property categoryId = getPropertyByName(record, RECORD_PROPERTY_CATEGORY_ID);
        record.addToOne(category, categoryId);
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
