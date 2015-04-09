package pl.net.korzeniowski.walletplus.dagger.test;

import javax.inject.Inject;

import pl.net.korzeniowski.walletplus.dagger.InMemoryServiceComponent;
import pl.net.korzeniowski.walletplus.service.CashFlowService;
import pl.net.korzeniowski.walletplus.service.StatisticService;
import pl.net.korzeniowski.walletplus.service.TagService;
import pl.net.korzeniowski.walletplus.service.WalletService;

public class ServiceInjectedUnitTest {
    @Inject
    protected WalletService walletService;

    @Inject
    protected CashFlowService cashFlowService;

    @Inject
    protected TagService tagService;

    @Inject
    protected StatisticService statisticService;

    public void setUp() {
        InMemoryServiceComponent.Initializer.init().inject(this);
    }
}
