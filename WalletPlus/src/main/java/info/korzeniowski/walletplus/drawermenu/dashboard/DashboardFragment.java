package info.korzeniowski.walletplus.drawermenu.dashboard;

import android.support.v4.app.Fragment;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.DecimalFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.model.Wallet;

@EFragment(R.layout.dashboard_fragment)
public class DashboardFragment extends Fragment {

    @ViewById
    TextView sum;

    @Inject
    @Named("local")
    WalletDataManager localWalletDataManager;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        Double currentAmountSumFromMyWallets = getCurrentAmountSumFromMyWallets();
        sum.setText(new DecimalFormat(",####.00").format(currentAmountSumFromMyWallets));
    }

    private Double getCurrentAmountSumFromMyWallets() {
        List<Wallet> myWallets = localWalletDataManager.getMyWallets();
        Double sum = (double) 0;
        for (Wallet wallet : myWallets) {
            sum += wallet.getCurrentAmount();
        }
        return sum;


    }

}
