package info.korzeniowski.walletplus.ui.cashflow.list;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.google.common.collect.Lists;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import butterknife.OnItemLongClick;
import info.korzeniowski.walletplus.KorzeniowskiUtils;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.ui.cashflow.details.CashFlowDetailsContainerFragment;

/**
 * Fragment with list of cash flows.
 */
public class CashFlowListFragment extends Fragment {
    public static final String TAG = "cashFlowList";

    @InjectView(R.id.list)
    ListView list;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    private List<CashFlow> categories;
    private List<CashFlow> selected;
    private String title;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.card_list, container, false);
        ButterKnife.inject(this, view);
        categories = localCashFlowService.getAll();
        selected = Lists.newArrayList();
        setupView();
        return view;
    }

    void setupView() {
        list.setAdapter(new CashFlowListAdapter(getActivity(), categories));
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        View itemView = KorzeniowskiUtils.Views.getViewByPosition(list, position);
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            itemView.setBackgroundResource(R.drawable.list_item_background_checked);
            startCashFlowDetailsFragment(list.getAdapter().getItemId(position));
        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            handleCategorySelect(position, itemView);
            if (selected.size() == 0) {
                endMultipleChoiceMode();
            } else {
                getActivity().setTitle(getSelectedTitle());
            }
        }
    }

    private void handleCategorySelect(int position, View itemView) {
        if (selected.contains(categories.get(position))) {
            unselectCategory(position, itemView);
        } else {
            selectCategory(position, itemView);
        }
    }

    @OnItemLongClick(R.id.list)
    boolean listItemLongClicked(int position) {
        if (list.getChoiceMode() == AbsListView.CHOICE_MODE_SINGLE) {
            startMultipleChoiceMode(position);
        } else if (list.getChoiceMode() == AbsListView.CHOICE_MODE_MULTIPLE) {
            listItemClicked(position);
        }
        return true;
    }

    private void startMultipleChoiceMode(int position) {
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        selectCategory(position, KorzeniowskiUtils.Views.getViewByPosition(list, position));
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.darkerMainColor));

        title = getActivity().getTitle().toString();
        getActivity().setTitle(getSelectedTitle());
    }

    private void endMultipleChoiceMode() {
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.mainColor));
        getActivity().setTitle(title);
    }

    @Override
    public void onStop() {
        ((MainActivity) getActivity()).setToolbarBackground(getResources().getColor(R.color.mainColor));
        super.onStop();
    }

    private String getSelectedTitle() {
        return title + " (" + selected.size() + ")";
    }

    private void selectCategory(int position, View itemView) {
        selected.add(categories.get(position));
        itemView.setBackgroundResource(R.drawable.list_item_background_checked);
    }

    private void unselectCategory(int position, View itemView) {
        selected.remove(categories.get(position));
        itemView.setBackgroundResource(R.drawable.list_item_background);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.action_new, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            startCashFlowDetailsFragment();
            return true;
        }
        return false;
    }

    private void startCashFlowDetailsFragment() {
        ((MainActivity) getActivity()).setContentFragment(new CashFlowDetailsContainerFragment(), true, CashFlowDetailsContainerFragment.TAG);
    }

    private void startCashFlowDetailsFragment(Long id) {
        Fragment fragment = new CashFlowDetailsContainerFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(CashFlowDetailsContainerFragment.CASH_FLOW_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true, CashFlowDetailsContainerFragment.TAG);
    }
}