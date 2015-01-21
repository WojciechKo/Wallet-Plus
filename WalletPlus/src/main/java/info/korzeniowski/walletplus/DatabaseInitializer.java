package info.korzeniowski.walletplus;

import java.lang.ref.WeakReference;
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
import info.korzeniowski.walletplus.service.local.LocalAccountService;

public class DatabaseInitializer {

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CategoryService localCategoryService;

    private final WeakReference<WalletPlus> walletPlus;

    public DatabaseInitializer(WalletPlus walletPlus) {
        this.walletPlus = new WeakReference<>(walletPlus);
    }

    public Account createExampleAccount() {
        LocalAccountService localAccountService1 = walletPlus.get().getGraph().get(LocalAccountService.class);
        Account result = new Account().setName("Example Account");
        localAccountService1.insert(result);
        walletPlus.get().setCurrentAccount(result);
        walletPlus.get().inject(this);
        fillExampleDatabase();
        return result;
    }

    private void fillExampleDatabase() {
        /** Init my wallets **/
        Wallet personalWallet = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Personal wallet").setInitialAmount(100.0).setCurrentAmount(100.0);
        localWalletService.insert(personalWallet);
        Wallet wardrobe = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Wardrobe").setInitialAmount(1500.0).setCurrentAmount(100.0);
        localWalletService.insert(wardrobe);
        Wallet sock = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Sock").setInitialAmount(500.0).setCurrentAmount(100.0);
        localWalletService.insert(sock);
        Wallet bankAccount = new Wallet().setType(Wallet.Type.MY_WALLET).setName("Bank account").setInitialAmount(2500.0).setCurrentAmount(100.0);
        localWalletService.insert(bankAccount);

        /** Init other wallets **/
        Wallet sevenEleven = new Wallet().setType(Wallet.Type.OTHER).setName("7-Eleven").setInitialAmount(0.0).setCurrentAmount(100.0);
        localWalletService.insert(sevenEleven);
        Wallet tesco = new Wallet().setType(Wallet.Type.OTHER).setName("Tesco").setInitialAmount(0.0).setCurrentAmount(100.0);
        localWalletService.insert(tesco);
        Wallet walMart = new Wallet().setType(Wallet.Type.OTHER).setName("Wal-Mart").setInitialAmount(0.0).setCurrentAmount(100.0);
        localWalletService.insert(walMart);
        Wallet amazon = new Wallet().setType(Wallet.Type.OTHER).setName("Amazon").setInitialAmount(0.0).setCurrentAmount(100.0);
        localWalletService.insert(amazon);

        /** Init categories **/
        Category mainHouse = new Category().setName("House");
        localCategoryService.insert(mainHouse);
        Category energy = new Category().setName("Energy").setParent(mainHouse);
        localCategoryService.insert(energy);
        Category water = new Category().setName("Water").setParent(mainHouse);
        localCategoryService.insert(water);
        Category gas = new Category().setName("Gas").setParent(mainHouse);
        localCategoryService.insert(gas);

        Category mainInternet = new Category().setName("Internet");
        localCategoryService.insert(mainInternet);
        localCategoryService.insert(new Category().setParent(mainInternet).setName("Music forum"));
        localCategoryService.insert(new Category().setParent(mainInternet).setName("News Service"));

        Category mainPartner = new Category().setName("Partner");
        localCategoryService.insert(mainPartner);

        /** Init cashflows **/
        Calendar date = Calendar.getInstance();

        CashFlow cashFlow = new CashFlow();
        localCashFlowService.insert(cashFlow.setAmount(100.0).setCategory(mainHouse).setFromWallet(personalWallet).setToWallet(walMart).setDateTime(date.getTime()).setComment("Food"));

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(cashFlow.setAmount(150.0).setCategory(mainHouse).setFromWallet(personalWallet).setToWallet(walMart).setDateTime(date.getTime()).setComment("Cleaning products"));

        date.add(Calendar.HOUR_OF_DAY, -1);
        localCashFlowService.insert(cashFlow.setAmount(100.0).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(sock).setToWallet(personalWallet).setDateTime(date.getTime()).setComment("Transfer to personal wallet"));

        date.add(Calendar.HOUR_OF_DAY, -1);
        localCashFlowService.insert(cashFlow.setAmount(75.0).setCategory(energy).setFromWallet(bankAccount).setToWallet(null).setComment(null));
        localCashFlowService.insert(cashFlow.setAmount(100.0).setCategory(water).setFromWallet(bankAccount).setToWallet(null).setComment(null));
        localCashFlowService.insert(cashFlow.setAmount(50.0).setCategory(gas).setFromWallet(bankAccount).setToWallet(null).setComment(null));

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(cashFlow.setAmount(500.0).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(bankAccount).setToWallet(personalWallet).setComment(null));
        localCashFlowService.insert(cashFlow.setAmount(1000.0).setCategory(localCashFlowService.getTransferCategory()).setFromWallet(bankAccount).setToWallet(wardrobe).setComment("Savings"));
        localCashFlowService.insert(cashFlow.setAmount(3000.0).setCategory(null).setFromWallet(amazon).setToWallet(bankAccount).setComment("Payment"));
    }

}
