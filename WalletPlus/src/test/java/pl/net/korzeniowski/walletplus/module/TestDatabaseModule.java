package pl.net.korzeniowski.walletplus.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.model.Profile;
import pl.net.korzeniowski.walletplus.service.ormlite.ProfileServiceOrmLite;
import pl.net.korzeniowski.walletplus.service.ormlite.UserDatabaseHelper;
import pl.net.korzeniowski.walletplus.util.PrefUtils;

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
