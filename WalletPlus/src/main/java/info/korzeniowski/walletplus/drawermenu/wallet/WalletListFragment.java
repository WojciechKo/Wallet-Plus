package info.korzeniowski.walletplus.drawermenu.wallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.service.WalletService;

public class WalletListFragment extends Fragment {

    @InjectView(R.id.list)
    ListView list;

    @Inject @Named("local")
    WalletService localWalletService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.card_list, container, false);
        ButterKnife.inject(this, view);
        setupViews();
        return view;
    }

    void setupViews() {
        setHasOptionsMenu(true);
        list.setAdapter(new WalletListAdapter(getActivity(), localWalletService.getMyWallets()));
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                listItemClicked(position);
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_new, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == info.korzeniowski.walletplus.R.id.menu_new) {
            actionAdd();
            return true;
        }
        return false;
    }

    void listItemClicked(int position) {
        startWalletDetailsFragment(list.getAdapter().getItemId(position));
    }

    void actionAdd() {
        startWalletDetailsFragment(0L);
    }

    private void startWalletDetailsFragment(Long id) {
        Fragment fragment = new WalletDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(WalletDetailsFragment.WALLET_ID, id);
        fragment.setArguments(bundle);
        ((MainActivity) getActivity()).setContentFragment(fragment, true);
    }
}
