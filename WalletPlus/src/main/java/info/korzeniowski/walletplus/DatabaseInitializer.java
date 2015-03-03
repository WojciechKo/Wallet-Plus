package info.korzeniowski.walletplus;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.service.exception.DatabaseException;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.util.PrefUtils;

public class DatabaseInitializer {

    @Inject
    @Named(CashFlowService.ORMLITE_IMPL)
    CashFlowService cashFlowService;

    @Inject
    @Named(WalletService.ORMLITE_IMPL)
    WalletService walletService;

    @Inject
    @Named(TagService.ORMLITE_IMPL)
    TagService tagService;

    private final WeakReference<WalletPlus> walletPlus;

    public DatabaseInitializer(WalletPlus walletPlus) {
        this.walletPlus = new WeakReference<>(walletPlus);
    }

    public void createExampleAccountWithProfile() {
        try {
            AccountServiceOrmLite accountServiceOrmLite = walletPlus.get().getGraph().get(AccountServiceOrmLite.class);
            Account exampleAccount = new Account().setName("Example Account");
            accountServiceOrmLite.insert(exampleAccount);
            ProfileServiceOrmLite profileServiceOrmLite = walletPlus.get().getGraph().get(ProfileServiceOrmLite.class);
            Profile exampleProfile = new Profile().setName("Personal example").setAccount(exampleAccount);
            profileServiceOrmLite.insert(exampleProfile);
            PrefUtils.setActiveProfileId(walletPlus.get().getBaseContext(), exampleProfile.getId());
            walletPlus.get().inject(this);
            fillExampleDatabase();

            Profile bestCompany = new Profile().setName("Best company").setAccount(exampleAccount);
            profileServiceOrmLite.insert(bestCompany);
            PrefUtils.setActiveProfileId(walletPlus.get().getBaseContext(), bestCompany.getId());
            walletPlus.get().reinitializeObjectGraph();
            walletPlus.get().inject(this);
            fillExampleDatabase();

            Profile oldCompany = new Profile().setName("Old company").setAccount(exampleAccount);
            profileServiceOrmLite.insert(oldCompany);
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
        walletService.insert(personalWallet);
        Wallet wardrobe = new Wallet().setName("Wardrobe").setInitialAmount(1500.0).setCurrentAmount(100.0);
        walletService.insert(wardrobe);
        Wallet sock = new Wallet().setName("Sock").setInitialAmount(500.0).setCurrentAmount(100.0);
        walletService.insert(sock);
        Wallet bankAccount = new Wallet().setName("Bank account").setInitialAmount(2500.0).setCurrentAmount(100.0);
        walletService.insert(bankAccount);

        /** Init categories **/
        Tag mainHouse = new Tag().setName("House");
        tagService.insert(mainHouse);
        Tag energy = new Tag().setName("Energy");
        tagService.insert(energy);
        Tag water = new Tag().setName("Water");
        tagService.insert(water);
        Tag gas = new Tag().setName("Gas");
        tagService.insert(gas);

        Tag mainInternet = new Tag().setName("Internet");
        tagService.insert(mainInternet);
        tagService.insert(new Tag().setName("Music-forum"));
        tagService.insert(new Tag().setName("News-service"));

        Tag mainPartner = new Tag().setName("Partner");
        tagService.insert(mainPartner);

        /** Init cashflows **/
        Calendar date = Calendar.getInstance();

        cashFlowService.insert(new CashFlow().setAmount(100.0).setType(CashFlow.Type.EXPANSE).addTag(mainHouse).setWallet(personalWallet).setDateTime(date.getTime()).setComment("Food"));

        date.add(Calendar.DATE, -1);
        cashFlowService.insert(new CashFlow().setAmount(150.0).setType(CashFlow.Type.EXPANSE).addTag(mainHouse).setWallet(personalWallet).setDateTime(date.getTime()).setComment("Cleaning products"));

        date.add(Calendar.HOUR_OF_DAY, -1);

        date.add(Calendar.HOUR_OF_DAY, -1);
        cashFlowService.insert(new CashFlow().setAmount(75.0).setType(CashFlow.Type.EXPANSE).addTag(energy).setWallet(bankAccount).setDateTime(date.getTime()));
        cashFlowService.insert(new CashFlow().setAmount(100.0).setType(CashFlow.Type.EXPANSE).addTag(water).setWallet(bankAccount).setDateTime(date.getTime()));
        cashFlowService.insert(new CashFlow().setAmount(50.0).setType(CashFlow.Type.EXPANSE).addTag(gas).setWallet(bankAccount).setDateTime(date.getTime()));

        date.add(Calendar.DATE, -1);
        cashFlowService.insert(new CashFlow().setAmount(500.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setWallet(personalWallet).setDateTime(date.getTime()));
        cashFlowService.insert(new CashFlow().setAmount(1000.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setWallet(wardrobe).setComment("Investition").setDateTime(date.getTime()));
        cashFlowService.insert(new CashFlow().setAmount(3000.0).setType(CashFlow.Type.INCOME).setWallet(bankAccount).setComment("Payment").setDateTime(date.getTime()));
    }
}
