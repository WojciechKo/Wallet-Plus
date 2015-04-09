package pl.net.korzeniowski.walletplus.dagger;

import javax.inject.Singleton;

import dagger.Component;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.dagger.module.*;

@Singleton
@Component(
        modules = {
                MainModule.class,
                DatabaseModule.class,
                ServicesModule.class,
                GoogleDriveRestModule.class}
)
public interface AppComponent extends IAppComponent {

    final class Initializer {
        public static AppComponent init(boolean mockMode) {
            return DaggerAppComponent
                    .builder()
                    .mainModule(new MainModule(WalletPlus.getInstance()))
                    .build();
        }
    }
}