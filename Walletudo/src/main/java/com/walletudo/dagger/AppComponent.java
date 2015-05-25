package com.walletudo.dagger;

import com.walletudo.Walletudo;
import com.walletudo.dagger.module.DatabaseModule;
import com.walletudo.dagger.module.GoogleDriveRestModule;
import com.walletudo.dagger.module.MainModule;
import com.walletudo.dagger.module.NavigationDrawerMenuModule;
import com.walletudo.dagger.module.ServicesModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                MainModule.class,
                NavigationDrawerMenuModule.class,
                DatabaseModule.class,
                ServicesModule.class,
                GoogleDriveRestModule.class}
)
public interface AppComponent extends IAppComponent {

    final class Initializer {
        public static AppComponent init(boolean mockMode) {
            return DaggerAppComponent
                    .builder()
                    .mainModule(new MainModule(Walletudo.getInstance()))
                    .build();
        }
    }
}