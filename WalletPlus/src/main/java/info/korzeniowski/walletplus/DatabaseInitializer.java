package info.korzeniowski.walletplus;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.CategoryService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.local.LocalAccountService;
import info.korzeniowski.walletplus.service.local.LocalProfileService;
import info.korzeniowski.walletplus.util.PrefUtils;

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

    public void createExampleAccountWithProfile() {
        try {
            LocalAccountService localAccountService = walletPlus.get().getGraph().get(LocalAccountService.class);
            Account exampleAccount = new Account().setName("Example Account");
            localAccountService.insert(exampleAccount);
            LocalProfileService localProfileService = walletPlus.get().getGraph().get(LocalProfileService.class);
            Profile exampleProfile = new Profile().setName("Personal example").setAccount(exampleAccount);
            localProfileService.insert(exampleProfile);
            PrefUtils.setActiveProfileId(walletPlus.get().getBaseContext(), exampleProfile.getId());
            walletPlus.get().inject(this);
            fillExampleDatabase();

            Profile bestCompany = new Profile().setName("Best company").setAccount(exampleAccount);
            localProfileService.insert(bestCompany);
            PrefUtils.setActiveProfileId(walletPlus.get().getBaseContext(), bestCompany.getId());
            walletPlus.get().reinitializeObjectGraph();
            walletPlus.get().inject(this);
            fillExampleDatabase();

            Profile oldCompany = new Profile().setName("Old company").setAccount(exampleAccount);
            localProfileService.insert(oldCompany);
            PrefUtils.setActiveProfileId(walletPlus.get().getBaseContext(), oldCompany.getId());
            walletPlus.get().reinitializeObjectGraph();
            walletPlus.get().inject(this);
            fillExampleDatabase();
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    private void fillExampleDatabase() {
        /** Init my wallets **/
        Wallet personalWallet = new Wallet().setName("Personal wallet").setInitialAmount(100.0).setCurrentAmount(100.0);
        localWalletService.insert(personalWallet);
        Wallet wardrobe = new Wallet().setName("Wardrobe").setInitialAmount(1500.0).setCurrentAmount(100.0);
        localWalletService.insert(wardrobe);
        Wallet sock = new Wallet().setName("Sock").setInitialAmount(500.0).setCurrentAmount(100.0);
        localWalletService.insert(sock);
        Wallet bankAccount = new Wallet().setName("Bank account").setInitialAmount(2500.0).setCurrentAmount(100.0);
        localWalletService.insert(bankAccount);

        /** Init categories **/
        Category mainHouse = new Category().setName("House");
        localCategoryService.insert(mainHouse);
        Category energy = new Category().setName("Energy");
        localCategoryService.insert(energy);
        Category water = new Category().setName("Water");
        localCategoryService.insert(water);
        Category gas = new Category().setName("Gas");
        localCategoryService.insert(gas);

        Category mainInternet = new Category().setName("Internet");
        localCategoryService.insert(mainInternet);
        localCategoryService.insert(new Category().setName("Music-forum"));
        localCategoryService.insert(new Category().setName("News-service"));

        Category mainPartner = new Category().setName("Partner");
        localCategoryService.insert(mainPartner);

        /** Init cashflows **/
        Calendar date = Calendar.getInstance();

        localCashFlowService.insert(new CashFlow().setAmount(100.0).setType(CashFlow.Type.EXPANSE).addCategory(mainHouse).setWallet(personalWallet).setDateTime(date.getTime()).setComment("Food"));

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(new CashFlow().setAmount(150.0).setType(CashFlow.Type.EXPANSE).addCategory(mainHouse).setWallet(personalWallet).setDateTime(date.getTime()).setComment("Cleaning products"));

        date.add(Calendar.HOUR_OF_DAY, -1);

        date.add(Calendar.HOUR_OF_DAY, -1);
        localCashFlowService.insert(new CashFlow().setAmount(75.0).setType(CashFlow.Type.EXPANSE).addCategory(energy).setWallet(bankAccount).setDateTime(date.getTime()));
        localCashFlowService.insert(new CashFlow().setAmount(100.0).setType(CashFlow.Type.EXPANSE).addCategory(water).setWallet(bankAccount).setDateTime(date.getTime()));
        localCashFlowService.insert(new CashFlow().setAmount(50.0).setType(CashFlow.Type.EXPANSE).addCategory(gas).setWallet(bankAccount).setDateTime(date.getTime()));

        date.add(Calendar.DATE, -1);
        localCashFlowService.insert(new CashFlow().setAmount(500.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setWallet(personalWallet).setDateTime(date.getTime()));
        localCashFlowService.insert(new CashFlow().setAmount(1000.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setWallet(wardrobe).setComment("Investition").setDateTime(date.getTime()));
        localCashFlowService.insert(new CashFlow().setAmount(3000.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setComment("Payment").setDateTime(date.getTime()));
    }
}
