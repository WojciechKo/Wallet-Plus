package info.korzeniowski.walletplus.module;

import android.content.Context;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ormlite.MainDatabaseHelper;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.util.PrefUtils;
import info.korzeniowski.walletplus.util.Utils;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    public MainDatabaseHelper provideMainDatabaseHelper(Context context) {
        return new MainDatabaseHelper(context);
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideProfileDatabaseHelper(Context context, ProfileServiceOrmLite profileService) {
        Profile profile = profileService.getActiveProfile();

        if (profile != null) {
            return new UserDatabaseHelper(context, Utils.getProfileDatabaseName(profile.getName()));
        }

        throw new RuntimeException("No profile exists! Should be handled.");
    }
}
