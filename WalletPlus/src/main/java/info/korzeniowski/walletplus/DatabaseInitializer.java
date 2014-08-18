package info.korzeniowski.walletplus;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.DatabaseHelper;

public class DatabaseInitializer {

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @Inject @Named("local")
    WalletService localWalletService;

    @Inject @Named("local")
    CategoryService localCategoryService;

    @Inject
    DatabaseHelper databaseHelper;

    private WalletPlus walletPlus;

    DatabaseInitializer(WalletPlus walletPlus) {
        this.walletPlus = walletPlus;
        walletPlus.inject(this);
    }

    public void initDatabaseAfterInstallation() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(walletPlus);
        boolean isFirstRun = sharedPreferences.getBoolean("firstRun", true);
        if (isFirstRun) {
            clearDatabase();
            initDatabase();
            initExampleDatabase();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstRun", false);
            editor.commit();
        }
    }

    private void clearDatabase() {
        try {
            TableUtils.clearTable(databaseHelper.getConnectionSource(), Account.class);
            TableUtils.clearTable(databaseHelper.getConnectionSource(), CashFlow.class);
            TableUtils.clearTable(databaseHelper.getConnectionSource(), Category.class);
            TableUtils.clearTable(databaseHelper.getConnectionSource(), Wallet.class);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    private void initDatabase() {
        localCategoryService.insert(new Category.Builder().setType(Category.Type.OTHER).setName(walletPlus.getString(R.string.otherCategoryName)).build());
        localCategoryService.insert(new Category.Builder().setType(Category.Type.TRANSFER).setName(walletPlus.getString(R.string.transferCategoryName)).build());
    }

    private void initExampleDatabase() {
        /** Init my wallets **/
        Wallet.Builder walletBuilder = new Wallet.Builder().setType(Wallet.Type.MY_WALLET);
        Wallet personalWallet = walletBuilder.setName("Personal wallet").setInitialAmount(100.0).build();
        localWalletService.insert(personalWallet);
        Wallet wardrobe = walletBuilder.setName("Wardrobe").setInitialAmount(1500.0).build();
        localWalletService.insert(wardrobe);
        Wallet sock = walletBuilder.setName("Sock").setInitialAmount(500.0).build();
        localWalletService.insert(sock);
        Wallet bankAccount = walletBuilder.setName("Bank account").setInitialAmount(2500.0).build();
        localWalletService.insert(bankAccount);

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
        Category.Builder categoryBuilder = new Category.Builder();
        Category mainHouse = categoryBuilder.setName("House").setType(Category.Type.EXPENSE).build();
        localCategoryService.insert(mainHouse);
        categoryBuilder.setParent(mainHouse).setType(null);

        Category energy = categoryBuilder.setName("Energy").build();
        localCategoryService.insert(energy);
        Category water = categoryBuilder.setName("Water").build();
        localCategoryService.insert(water);
        Category gas = categoryBuilder.setName("Gas").build();
        localCategoryService.insert(gas);

        Category mainInternet = categoryBuilder.setName("Internet").setType(Category.Type.INCOME).setParent(null).build();
        localCategoryService.insert(mainInternet);
        categoryBuilder.setParent(mainInternet).setType(null);
        localCategoryService.insert(categoryBuilder.setName("Music forum").build());
        localCategoryService.insert(categoryBuilder.setName("News Service").build());

        Category mainPartner = categoryBuilder.setName("Partner").setType(Category.Type.INCOME_EXPENSE).setParent(null).build();
        localCategoryService.insert(mainPartner);

        /** Init cashflows **/
        Calendar date = Calendar.getInstance();

        CashFlow.Builder cashFlowBuilder = new CashFlow.Builder();
        localCashFlowService.insert(cashFlowBuilder.setAmount(100.0f).setCategory(mainHouse).setFromWallet(personalWallet).setToWallet(walMart).setDateTime(date.getTime()).setComment("Food").build());

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(cashFlowBuilder.setAmount(150.0f).setCategory(mainHouse).setFromWallet(personalWallet).setToWallet(walMart).setDateTime(date.getTime()).setComment("Cleaning products").build());

        date.add(Calendar.HOUR_OF_DAY, -1);
        localCashFlowService.insert(cashFlowBuilder.setAmount(100.0f).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(sock).setToWallet(personalWallet).setDateTime(date.getTime()).setComment("Transfer to personal wallet").build());

        date.add(Calendar.HOUR_OF_DAY, -1);
        localCashFlowService.insert(cashFlowBuilder.setAmount(75.0f).setCategory(energy).setFromWallet(bankAccount).setToWallet(null).setComment(null).build());
        localCashFlowService.insert(cashFlowBuilder.setAmount(100.0f).setCategory(water).setFromWallet(bankAccount).setToWallet(null).setComment(null).build());
        localCashFlowService.insert(cashFlowBuilder.setAmount(50.0f).setCategory(gas).setFromWallet(bankAccount).setToWallet(null).setComment(null).build());

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(cashFlowBuilder.setAmount(500.0f).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(bankAccount).setToWallet(personalWallet).setComment(null).build());
        localCashFlowService.insert(cashFlowBuilder.setAmount(1000.0f).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(bankAccount).setToWallet(wardrobe).setComment("Savings").build());
        localCashFlowService.insert(cashFlowBuilder.setAmount(3000.0f).setCategory(localCashFlowService.getOtherCategory()).setFromWallet(amazon).setToWallet(bankAccount).setComment("Payment").build());
    }

}
