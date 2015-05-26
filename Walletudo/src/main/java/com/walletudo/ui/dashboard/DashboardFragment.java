package com.walletudo.ui.dashboard;

import android.graphics.Color;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.CashFlow;
import com.walletudo.model.Wallet;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.WalletService;
import com.walletudo.ui.cashflow.list.CashFlowListItemView;
import com.walletudo.util.WalletudoUtils;

import org.joda.time.LocalDate;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.listener.BubbleChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.BubbleChartData;
import lecho.lib.hellocharts.model.BubbleValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.BubbleChartView;

public class DashboardFragment extends Fragment {
    public static final String TAG = DashboardFragment.class.getSimpleName();

    @InjectView(R.id.totalAmount)
    TextView totalAmount;

    @InjectView(R.id.chart)
    BubbleChartView chart;

    @InjectView(R.id.selectedCashFlow)
    CashFlowListItemView selectedCashFlow;

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    private List<CashFlow> cashFlowList;
    private Map<BubbleValue, CashFlow> bubbleMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Walletudo) getActivity().getApplication()).component().inject(this);
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
        chart.setBubbleChartData(getBubbleChartData());
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
                selectedCashFlow.setCashFlow(bubbleMap.get(bubbleValue));
            }

            @Override
            public void onValueDeselected() {

            }
        });
        chart.setZoomEnabled(true);
        chart.setScrollEnabled(true);
        chart.setContainerScrollEnabled(true, ContainerScrollType.VERTICAL);
    }

    private BubbleChartData getBubbleChartData() {
        List<BubbleValue> bubbleValues = Lists.newArrayList();
        List<AxisValue> dateAxisValues = Lists.newArrayList();
        bubbleMap = Maps.newHashMap();
        LocalDate today = new LocalDate();
        LocalDate firstDay = today.plusWeeks(-1).plusDays(1);
        cashFlowList = cashFlowService.findCashFlows(
                new CashFlowService.CashFlowQuery()
                        .withFromDate(firstDay.toDate())
                        .withToDate(today.toDate()));

        while (!firstDay.equals(today)) {
            dateAxisValues.add(getAxisValue(firstDay.toDate()));
            firstDay = firstDay.plusDays(1);
        }
        dateAxisValues.add(getAxisValue(today.toDate()));

        for (CashFlow cashFlow : cashFlowList) {
            Date roundedDate = new LocalDate(cashFlow.getDateTime()).toDate();
            BubbleValue bubbleValue = getBubbleValue(roundedDate, cashFlow);
            bubbleMap.put(bubbleValue, cashFlow);
            bubbleValues.add(bubbleValue);
        }

        BubbleChartData bubbleChartData = new BubbleChartData(bubbleValues);
        // setup axis with dates.
        bubbleChartData.setAxisXBottom(new Axis(dateAxisValues));

        bubbleChartData.setAxisYRight(new Axis(Lists.newArrayList(new AxisValue(0.0F).setLabel(""))).setHasSeparationLine(false).setHasLines(true).setLineColor(getResources().getColor(R.color.red)));
        // setup axis with values.

        List<AxisValue> helperLinesAxisValueList = Lists.newArrayList();
        for (Integer value : WalletudoUtils.Charts.getHelperLineValues(cashFlowList)) {
            helperLinesAxisValueList.add(new AxisValue(value));
        }
        Axis axisWithLines = new Axis(helperLinesAxisValueList);
        axisWithLines.setHasLines(true);
        bubbleChartData.setAxisYLeft(axisWithLines);

        return bubbleChartData;

    }

    private BubbleValue getBubbleValue(Date roundedDate, CashFlow cashFlow) {
        int color = 0;
        if (cashFlow.getType().equals(CashFlow.Type.EXPENSE)) {
            color = getResources().getColor(R.color.red);
        } else if (cashFlow.getType().equals(CashFlow.Type.INCOME)) {
            color = getResources().getColor(R.color.green);
        }
        color = Color.argb(130, Color.red(color), Color.green(color), Color.blue(color));

        return new BubbleValue(
                (float) roundedDate.getTime(),
                cashFlow.getRelativeAmount().floatValue(),
                cashFlow.getAmount().floatValue(),
                color);
    }


    private AxisValue getAxisValue(Date date) {
        AxisValue axisValue = new AxisValue(date.getTime());
        axisValue.setLabel(new SimpleDateFormat("dd.MM", Locale.getDefault()).format(date));
//        axisValue.setLabel(android.text.format.DateFormat.getDateFormat(getActivity()).format(date));
//        axisValue.setLabel(WalletudoUtils.Dates.getShortDateLabel(getActivity(), date));
        return axisValue;
    }
}
