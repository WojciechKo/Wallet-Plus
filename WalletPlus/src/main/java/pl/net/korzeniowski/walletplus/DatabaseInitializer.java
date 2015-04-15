package pl.net.korzeniowski.walletplus;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.model.CashFlow;
import pl.net.korzeniowski.walletplus.model.Profile;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.ProfileService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.WalletService;
import pl.net.korzeniowski.walletplus.service.exception.DatabaseException;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

public class DatabaseInitializer {

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    TagService tagService;

    @Inject
    PrefUtils prefUtils;

    private final WeakReference<WalletPlus> walletPlus;

    public DatabaseInitializer(WalletPlus walletPlus) {
        this.walletPlus = new WeakReference<>(walletPlus);
    }

    public void createExampleAccountWithProfile() {
        try {
            ProfileService profileService = walletPlus.get().component().profileService();

            Profile exampleProfile = new Profile().setName("Personal");
            profileService.insert(exampleProfile);

            walletPlus.get().reinitializeObjectGraph();
            walletPlus.get().component().inject(this);
            fillExampleDatabase();

            Profile myCompany = new Profile().setName("My startup");
            profileService.insert(myCompany);
            prefUtils.setActiveProfileId(myCompany.getId());

            walletPlus.get().reinitializeObjectGraph();
            walletPlus.get().component().inject(this);
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
