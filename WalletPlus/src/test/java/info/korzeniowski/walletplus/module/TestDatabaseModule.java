package info.korzeniowski.walletplus.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ormlite.AccountServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.CashFlowServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.statistic.StatisticServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.tag.TagServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletServiceOrmLiteTest;
import info.korzeniowski.walletplus.util.PrefUtils;

@Module(
        includes = DatabaseModule.class,
        injects = {
                CashFlowServiceOrmLiteTest.class,
                StatisticServiceOrmLiteTest.class,
                TagServiceOrmLiteTest.class,
                WalletServiceOrmLiteTest.class
        },
        overrides = true,
        complete = false
)
public class TestDatabaseModule {
    private Context context;

    public TestDatabaseModule(Context context) {
        this.context = context;
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideUserDatabaseHelper(ProfileServiceOrmLite profileService, AccountServiceOrmLite accountService) {
        Profile profile = new Profile().setName("Test profile");
        profileService.insert(profile);
        PrefUtils.setActiveProfileId(profile.getId());
        return new UserDatabaseHelper(context, profile.getName());
    }
}
