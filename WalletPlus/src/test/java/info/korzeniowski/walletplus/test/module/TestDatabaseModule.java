package info.korzeniowski.walletplus.test.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.module.DatabaseModule;
import info.korzeniowski.walletplus.service.local.LocalAccountService;
import info.korzeniowski.walletplus.service.local.LocalProfileService;
import info.korzeniowski.walletplus.service.local.UserDatabaseHelper;
import info.korzeniowski.walletplus.test.service.cashflow.LocalCashFlowServiceTest;
import info.korzeniowski.walletplus.test.service.tag.TagStatisticsTest;
import info.korzeniowski.walletplus.test.service.tag.LocalTagServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.LocalWalletServiceTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletValidatorTest;
import info.korzeniowski.walletplus.util.PrefUtils;

@Module(
        includes = DatabaseModule.class,
        injects = {
                LocalCashFlowServiceTest.class,
                TagStatisticsTest.class,
                LocalTagServiceTest.class,
                LocalWalletServiceTest.class,
                WalletValidatorTest.class
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
    public UserDatabaseHelper provideUserDatabaseHelper(LocalProfileService profileService, LocalAccountService accountService) {
        Profile profile = new Profile().setName("Test profile");
        profileService.insert(profile);
        PrefUtils.setActiveProfileId(context, profile.getId());
        return new UserDatabaseHelper(context, profile.getName());
    }
}
