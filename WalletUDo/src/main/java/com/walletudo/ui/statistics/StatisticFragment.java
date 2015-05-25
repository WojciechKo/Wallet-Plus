package com.walletudo.ui.statistics;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.walletudo.R;
import com.walletudo.WalletUDo;
import com.walletudo.model.Tag;
import com.walletudo.service.StatisticService;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StatisticFragment extends Fragment {

    @InjectView(R.id.balance)
    TextView balance;

    @InjectView(R.id.profitList)
    ListView profitList;

    @InjectView(R.id.lostList)
    ListView lostList;

    @InjectView(R.id.fab)
    FloatingActionMenu fab;

    @Inject
    StatisticService statisticService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletUDo) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_statistics, null);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    private void setupViews() {
        onFabWeekPeriodClick();
    }

    @OnClick(R.id.fab_week)
    public void onFabWeekPeriodClick() {
        LocalDate now = LocalDate.now();
        showStatisticsFromPeriod(now.dayOfWeek().withMinimumValue(), now.dayOfMonth().withMaximumValue());
    }

    @OnClick(R.id.fab_month)
    public void onFabMonthPeriodClick() {
        LocalDate now = LocalDate.now();
        showStatisticsFromPeriod(now.dayOfMonth().withMinimumValue(), now.dayOfMonth().withMaximumValue());
    }

    @OnClick(R.id.fab_custom)
    public void onFabCustomPeriodClick() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.statisticCustomPeriodNotAvailable), Toast.LENGTH_SHORT).show();
    }

    private void showStatisticsFromPeriod(LocalDate from, LocalDate to) {
        StatisticService.Statistics statistics = statisticService.getStatistics(from.toDate(), to.toDate());
        balance.setText(statistics.getBalance().toString());

        profitList.setAdapter(new StatisticEntryAdapter(getActivity(), statistics.getProfit()));
        lostList.setAdapter(new StatisticEntryAdapter(getActivity(), statistics.getLost()));
    }

    public static class StatisticEntryAdapter extends BaseAdapter {
        private List<Map.Entry<Tag, Double>> profit;
        private Context context;

        public StatisticEntryAdapter(Context context, List<Map.Entry<Tag, Double>> profit) {
            this.context = context;
            this.profit = profit;
        }

        @Override
        public int getCount() {
            return profit.size();
        }

        @Override
        public Map.Entry<Tag, Double> getItem(int position) {
            return profit.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getKey().getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_2, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Map.Entry<Tag, Double> item = getItem(position);
            holder.text1.setText(item.getKey().getName());
            holder.text2.setText(item.getValue().toString());
            return convertView;
        }

        static class ViewHolder {
            @InjectView(android.R.id.text1)
            TextView text1;

            @InjectView(android.R.id.text2)
            TextView text2;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
