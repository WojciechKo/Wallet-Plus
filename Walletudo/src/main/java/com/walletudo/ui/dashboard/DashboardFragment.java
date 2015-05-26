package com.walletudo.ui.dashboard;

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

import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.Wallet;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.WalletService;
import com.walletudo.ui.cashflow.list.CashFlowListItemView;

import org.joda.time.LocalDate;

import java.text.NumberFormat;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.BubbleChartOnValueSelectListener;
import lecho.lib.hellocharts.model.BubbleValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.BubbleChartView;

public class DashboardFragment extends Fragment {
    public static final String TAG = DashboardFragment.class.getSimpleName();

    @InjectView(R.id.totalAmount)
    TextView totalAmount;

    @InjectView(R.id.chartLabel)
    TextView chartLabel;

    @InjectView(R.id.chart)
    BubbleChartView chart;

    @InjectView(R.id.selectedCashFlow)
    CashFlowListItemView selectedCashFlow;

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    private BubbleChartDataProvider bubbleChartDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Walletudo) getActivity().getApplication()).component().inject(this);
        bubbleChartDataProvider = new BubbleChartDataProvider(getActivity(), cashFlowService);
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
        chartLabel.setText(getString(R.string.dashboardChartLabel, bubbleChartDataProvider.getColumnsNumber()));
        setupChartOfLastNCashFlows();
    }

    private CharSequence getTotalAmountText() {
        Double sumOfCurrentAmountOfWallets = getSumOfCurrentAmountOfWallets();
        String totalAmountString = NumberFormat.getCurrencyInstance().format(sumOfCurrentAmountOfWallets);

        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getString(R.string.dashboardTotalAmountLabel) + "\n");
        spanTxt.append(totalAmountString);
        spanTxt.setSpan(new RelativeSizeSpan(2f), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        if (sumOfCurrentAmountOfWallets < 0) {
            spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        } else {
            spanTxt.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.green)), spanTxt.length() - totalAmountString.length(), spanTxt.length(), 0);
        }
        return spanTxt;
    }

    private Double getSumOfCurrentAmountOfWallets() {
        List<Wallet> wallets = walletService.getAll();
        Double sum = (double) 0;
        for (Wallet wallet : wallets) {
            sum += wallet.getCurrentAmount();
        }
        return sum;
    }

    private void setupChartOfLastNCashFlows() {
        LocalDate today = new LocalDate();
        chart.setBubbleChartData(bubbleChartDataProvider.getBubbleChartData(today.plusDays(-bubbleChartDataProvider.getColumnsNumber() + 1), today));
        chart.setViewportCalculationEnabled(false);
        final Viewport v = new Viewport(chart.getMaximumViewport());
        float diff = v.top - v.bottom;
        v.bottom -= 0.2 * diff;
        v.top += 0.2 * diff;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
        chart.setValueSelectionEnabled(true);
        chart.setOnValueTouchListener(new BubbleChartOnValueSelectListener() {
            @Override
            public void onValueSelected(int i, BubbleValue bubbleValue) {
                selectedCashFlow.setCashFlow(bubbleChartDataProvider.getCashFlow(bubbleValue));
            }

            @Override
            public void onValueDeselected() {

            }
        });
        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
    }
}
