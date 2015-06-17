package com.walletudo.ui.statistics;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.model.Tag;
import com.walletudo.service.StatisticService;
import com.walletudo.ui.view.AmountView;
import com.walletudo.ui.view.TagView;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class StatisticFragment extends Fragment {

    @InjectView(R.id.balance)
    AmountView balance;

    @InjectView(R.id.viewPager)
    ViewPager viewPager;

    @InjectView(R.id.emptyProfitList)
    TextView emptyProfitList;

    @InjectView(R.id.profitList)
    ListView profitList;

    @InjectView(R.id.emptyLostList)
    TextView emptyLostList;

    @InjectView(R.id.lostList)
    ListView lostList;

    @InjectView(R.id.fab)
    FloatingActionMenu fab;

    @Inject
    StatisticService statisticService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Walletudo) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = View.inflate(getActivity(), R.layout.fragment_statistics, null);
        ButterKnife.inject(this, view);
        setupViews();
        onFabWeekPeriodClick();
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.toolbar_tabs);
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViews() {
        profitList.setEmptyView(emptyProfitList);
        lostList.setEmptyView(emptyLostList);
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                switch (position) {
                    case 0:
                        return getActivity().findViewById(R.id.profitView);
                    case 1:
                        return getActivity().findViewById(R.id.lostView);
                }
                return null;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.statisticsProfitLabel);
                    case 1:
                        return getString(R.string.statisticsLostLabel);
                }
                return "";
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == ((View) object);
            }
        });
    }

    @OnClick(R.id.fab_week)
    public void onFabWeekPeriodClick() {
        LocalDate now = LocalDate.now();
        setupStatisticFromPeriod(now.dayOfWeek().withMinimumValue(), now.dayOfMonth().withMaximumValue());
        fab.close(true);
    }

    @OnClick(R.id.fab_month)
    public void onFabMonthPeriodClick() {
        LocalDate now = LocalDate.now();
        setupStatisticFromPeriod(now.dayOfMonth().withMinimumValue(), now.dayOfMonth().withMaximumValue());
        fab.close(true);
    }

    @OnClick(R.id.fab_custom)
    public void onFabCustomPeriodClick() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.statisticCustomPeriodNotAvailable), Toast.LENGTH_SHORT).show();
    }

    private void setupStatisticFromPeriod(LocalDate from, LocalDate to) {
        StatisticService.Statistics statistics = statisticService.getStatistics(from.toDate(), to.toDate());
        balance.setAmount(statistics.getBalance());

        profitList.setAdapter(new StatisticEntryAdapter(getActivity(), statistics.getProfit()));

        List<Map.Entry<Tag, Double>> lost = Lists.transform(statistics.getLost(), new Function<Map.Entry<Tag,Double>, Map.Entry<Tag, Double>>() {
            @Override
            public Map.Entry<Tag, Double> apply(@Nullable Map.Entry<Tag, Double> input) {
                return Maps.immutableEntry(input.getKey(), -input.getValue());
            }
        });
        lostList.setAdapter(new StatisticEntryAdapter(getActivity(), lost));
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
                convertView = LayoutInflater.from(context).inflate(R.layout.item_statistics_list, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Map.Entry<Tag, Double> item = getItem(position);
            holder.tag.setText(item.getKey().getName());
            holder.tag.setTagColor(item.getKey().getColor());
            holder.amount.setAmount(item.getValue());
            return convertView;
        }

        static class ViewHolder {
            @InjectView(R.id.tag)
            TagView tag;

            @InjectView(R.id.amount)
            AmountView amount;

            ViewHolder(View view) {
                ButterKnife.inject(this, view);
            }
        }
    }
}
