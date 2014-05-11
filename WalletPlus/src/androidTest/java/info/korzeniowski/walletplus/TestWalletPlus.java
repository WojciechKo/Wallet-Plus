package info.korzeniowski.walletplus;

import org.robolectric.Robolectric;

import java.util.List;

public class TestWalletPlus extends WalletPlus {

    @Override
    protected List<Object> getModules() {
        List<Object> modules = super.getModules();
        //modules.add(new TestModule());
        return modules;
    }

    public void injectMocks(Object object) {
        ((TestWalletPlus) Robolectric.application).inject(object);
    }
}
