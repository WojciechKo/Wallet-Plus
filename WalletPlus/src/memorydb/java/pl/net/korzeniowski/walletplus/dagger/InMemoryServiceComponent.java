package pl.net.korzeniowski.walletplus.dagger;

import javax.inject.Singleton;

import dagger.Component;
import pl.net.korzeniowski.walletplus.dagger.module.InMemoryServicesModule;
import pl.net.korzeniowski.walletplus.dagger.test.ServiceInjectedUnitTest;

@Singleton
@Component(
        modules = {
                InMemoryServicesModule.class}
)
public interface InMemoryServiceComponent {

    void inject(ServiceInjectedUnitTest serviceInjectedUnitTest);

    final class Initializer {

        public static InMemoryServiceComponent init() {
            return DaggerInMemoryServiceComponent.create();
        }
    }
}