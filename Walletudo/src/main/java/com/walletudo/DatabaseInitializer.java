package com.walletudo;

import android.app.Activity;

import com.walletudo.model.CashFlow;
import com.walletudo.model.Profile;
import com.walletudo.model.Tag;
import com.walletudo.model.Wallet;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.ProfileService;
import com.walletudo.service.TagService;
import com.walletudo.service.WalletService;
import com.walletudo.service.exception.DatabaseException;
import com.walletudo.util.PrefUtils;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import javax.inject.Inject;

import static com.walletudo.model.CashFlow.Type.EXPENSE;
import static com.walletudo.model.CashFlow.Type.INCOME;

public class DatabaseInitializer {

    @Inject
    CashFlowService cashFlowService;

    @Inject
    WalletService walletService;

    @Inject
    TagService tagService;

    private final WeakReference<Activity> activity;

    public DatabaseInitializer(Activity activity) {
        this.activity = new WeakReference<>(activity);
    }

    public void createExampleAccountWithProfile() {
        try {
            Walletudo application = (Walletudo) activity.get().getApplication();

            ProfileService profileService = application.component().profileService();
            PrefUtils prefUtils = application.component().prefUtils();

            Profile exampleProfile = new Profile().setName("Example profile");
            profileService.insert(exampleProfile);
            prefUtils.setActiveProfileId(exampleProfile.getId());

            application.reinitializeObjectGraph();
            application.component().inject(this);
            fillExampleDatabase();

        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    private void fillExampleDatabase() {
        /** Init tags **/
        Tag house = new Tag().setName("house");
        tagService.insert(house);
        Tag energy = new Tag().setName("energy");
        tagService.insert(energy);
        Tag water = new Tag().setName("water");
        tagService.insert(water);
        Tag gas = new Tag().setName("gas");
        tagService.insert(gas);
        Tag roomRent = new Tag().setName("room-renting");
        tagService.insert(roomRent);
        Tag food = new Tag().setName("food");
        tagService.insert(food);
        Tag alcohol = new Tag().setName("alcohol");
        tagService.insert(alcohol);

        Tag internet = new Tag().setName("internet");
        tagService.insert(internet);
        Tag musicForum = new Tag().setName("music-forum");
        tagService.insert(musicForum);
        Tag newsService = new Tag().setName("news-service");
        tagService.insert(newsService);

        Tag crafts = new Tag().setName("crafts");
        tagService.insert(crafts);
        Tag paintings = new Tag().setName("paintings");
        tagService.insert(paintings);
        Tag sculptures = new Tag().setName("Sculptures");
        tagService.insert(sculptures);
        Tag blacksmiths = new Tag().setName("blacksmiths");
        tagService.insert(blacksmiths);

        Tag love = new Tag().setName("love");
        tagService.insert(love);
        Tag gifts = new Tag().setName("gifts");
        tagService.insert(gifts);

        Tag job = new Tag().setName("job");
        tagService.insert(job);

        /** Init wallets **/
        Wallet personalWallet = new Wallet().setName("Personal wallet").setInitialAmount(100.0);
        walletService.insert(personalWallet);
        Wallet bankAccount = new Wallet().setName("Bank account").setInitialAmount(10000.0);
        walletService.insert(bankAccount);

        /** Init cashflows **/
        Calendar date = Calendar.getInstance();
        date.set(Calendar.DAY_OF_MONTH, 0);

        insertCashFlow(personalWallet, 30.0, EXPENSE, date, "Onion, flavor, oil, meat", food);
        insertCashFlow(personalWallet, 100.0, INCOME, date, "Portrait", crafts, paintings);

        date.add(Calendar.DATE, 2);
        insertCashFlow(personalWallet, 15.0, EXPENSE, date, "Dinner", food);
        insertCashFlow(personalWallet, 25.0, EXPENSE, date, "Red wine", alcohol);
        insertCashFlow(bankAccount, 150.0, EXPENSE, date, "Cleaning products", house);

        date.add(Calendar.DATE, 2);
        insertCashFlow(personalWallet, 250.0, INCOME, date, "Hauberk", crafts, blacksmiths);
        insertCashFlow(personalWallet, 50.0, EXPENSE, date, "Hauberk materials", crafts, blacksmiths);
        insertCashFlow(personalWallet, 100.0, EXPENSE, date, "Meal voucher", food);
        insertCashFlow(personalWallet, 40.0, EXPENSE, date, "Beer and vodka", alcohol);

        date.add(Calendar.DATE, 3);
        insertCashFlow(bankAccount, 1300.0, INCOME, date, "Salary", job);

        date.add(Calendar.DATE, 2);
        insertCashFlow(personalWallet, 100.0, EXPENSE, date, "Dinner", food);
        insertCashFlow(bankAccount, 200.0, INCOME, date, "Ads", internet, musicForum);
        insertCashFlow(bankAccount, 10.0, EXPENSE, date, "Servers", internet, musicForum);
        insertCashFlow(bankAccount, 10.0, EXPENSE, date, "Servers", internet, newsService);
        insertCashFlow(bankAccount, 150.0, EXPENSE, date, "Feature costs", internet, newsService);

        date.add(Calendar.DATE, 1);
        insertCashFlow(bankAccount, 25.0, INCOME, date, "Ads", internet, newsService);

        date.add(Calendar.DATE, 1);
        insertCashFlow(personalWallet, 50.0, EXPENSE, date, "Sculpture materials", crafts, sculptures);
        insertCashFlow(personalWallet, 150.0, INCOME, date, "Sculpture of Alicia", crafts, sculptures);

        date.add(Calendar.DATE, 3);
        insertCashFlow(bankAccount, 75.0, EXPENSE, date, "", house, energy);
        insertCashFlow(bankAccount, 100.0, EXPENSE, date, "", house, water);
        insertCashFlow(bankAccount, 50.0, EXPENSE, date, "", house, gas);
        insertCashFlow(bankAccount, 700.0, INCOME, date, "From Bob", roomRent);

        date.add(Calendar.DATE, 3);
        insertCashFlow(personalWallet, 100.0, EXPENSE, date, "Romantic dinner", love);
    }

    private void insertCashFlow(Wallet wallet, Double amount, CashFlow.Type type, Calendar date, String comment, Tag... tags) {
        cashFlowService.insert(new CashFlow().setWallet(wallet).setAmount(amount).setType(type).setDateTime(date.getTime()).setComment(comment).addTag(tags));
    }
}
