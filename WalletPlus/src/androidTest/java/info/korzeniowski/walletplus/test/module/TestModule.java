package info.korzeniowski.walletplus.test.module;

import dagger.Module;

@Module(
//        injects = {
//                LocalCategoryDataManagerTest.class
//        }
        //includes = DatabaseModule.class,
        //overrides = true
)
public class TestModule {

//    private SQLiteOpenHelper dbHelper;
//    private SQLiteDatabase database;
//    private DaoMaster daoMaster;
//    private DaoSession daoSession;

//    public TestDatabaseModule() {
//        TestWalletPlus application = (TestWalletPlus) Robolectric.application;
//        dbHelper = new DaoMaster.DevOpenHelper(application, "test-" + application.DATABASE_NAME, null);
//        database = dbHelper.getWritableDatabase();
//        daoMaster = new DaoMaster(database);
//        daoSession = daoMaster.newSession();
//    }

//    @Provides
//    @Singleton
//    public TestClass provideTestClass() {
//        return new TestClass();
////        return daoSession.getCategoryDao();
//    }

//    @Provides
//    @Singleton
//    public CategoryDao provideCategoryDao() {
//        return new CategoryDao(null, null);
////        return daoSession.getCategoryDao();
//    }
}
