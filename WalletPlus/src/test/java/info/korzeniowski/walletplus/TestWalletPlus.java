package info.korzeniowski.walletplus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.module.MockDatabaseModule;
import info.korzeniowski.walletplus.module.TestDatabaseModule;
import info.korzeniowski.walletplus.util.PrefUtils;

public class TestWalletPlus extends WalletPlus {

    private List<Object> modules;
    private Long activeProfileId = 47L;

    @Override
    List<Object> getModules() {
        if (modules == null) {
            modules = super.getModules();
        }
        return modules;
    }

    @Override
    public void initExampleData() {
        PrefUtils.setActiveProfileId(this, activeProfileId);
    }

    public void addModules(Object module) {
        getModules().add(module);
        graph = ObjectGraph.create(getModules().toArray());
    }
}
