package info.korzeniowski.walletplus.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.NumberFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;

public class DashboardFragment extends Fragment {
    public static final String TAG = "dashboard";

    @InjectView(R.id.totalAmount)
    TextView totalAmount;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_dashboard, null);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

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
        List<Wallet> myWallets = localWalletService.getMyWallets();
        Double sum = (double) 0;
        for (Wallet wallet : myWallets) {
            sum += wallet.getCurrentAmount();
        }
        return sum;
    }
}
