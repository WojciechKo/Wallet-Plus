package info.korzeniowski.walletplus.service.local;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.service.StatisticService;

public class LocalStatisticService implements StatisticService {

    @Inject
    public LocalStatisticService() {

    }

    @Override
    public List<Map.Entry<Category, Double>> getCategoryListWit(Category.Type type, Period period, Date periodBegin, int iteration) {
        return null;
    }
}
