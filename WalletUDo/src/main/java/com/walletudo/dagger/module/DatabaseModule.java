package com.walletudo.dagger.module;

import android.content.Context;

import com.walletudo.model.Profile;
import com.walletudo.service.ormlite.MainDatabaseHelper;
import com.walletudo.service.ormlite.ProfileDatabaseHelper;
import com.walletudo.service.ormlite.ProfileServiceOrmLite;

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
    public ProfileDatabaseHelper provideProfileDatabaseHelper(Context context, ProfileServiceOrmLite profileService) {
        Profile profile = profileService.getActiveProfile();

        if (profile != null) {
            return new ProfileDatabaseHelper(context, profile.getName());
        }
        return new ProfileDatabaseHelper(context, null);
    }
}
