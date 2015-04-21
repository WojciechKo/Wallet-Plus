package com.walletudo.dagger;

import javax.inject.Singleton;

import dagger.Component;
import com.walletudo.dagger.module.InMemoryServicesModule;
import com.walletudo.dagger.test.ServiceInjectedUnitTest;

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