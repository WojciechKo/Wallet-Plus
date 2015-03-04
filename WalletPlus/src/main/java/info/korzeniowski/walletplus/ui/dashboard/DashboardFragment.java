package info.korzeniowski.walletplus.ui.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.collect.Lists;

import java.text.NumberFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

public class DashboardFragment extends Fragment {
    public static final String TAG = "dashboard";
    private static final int MAX_NUMBER_OF_POINTS_IN_CHART = 5;

    @InjectView(R.id.totalAmount)
    TextView totalAmount;

    @InjectView(R.id.chart)
    LineChartView chart;

    @Inject
    WalletService walletService;

    @Inject
    CashFlowService cashFlowService;

    private Double sumOfCurrentAmountOfWallets;

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

        chart.setLineChartData(getMainLineChartData());
        chart.setZoomEnabled(false);
        chart.setValueSelectionEnabled(true);
        chart.setViewportCalculationEnabled(false);
        final Viewport v = new Viewport(chart.getMaximumViewport());
        float diff = v.top - v.bottom;
        v.bottom -= 0.2 * diff;
        v.top += 0.2 * diff;
        chart.setMaximumViewport(v);
        chart.setCurrentViewportWithAnimation(v);
    }

    private LineChartData getMainLineChartData() {
        List<PointValue> values = Lists.newArrayList();
        List<AxisValue> dateAxisValues = Lists.newArrayList();
        List<CashFlow> cashFlowList = cashFlowService.getLastNCashFlows(MAX_NUMBER_OF_POINTS_IN_CHART);
        ListIterator<CashFlow> cashFlowListIterator = cashFlowList.listIterator();

        for (int i = 0 ; i < cashFlowList.size(); i++) {
            if (!cashFlowListIterator.hasNext()) {
                break;
            }
            CashFlow cashFlow = cashFlowListIterator.next();

            if (cashFlow.getType() == CashFlow.Type.INCOME) {
                values.add(new PointValue(i, cashFlow.getAmount().floatValue()));
            } else if (cashFlow.getType() == CashFlow.Type.EXPANSE) {
                values.add(new PointValue(i, cashFlow.getAmount().floatValue() * -1));
            }

            dateAxisValues.add(new AxisValue(
                    i,
                    KorzeniowskiUtils.Dates.getShortDateLabel(getActivity(), cashFlow.getDateTime()).toCharArray()));

        }
        if (values.size() > 1) {
            Line mainLine = new Line(values);
            mainLine.setColor(getResources().getColor(R.color.blue));
            mainLine.setHasLabelsOnlyForSelected(true);

            LineChartData lineChartData = new LineChartData(Lists.newArrayList(mainLine));

            // setup axis with dates.
            lineChartData.setAxisXBottom(new Axis(dateAxisValues));

            // setup axis with values.
            lineChartData.setAxisYLeft(new Axis().setHasLines(true));

            return lineChartData;
        } else {
            return new LineChartData(Lists.<Line>newArrayList());
        }
    }

    private char[] getAxisLabel(Date dateTime) {
//        int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE;
//        return DateUtils.formatDateTime(getActivity(), dateTime.getTime(), flags).toCharArray();
        String dateLabel = DateFormat.getDateFormat(DashboardFragment.this.getActivity()).format(dateTime.getTime());
        String timeLabel = DateFormat.getTimeFormat(DashboardFragment.this.getActivity()).format(dateTime.getTime());
        return (dateLabel + "\n" + timeLabel).toCharArray();
    }

    private CharSequence getTotalAmountText() {
        sumOfCurrentAmountOfWallets = getSumOfCurrentAmountOfWallets();
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
}
