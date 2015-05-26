package com.walletudo.ui.dashboard;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walletudo.R;
import com.walletudo.model.CashFlow;
import com.walletudo.service.CashFlowService;
import com.walletudo.util.WalletudoUtils;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.BubbleChartData;
import lecho.lib.hellocharts.model.BubbleValue;

public class BubbleChartDataProvider {

    private static final int COLUMN_SIZE = 100;
    private final Map<BubbleValue, CashFlow> bubbleMap;
    private final CashFlowService cashFlowService;
    private final int columnNumber;
    private Context context;
    private List<CashFlow> cashFlowList;

    public BubbleChartDataProvider(Context context, CashFlowService cashFlowService) {
        this.context = context;
        this.cashFlowService = cashFlowService;
        bubbleMap = Maps.newHashMap();

        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        columnNumber = (int) ((displayMetrics.widthPixels / displayMetrics.density) / COLUMN_SIZE);
    }

    public BubbleChartData getBubbleChartData(LocalDate firstDay, LocalDate today) {
        Preconditions.checkArgument(today.toDate().getTime() - firstDay.toDate().getTime() >= 0);

        cashFlowList = cashFlowService.findCashFlows(
                new CashFlowService.CashFlowQuery()
                        .withFromDate(firstDay.toDate())
                        .withToDate(today.toDate()));

        BubbleChartData bubbleChartData = new BubbleChartData(getBubbleValues());
        bubbleChartData.setAxisXBottom(getDateAxis(firstDay, today));
        bubbleChartData.setAxisYRight(getZeroLineAxis());

        Axis axisWithLines = getHelperLinesAxis();
        axisWithLines.setHasLines(true);
        bubbleChartData.setAxisYLeft(axisWithLines);
        return bubbleChartData;
    }

    private List<BubbleValue> getBubbleValues() {
        List<BubbleValue> bubbleValues = Lists.newArrayList();
        for (CashFlow cashFlow : cashFlowList) {
            BubbleValue bubbleValue = getBubbleValue(cashFlow);
            bubbleMap.put(bubbleValue, cashFlow);
            bubbleValues.add(bubbleValue);
        }
        return bubbleValues;
    }

    private BubbleValue getBubbleValue(CashFlow cashFlow) {
        int color = 0;
        if (cashFlow.getType().equals(CashFlow.Type.EXPENSE)) {
            color = context.getResources().getColor(R.color.red);
        } else if (cashFlow.getType().equals(CashFlow.Type.INCOME)) {
            color = context.getResources().getColor(R.color.green);
        }
        color = Color.argb(130, Color.red(color), Color.green(color), Color.blue(color));

        Date roundedDate = new LocalDate(cashFlow.getDateTime()).toDate();

        return new BubbleValue(
                (float) roundedDate.getTime(),
                cashFlow.getRelativeAmount().floatValue(),
                cashFlow.getAmount().floatValue(),
                color).setLabel(cashFlow.getRelativeAmount().toString());
    }

    private Axis getDateAxis(LocalDate firstDay, LocalDate today) {
        List<AxisValue> dateAxisValues = Lists.newArrayList();
        while (!firstDay.equals(today)) {
            dateAxisValues.add(getAxisValue(firstDay.toDate()));
            firstDay = firstDay.plusDays(1);
        }
        dateAxisValues.add(getAxisValue(today.toDate()));
        return new Axis(dateAxisValues);
    }

    private Axis getZeroLineAxis() {
        return new Axis(Lists.newArrayList(new AxisValue(0.0F).setLabel(""))).setHasSeparationLine(false).setHasLines(true).setLineColor(context.getResources().getColor(R.color.red));
    }

    private Axis getHelperLinesAxis() {
        List<AxisValue> helperLinesAxisValueList = Lists.newArrayList();
        for (Integer value : WalletudoUtils.Charts.getHelperLineValues(cashFlowList)) {
            helperLinesAxisValueList.add(new AxisValue(value));
        }
        return new Axis(helperLinesAxisValueList);
    }


    private AxisValue getAxisValue(Date date) {
        AxisValue axisValue = new AxisValue(date.getTime());
        axisValue.setLabel(new SimpleDateFormat("dd.MM", Locale.getDefault()).format(date));
//        axisValue.setLabel(android.text.format.DateFormat.getDateFormat(getActivity()).format(date));
//        axisValue.setLabel(WalletudoUtils.Dates.getShortDateLabel(getActivity(), date));
        return axisValue;
    }

    public CashFlow getCashFlow(BubbleValue bubbleValue) {
        return bubbleMap.get(bubbleValue);
    }

    public int getColumnsNumber() {
        return columnNumber;
    }
}
