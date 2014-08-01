package info.korzeniowski.walletplus.drawermenu.dashboard;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.TextAppearanceSpan;
import android.widget.TextView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.text.DecimalFormat;
import java.text.NumberFormat;
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
    TextView totalAmount;

    @Inject
    @Named("local")
    WalletDataManager localWalletDataManager;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        totalAmount.setText(getTotalAmountText());
    }

    private CharSequence getTotalAmountText() {
        Double currentAmountSumFromMyWallets = getCurrentAmountSumFromMyWallets();
        String totalAmountString = NumberFormat.getCurrencyInstance().format(currentAmountSumFromMyWallets);

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getString(R.string.totalAmountLabel) + "\n");
        spanTxt.append(totalAmountString);
        spanTxt.setSpan(new RelativeSizeSpan(2f), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        if (currentAmountSumFromMyWallets < 0) {
            spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        } else {
            spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        }
        return spanTxt;
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
