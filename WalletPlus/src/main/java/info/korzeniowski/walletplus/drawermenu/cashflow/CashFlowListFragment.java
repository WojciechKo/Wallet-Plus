package info.korzeniowski.walletplus.drawermenu.cashflow;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.CashFlow;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.widget.IdentityableMultiChoiceModeListener;

/**
 * Fragment with list of cash flows.
 */
@OptionsMenu(R.menu.action_new)
@EFragment(R.layout.card_list)
public class CashFlowListFragment extends Fragment {

    @ViewById
    ListView list;

    @Inject @Named("local")
    CashFlowService localCashFlowService;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupView() {
        setHasOptionsMenu(true);
        list.setAdapter(new CashFlowListAdapter(getActivity(), localCashFlowService.getAll()));
        list.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new IdentityableMultiChoiceModeListener<CashFlow>(list, localCashFlowService, getActivity()));
    }

    @ItemClick
    void listItemClicked(int position) {
        startCashFlowDetailsFragment(list.getAdapter().getItemId(position));
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        startCashFlowDetailsFragment(0L);
    }

    private void startCashFlowDetailsFragment(Long id) {
        Fragment fragment = new CashFlowDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(CashFlowDetailsFragment_.CASH_FLOW_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }
}