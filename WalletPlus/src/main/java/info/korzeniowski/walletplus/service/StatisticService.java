package info.korzeniowski.walletplus.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import info.korzeniowski.walletplus.model.Category;

public interface StatisticService {

    enum Period {
        DAY,
        WEEK,
        MONTH,
        YEAR
    }

    List<Map.Entry<Category, Double>> getCategoryListWit(Category.Type type, Period period, Date periodBegin, int iteration);
}
