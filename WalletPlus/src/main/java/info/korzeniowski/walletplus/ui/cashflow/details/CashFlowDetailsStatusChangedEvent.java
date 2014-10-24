package info.korzeniowski.walletplus.ui.cashflow.details;

import android.support.v4.app.Fragment;

public class CashFlowDetailsStatusChangedEvent {
    Class<? extends Fragment> fragmentClass ;

    public CashFlowDetailsStatusChangedEvent(Class<? extends Fragment> fragmentClass) {
        this.fragmentClass = fragmentClass;
    }

    public Class<? extends Fragment> getFragmentClass() {
        return fragmentClass;
    }
}
