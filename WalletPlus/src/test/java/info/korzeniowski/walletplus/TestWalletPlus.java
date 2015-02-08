package info.korzeniowski.walletplus;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;
import info.korzeniowski.walletplus.util.PrefUtils;

public class TestWalletPlus extends WalletPlus {

    private List<Object> modules;
    private Long activeProfileId = 47L;

    @Override
    List<Object> getModules() {
        if (modules == null) {
            modules = super.getModules();
            modules.add(new TestDatabaseModule(this));
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

    public void removeModule(final Class<?> moduleClass) {
        Iterables.removeIf(modules, new Predicate<Object>() {
            @Override
            public boolean apply(Object input) {
                return input.getClass().equals(moduleClass);
            }
        });
        graph = ObjectGraph.create(getModules().toArray());
    }
}
