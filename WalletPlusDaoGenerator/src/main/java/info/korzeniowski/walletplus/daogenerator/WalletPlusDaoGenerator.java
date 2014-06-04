package info.korzeniowski.walletplus.daogenerator;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

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
    private static final String ENTITY_ID = "GUID";

    // Account table
    private static final String ACCOUNT_TABLE_NAME = "Account";
    private static final String ACCOUNT_CLASS_NAME = "GreenAccount";
    private static final String ACCOUNT_PROPERTY_NAME = "name";
    private static final String ACCOUNT_PROPERTY_PASSWORD_HASH = "passwordHash";

    // Category table
    private static final String CATEGORY_TABLE_NAME = "Category";
    private static final String CATEGORY_CLASS_NAME = "GreenCategory";
    private static final String CATEGORY_PROPERTY_ACCOUNT_ID = "accountId";
    private static final String CATEGORY_PROPERTY_PARENT_ID = "parentId";
    private static final String CATEGORY_PROPERTY_NAME = "name";
    private static final String CATEGORY_PROPERTY_TYPE = "type";

    // Wallet table
    private static final String WALLET_TABLE_NAME = "Wallet";
    private static final String WALLET_CLASS_NAME = "GreenWallet";
    private static final String WALLET_PROPERTY_ACCOUNT_ID = "accountId";
    private static final String WALLET_PROPERTY_NAME = "name";
    private static final String WALLET_PROPERTY_INITIAL_AMOUNT = "initialAmount";
    private static final String WALLET_PROPERTY_CURRENT_AMOUNT = "currentAmount";
    private static final String WALLET_PROPERTY_TYPE = "type";

    // Cash flow table
    private static final String CASH_FLOW_TABLE_NAME = "CashFlow";
    private static final String CASH_FLOW_CLASS_NAME = "GreenCashFlow";
    private static final String CASH_FLOW_PROPERTY_FROM_WALLET_ID = "fromWalletId";
    private static final String CASH_FLOW_PROPERTY_TO_WALLET_ID = "toWalletId";
    private static final String CASH_FLOW_PROPERTY_AMOUNT = "amount";
    private static final String CASH_FLOW_PROPERTY_CATEGORY_ID = "categoryId";
    private static final String CASH_FLOW_PROPERTY_COMMENT = "comment";
    private static final String CASH_FLOW_PROPERTY_DATETIME = "dateTime";

    public static void main(String[] args) throws Exception {
        schema = new Schema(SCHEMA_VERSION, SRC_PACKAGE);
        addEntities();
        addRelations();
        schema.setDefaultJavaPackageDao(SRC_PACKAGE);
        new DaoGenerator().generateAll(schema, OUTPUT_SRC_DIR);
    }

    private static void addEntities() {
        addAccount();
        addCategory();
        addWallet();
        addCashFlow();
    }

    private static void addAccount() {
        Entity account = schema.addEntity(ACCOUNT_CLASS_NAME);
        account.setTableName(ACCOUNT_TABLE_NAME);
        account.addIdProperty();
        account.addStringProperty(ACCOUNT_PROPERTY_NAME).notNull();
        account.addStringProperty(ACCOUNT_PROPERTY_PASSWORD_HASH);
        account.setHasKeepSections(true);
    }

    private static void addCategory() {
        Entity category = schema.addEntity(CATEGORY_CLASS_NAME);
        category.setTableName(CATEGORY_TABLE_NAME);
        category.addIdProperty();
        category.addLongProperty(CATEGORY_PROPERTY_PARENT_ID);
        category.addLongProperty(CATEGORY_PROPERTY_ACCOUNT_ID);
        category.addStringProperty(CATEGORY_PROPERTY_NAME).notNull().unique();
        category.addIntProperty(CATEGORY_PROPERTY_TYPE);
        category.setHasKeepSections(true);
    }

    private static void addWallet() {
        Entity wallet = schema.addEntity(WALLET_CLASS_NAME);
        wallet.setTableName(WALLET_TABLE_NAME);
        wallet.addIdProperty();
        wallet.addLongProperty(WALLET_PROPERTY_ACCOUNT_ID);
        wallet.addStringProperty(WALLET_PROPERTY_NAME).notNull();
        wallet.addDoubleProperty(WALLET_PROPERTY_INITIAL_AMOUNT).notNull();
        wallet.addDoubleProperty(WALLET_PROPERTY_CURRENT_AMOUNT);
        wallet.addIntProperty(WALLET_PROPERTY_TYPE).notNull();
        wallet.setHasKeepSections(true);
    }

    private static void addCashFlow() {
        Entity cashFlow = schema.addEntity(CASH_FLOW_CLASS_NAME);
        cashFlow.setTableName(CASH_FLOW_TABLE_NAME);
        cashFlow.addIdProperty();
        cashFlow.addLongProperty(CASH_FLOW_PROPERTY_FROM_WALLET_ID);
        cashFlow.addLongProperty(CASH_FLOW_PROPERTY_TO_WALLET_ID);
        cashFlow.addFloatProperty(CASH_FLOW_PROPERTY_AMOUNT).notNull();
        cashFlow.addLongProperty(CASH_FLOW_PROPERTY_CATEGORY_ID).notNull();
        cashFlow.addStringProperty(CASH_FLOW_PROPERTY_COMMENT);
        cashFlow.addDateProperty(CASH_FLOW_PROPERTY_DATETIME).notNull();
        cashFlow.setHasKeepSections(true);
    }

    /**
     * Relations
     */
    private static void addRelations() {
        addAccountToCategory();
        addAccountToWallet();
        addCategoryToParentCategory();
        addCashFlowToCategory();
        addCashFlowToWallet();
    }

    private static void addAccountToCategory() {
        Entity account = getEntityByName(ACCOUNT_CLASS_NAME);
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Property accountId = getPropertyByName(category, CATEGORY_PROPERTY_ACCOUNT_ID);
        account.addToMany(category, accountId);
    }

    private static void addCategoryToParentCategory() {
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Property parentCategoryId = getPropertyByName(category, CATEGORY_PROPERTY_PARENT_ID);
        category.addToMany(category, parentCategoryId).setName("children");
        category.addToOne(category, parentCategoryId).setName("parent");
    }

    private static void addAccountToWallet() {
        Entity account = getEntityByName(ACCOUNT_CLASS_NAME);
        Entity wallet = getEntityByName(WALLET_CLASS_NAME);
        Property accountId = getPropertyByName(wallet, WALLET_PROPERTY_ACCOUNT_ID);
        account.addToMany(wallet, accountId);
    }

    private static void addCashFlowToWallet() {
        Entity wallet = getEntityByName(WALLET_CLASS_NAME);
        Entity cashFlow = getEntityByName(CASH_FLOW_CLASS_NAME);
        Property fromWalletId = getPropertyByName(cashFlow, CASH_FLOW_PROPERTY_FROM_WALLET_ID);
        Property toWalletId = getPropertyByName(cashFlow, CASH_FLOW_PROPERTY_TO_WALLET_ID);
        cashFlow.addToOne(wallet, fromWalletId).setName("from" + WALLET_CLASS_NAME);
        cashFlow.addToOne(wallet, toWalletId).setName("to" + WALLET_CLASS_NAME);
    }

    private static void addCashFlowToCategory() {
        Entity category = getEntityByName(CATEGORY_CLASS_NAME);
        Entity cashFlow = getEntityByName(CASH_FLOW_CLASS_NAME);
        Property categoryId = getPropertyByName(cashFlow, CASH_FLOW_PROPERTY_CATEGORY_ID);
        cashFlow.addToOne(category, categoryId);
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
}
