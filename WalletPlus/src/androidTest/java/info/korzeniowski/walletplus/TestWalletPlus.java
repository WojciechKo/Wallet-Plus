package info.korzeniowski.walletplus;

import com.google.common.collect.Lists;

import java.util.List;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;

public class TestWalletPlus extends WalletPlus {

    private List<Object> modules;

    @Override
    public void onCreate() {
        graph = ObjectGraph.create(getModules().toArray());
    }

    @Override
    protected List<Object> getModules() {
        if (modules == null) {
            modules = Lists.newArrayList();
            modules = super.getModules();
            modules.add(new TestDatabaseModule());
        }
        return modules;
    }

    public void addModules(Object module) {
        getModules().add(module);
        graph = ObjectGraph.create(getModules().toArray());
    }

    public void removeModule(Object module) {
        getModules().remove(module);
        graph = ObjectGraph.create(getModules().toArray());
    }

    public void removeModule(Class<? extends Object> moduleClass) {
        List<Object> modules = getModules();
        for (Object module : modules) {
            if (module.getClass().equals(moduleClass)) {
                modules.remove(module);
                break;
            }
        }

        graph = ObjectGraph.create(getModules().toArray());
    }

    @Override
    public Account getCurrentAccount() {
        return new Account().setName("Test Account").setDatabaseFileName("Test Account.db");
    }
}
