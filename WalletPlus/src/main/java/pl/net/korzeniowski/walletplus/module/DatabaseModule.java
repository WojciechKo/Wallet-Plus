package pl.net.korzeniowski.walletplus.module;

import android.content.Context;

import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.model.Profile;
import pl.net.korzeniowski.walletplus.service.ormlite.MainDatabaseHelper;
import pl.net.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import pl.net.korzeniowski.walletplus.util.PrefUtils;
import pl.net.korzeniowski.walletplus.util.Utils;

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
