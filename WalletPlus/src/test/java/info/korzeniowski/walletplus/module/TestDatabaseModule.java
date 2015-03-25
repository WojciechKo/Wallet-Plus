package info.korzeniowski.walletplus.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import info.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import info.korzeniowski.walletplus.util.PrefUtils;

@Module
public class TestDatabaseModule {

    @Provides
    @Singleton
    public UserDatabaseHelper provideUserDatabaseHelper(Context context, ProfileServiceOrmLite profileService, PrefUtils prefUtils) {
        Profile profile = new Profile().setName("Test profile");
        profileService.insert(profile);
        prefUtils.setActiveProfileId(profile.getId());
        return new UserDatabaseHelper(context, profile.getName());
    }
}
