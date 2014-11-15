package info.korzeniowski.walletplus;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.Period;

public class KorzeniowskiUtils {
    public static class Time {
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

}
