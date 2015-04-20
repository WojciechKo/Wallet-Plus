package com.walletudo.dagger;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import com.walletudo.model.Profile;
import com.walletudo.service.ormlite.ProfileServiceOrmLite;
import com.walletudo.service.ormlite.UserDatabaseHelper;
import com.walletudo.util.PrefUtils;

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
