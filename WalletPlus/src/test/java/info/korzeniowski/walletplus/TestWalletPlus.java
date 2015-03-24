package info.korzeniowski.walletplus;

import net.danlew.android.joda.JodaTimeAndroid;

import javax.inject.Singleton;

import dagger.Component;
import info.korzeniowski.walletplus.module.GoogleDriveRestModule;
import info.korzeniowski.walletplus.module.MainModule;
import info.korzeniowski.walletplus.module.MockServicesModule;
import info.korzeniowski.walletplus.module.ServicesModule;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.test.service.cashflow.CashFlowServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.statistic.StatisticServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.tag.TagServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.service.wallet.WalletServiceOrmLiteTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.CreateNewMyWalletTest;
import info.korzeniowski.walletplus.test.ui.wallet.details.EditMyWalletTest;
import info.korzeniowski.walletplus.test.ui.wallet.list.MyWalletListTest;

public class TestWalletPlus extends WalletPlus {

    @Singleton
    @Component(
            modules = {
                    MainModule.class,
                    TestDatabaseModule.class,
                    ServicesModule.class,
                    GoogleDriveRestModule.class}
    )
    public interface DatabaseComponent extends TestComponent {

    }

    @Singleton
    @Component(
            modules = {
                    MainModule.class,
                    MockServicesModule.class,
                    GoogleDriveRestModule.class}
    )
    public interface MockComponent extends TestComponent {

    }

    public interface TestComponent extends ApplicationComponent {
        void inject(CashFlowServiceOrmLiteTest object);

        void inject(StatisticServiceOrmLiteTest object);

        void inject(TagServiceOrmLiteTest object);

        void inject(WalletServiceOrmLiteTest object);

        void inject(CreateNewMyWalletTest object);

        void inject(EditMyWalletTest object);

        void inject(MyWalletListTest object);
    }

    @Override
    public void onCreate() {
        setDatabaseComponent();
        component().inject(this);
        JodaTimeAndroid.init(this);
        initExampleData();
    }

    @Override
    public void initExampleData() {
        prefUtils.setActiveProfileId(47L);
    }

    public void setDatabaseComponent() {
        component = Dagger_TestWalletPlus_DatabaseComponent.builder()
                .mainModule(new MainModule(this))
                .build();
    }

    public void setMockComponent() {
        component = Dagger_TestWalletPlus_MockComponent.builder()
                .mainModule(new MainModule(this))
                .build();
    }

    public TestComponent component() {
        if (component == null) {
            throw new RuntimeException("You need to set Database type.");
        }
        return (TestComponent) component;
    }
}
