package com.walletudo.dagger.module;

import android.content.Context;

import com.walletudo.model.Profile;
import com.walletudo.service.ormlite.MainDatabaseHelper;
import com.walletudo.service.ormlite.ProfileServiceOrmLite;
import com.walletudo.service.ormlite.UserDatabaseHelper;
import com.walletudo.util.Utils;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

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
        return new UserDatabaseHelper(context, null);
    }
}
