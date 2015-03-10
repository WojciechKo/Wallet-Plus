package info.korzeniowski.walletplus.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ormlite.MainDatabaseHelper;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.util.PrefUtils;

@Module
public class DatabaseModule {

    @Provides
    @Singleton
    public MainDatabaseHelper provideMainDatabaseHelper(Context context) {
        return new MainDatabaseHelper(context);
    }

    @Provides
    @Singleton
    public UserDatabaseHelper provideUserDatabaseHelper(Context context, ProfileServiceOrmLite profileService, PrefUtils prefUtils) {
        Profile profile = profileService.findById(prefUtils.getActiveProfileId());
        if (profile != null) {
            return new UserDatabaseHelper(context, profile.getName());
        }
        return new UserDatabaseHelper(context, null);
    }
}
