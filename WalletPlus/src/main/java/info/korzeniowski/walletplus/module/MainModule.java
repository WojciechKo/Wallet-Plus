package info.korzeniowski.walletplus.module;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.MainActivity_;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.drawermenu.MainDrawerContent;
import info.korzeniowski.walletplus.drawermenu.DrawerListAdapter;
import info.korzeniowski.walletplus.drawermenu.category.CategoryListFragment_;

/**
 * Module for common objects.
 */
@Module(
        injects = {
                MainActivity_.class,
                MainDrawerContent.class,
                DrawerListAdapter.class,
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
    Context provideContext() {
        return application.getApplicationContext();
    }
}
