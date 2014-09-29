package info.korzeniowski.walletplus;

import java.util.List;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;

public class TestWalletPlus extends WalletPlus {

    @Override
    public void onCreate() {
        graph = ObjectGraph.create(getModules().toArray());
    }

    @Override
    protected List<Object> getModules() {
        List<Object> modules = super.getModules();
        modules.add(new TestDatabaseModule());
        return modules;
    }
}
