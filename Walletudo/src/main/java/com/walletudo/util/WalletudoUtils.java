package com.walletudo.util;

import android.content.Context;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import com.google.common.collect.Lists;
import com.walletudo.model.CashFlow;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WalletudoUtils {
    public static class Dates {
        public static String getShortDateLabel(Context context, Date date) {
            return DateUtils.formatDateTime(
                    context,
                    date.getTime(),
                    DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_NO_YEAR | DateUtils.FORMAT_ABBREV_MONTH);
        }
    }

    public static class Times {
        public static Interval getInterval(DateTime firstDay, Period period, Integer iteration) {
            DateTime firstDayArg;
            if (iteration <= 0) {
                firstDayArg = firstDay.minus(period.multipliedBy(0 - iteration));
                firstDayArg = new DateTime(firstDayArg.getYear(), firstDayArg.getMonthOfYear(), firstDayArg.getDayOfMonth(), 0, 0);
            } else {
                firstDayArg = firstDay.plus(period.multipliedBy(iteration));
                firstDayArg = new DateTime(firstDayArg.getYear(), firstDayArg.getMonthOfYear(), firstDayArg.getDayOfMonth(), 0, 0);
            }

            DateTime lastDayArg = firstDayArg.plus(period);
            lastDayArg = new DateTime(lastDayArg.getYear(), lastDayArg.getMonthOfYear(), lastDayArg.getDayOfMonth(), 23, 59, 59, 999);
            return new Interval(firstDayArg, lastDayArg.minus(Days.ONE));
        }
    }

    public static class Views {
        public static View getViewByPosition(ListView listView, int pos) {
            final int firstListItemPosition = listView.getFirstVisiblePosition();
            final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

            if (pos < firstListItemPosition || pos > lastListItemPosition) {
                return listView.getAdapter().getView(pos, null, listView);
            } else {
                final int childIndex = pos - firstListItemPosition;
                return listView.getChildAt(childIndex);
            }
        }

        public static void performItemClick(ListView list, int position) {
            list.performItemClick(list.getChildAt(position), position, list.getAdapter().getItemId(position));
        }

        public static int dipToPixels(Context context, float dipValue) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics) + 0.5);
        }
    }

    public static class Files {
        public static String getBaseName(String fileName) {
            String split = "\\.(?=[^\\.]+$)";
            return fileName.split(split)[0];
        }
    }

    public static class Charts {
        private static final ArrayList<Integer> CORE_DIVIDERS = Lists.newArrayList(1, 2, 5);

        public static List<Integer> getHelperLineValues(List<CashFlow> cashFlows) {
            List<Integer> result = Lists.newArrayList();
            Double min = Double.MAX_VALUE;
            Double max = Double.MIN_VALUE;
            for (CashFlow cashFlow : cashFlows) {
                Double amountOfCashFlow = cashFlow.getRelativeAmount();
                if (amountOfCashFlow > max) {
                    max = amountOfCashFlow;
                }
                if (amountOfCashFlow < min) {
                    min = amountOfCashFlow;
                }
            }
            Integer step = calculateStep(min, max);
            Integer first = ((int) (min / step)) * step - step;

            int iteration = (int) (max - min) / step + 2;

            for (int i = 0; i < iteration; i++) {
                result.add(first + step * i);
            }

            return result;
        }

        private static Integer calculateStep(Double min, Double max) {
            Double diff = max - min;
            Integer previousStepValue = getNStepValue(0);
            for (int i = 0; i < 1000; i++) {
                Integer stepValue = getNStepValue(i);
                if (diff / stepValue <= 4) {
                    if (diff / stepValue >= 2) {
                        return stepValue;
                    } else {
                        return previousStepValue;
                    }
                }
                previousStepValue = stepValue;
            }
            return previousStepValue;
        }

        private static Integer getNStepValue(int n) {
            return CORE_DIVIDERS.get(n % CORE_DIVIDERS.size()) * (int) Math.pow(10, n / CORE_DIVIDERS.size());
        }
    }
}
