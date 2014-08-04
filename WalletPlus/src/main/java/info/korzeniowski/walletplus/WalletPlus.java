package info.korzeniowski.walletplus;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.androidannotations.annotations.EApplication;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.module.MainModule;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;

/**
 * Main Application class.
 */
@EApplication
public class WalletPlus extends Application {
    public static final String DATABASE_NAME = "wallet-plus.db";
    public static final String LOG_TAG = "WalletPlus";

    protected ObjectGraph graph;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CategoryService localCategoryService;

    /**
     * Just for Dagger DI.
     */
    @Inject
    public WalletPlus() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        graph = ObjectGraph.create(getModules().toArray());
        inject(this);
        initDatabaseAfterInstallation();
    }

    private void initDatabaseAfterInstallation() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isFirstRun = sharedPreferences.getBoolean("firstRun", true);
        if (isFirstRun) {
            initDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
        }
    }

    private void initDatabase() {
        /** Init my wallets **/
        Wallet.Builder walletBuilder = new Wallet.Builder().setType(Wallet.Type.MY_WALLET);
        Wallet personalWallet = walletBuilder.setName("Personal wallet").setInitialAmount(100.0).build();
        localWalletService.insert(personalWallet);
        Wallet wardrobe = walletBuilder.setName("Wardrobe").setInitialAmount(1500.0).build();
        localWalletService.insert(wardrobe);
        Wallet sock = walletBuilder.setName("Sock").setInitialAmount(500.0).build();
        localWalletService.insert(sock);
        Wallet savingsAccount = walletBuilder.setName("Savings account").setInitialAmount(2500.0).build();
        localWalletService.insert(savingsAccount);

        /** Init other wallets **/
        walletBuilder.setType(Wallet.Type.CONTRACTOR).setInitialAmount(0.0);
        Wallet sevenEleven = walletBuilder.setName("7-Eleven").build();
        localWalletService.insert(sevenEleven);
        Wallet tesco = walletBuilder.setName("Tesco").build();
        localWalletService.insert(tesco);
        Wallet walMart = walletBuilder.setName("Wal-Mart").build();
        localWalletService.insert(walMart);
        Wallet amazon = walletBuilder.setName("Amazon").build();
        localWalletService.insert(amazon);


        /** Init categories **/
        Category mainHouse = new Category().setName("House").setType(Category.Type.EXPENSE);
        localCategoryService.insert(mainHouse);
        localCategoryService.insert(new Category().setParent(mainHouse).setName("Energy"));
        localCategoryService.insert(new Category().setParent(mainHouse).setName("Water"));
        localCategoryService.insert(new Category().setParent(mainHouse).setName("Gas"));

        Category mainInternet = new Category().setName("Internet").setType(Category.Type.INCOME);
        localCategoryService.insert(mainInternet);
        localCategoryService.insert(new Category().setParent(mainInternet).setName("Music forum"));
        localCategoryService.insert(new Category().setParent(mainInternet).setName("News Service"));

        Category mainPartner = new Category().setName("Partner").setType(Category.Type.INCOME_EXPENSE);
        localCategoryService.insert(mainPartner);

        /** Init cashflows **/
        localCashFlowService.insert(new CashFlow(100.0f, new Date()).setCategory(mainHouse).setFromWallet(personalWallet).setComment("Cleaning products").setToWallet(walMart));
        localCashFlowService.insert(new CashFlow(150.0f, new Date()).setCategory(mainHouse).setFromWallet(personalWallet));
        localCashFlowService.insert(new CashFlow(200.0f, new Date()).setCategory(mainHouse).setFromWallet(personalWallet));

        localCashFlowService.insert(new CashFlow(150.0f, new Date()).setCategory(mainInternet).setToWallet(sock).setComment("AdSense"));
        localCashFlowService.insert(new CashFlow(50.0f, new Date()).setCategory(mainInternet).setToWallet(personalWallet));

        localCashFlowService.insert(new CashFlow(250.0f, new Date()).setFromWallet(personalWallet).setToWallet(sock));
        localCashFlowService.insert(new CashFlow(50.0f, new Date()).setFromWallet(personalWallet).setToWallet(walMart));

    }

    public void inject(Object object) {
        graph.inject(object);
    }

    protected List<Object> getModules() {
        List<Object> modules = new ArrayList<Object>();
        modules.add(new MainModule(this));
        modules.add(new DatabaseModule(this));
        return modules;
    }
}
