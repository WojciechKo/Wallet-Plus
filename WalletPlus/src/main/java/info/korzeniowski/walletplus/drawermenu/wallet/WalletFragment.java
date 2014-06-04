package info.korzeniowski.walletplus.drawermenu.wallet;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ItemClick;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.WalletDataManager;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment;
import info.korzeniowski.walletplus.drawermenu.category.CategoryDetailsFragment_;

@OptionsMenu(R.menu.action_new)
@EFragment
public class WalletFragment extends ListFragment {
    @Inject
    @Named("local")
    WalletDataManager localWalletDataManager;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @AfterViews
    void setupViews() {
        setHasOptionsMenu(true);
        setListAdapter(new WalletListAdapter(getActivity(), localWalletDataManager.getMyWallets()));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        startWalletDetailsFragment(id);
    }

    @OptionsItem(R.id.menu_new)
    void actionAdd() {
        startWalletDetailsFragment();
    }

    private void startWalletDetailsFragment() {
        startWalletDetailsFragment(0L);
    }

    private void startWalletDetailsFragment(Long id) {
        Fragment fragment = new WalletDetailsFragment_();
        Bundle bundle = new Bundle();
        bundle.putLong(WalletDetailsFragment.WALLET_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }
}
