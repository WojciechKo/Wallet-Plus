package com.walletudo;

import com.walletudo.dagger.module.InMemoryServicesModule;
import com.walletudo.test.ServiceInjectedUnitTest;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                InMemoryServicesModule.class}
)
public interface InMemoryServiceComponent {

    void inject(ServiceInjectedUnitTest serviceInjectedUnitTest);

    class Initializer {

        public static InMemoryServiceComponent init() {
            return DaggerInMemoryServiceComponent.create();
        }
    }
}