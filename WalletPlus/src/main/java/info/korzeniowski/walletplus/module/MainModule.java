package info.korzeniowski.walletplus.module;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.MainActivity_;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.MainDrawerContent;
import info.korzeniowski.walletplus.drawermenu.MainDrawerListAdapter;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;

/**
 * Module for common objects.
 */
@Module(
        injects = {
                MainActivity_.class,
                MainDrawerContent.class,
                MainDrawerListAdapter.class,
                CategoryListFragment_.class
        },
        complete = false
)
public class MainModule {
    private final WalletPlus application;

    public MainModule(WalletPlus application) {
        this.application = application;
    }

    @Provides
    @Singleton
    WalletPlus provideWalletPlus() {
        return application;
    }

    @Provides
    @Singleton
    Context provideContext() {
        return application.getApplicationContext();
    }
}
