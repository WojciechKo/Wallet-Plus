package info.korzeniowski.walletplus;

import java.util.List;

import dagger.ObjectGraph;
import info.korzeniowski.walletplus.model.Account;
import info.korzeniowski.walletplus.test.module.TestDatabaseModule;

public class TestWalletPlus extends WalletPlus {

    private List<Object> modules;

    @Override
    List<Object> getModules() {
        if (modules == null) {
            modules = super.getModules();
            modules.add(new TestDatabaseModule());
        }
        return modules;
    }

    @Override
    public void initApplication() {
    }

    public void addModules(Object module) {
        getModules().add(module);
        graph = ObjectGraph.create(getModules().toArray());
    }

    public void removeModule(Object module) {
        getModules().remove(module);
        graph = ObjectGraph.create(getModules().toArray());
    }

    public void removeModule(Class<?> moduleClass) {
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
    public Account getCurrentProfile() {
        return new Account().setName("Test Account").setDatabaseFileName("Test Account.db");
    }
}
