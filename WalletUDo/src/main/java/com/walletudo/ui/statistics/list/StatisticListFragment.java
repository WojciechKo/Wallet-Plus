package com.walletudo.ui.statistics.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.walletudo.R;
import com.walletudo.WalletUDo;
import com.walletudo.service.CashFlowService;
import com.walletudo.service.StatisticService;
import com.walletudo.service.TagService;
import com.walletudo.ui.statistics.details.StaticticDetailsActivity;

import org.joda.time.Period;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class StatisticListFragment extends Fragment {
    public static final String ITERATION = "iteration";

    @InjectView(R.id.list)
    ListView list;

    @Inject
    TagService tagService;

    @Inject
    CashFlowService cashFlowService;

    @Inject
    StatisticListActivityState statisticListActivityState;

    private int iteration;

    public static StatisticListFragment newInstance(int iteration) {
        StatisticListFragment fragment = new StatisticListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ITERATION, iteration);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletUDo) getActivity().getApplication()).component().inject(this);
        iteration = getArguments().getInt(ITERATION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        list.setAdapter(new StatisticListAdapter(
                getActivity(),
                statisticListActivityState.getTagList(),
                getCategoryStatsList()));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startCategoryDetailsActivity(id);
            }
        });
    }


    private List<StatisticService.TagStats> getCategoryStatsList() {
        return null;
//        return tagService.getTagStatsList(statisticListActivityState.getStartDate(), getPeriod(statisticListActivityState.getPeriod()), iteration);
    }

    private Period getPeriod(StatisticListActivity.Period period) {
        switch (period) {
            case DAY:
                return Period.days(1);
            case WEEK:
                return Period.weeks(1);
            case MONTH:
                return Period.months(1);
            case YEAR:
                return Period.years(1);
        }
        return null;
    }

    private void startCategoryDetailsActivity(Long id) {
        Intent intent = new Intent(getActivity(), StaticticDetailsActivity.class);
        intent.putExtra(StaticticDetailsActivity.EXTRAS_TAG_ID, id);
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
