package info.korzeniowski.walletplus.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ListView;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Period;

public class KorzeniowskiUtils {
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
}
