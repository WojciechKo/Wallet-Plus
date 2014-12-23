package info.korzeniowski.walletplus.ui.wallet.list;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.text.MessageFormat;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.MainActivity;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.CashFlowService;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.wallet.details.WalletDetailsFragment;

public class WalletListFragment extends Fragment {
    public static final String TAG = "walletList";

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Inject
    @Named("local")
    CashFlowService localCashFlowService;

    @Inject
    Bus bus;

    private List<Wallet> walletList;
    private BaseSwipeListViewListener swipeListViewListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.wallet_list, container, false);
        ButterKnife.inject(this, view);
        walletList = localWalletService.getMyWallets();
        setupViews();
        return view;
    }

    void setupViews() {
        list.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                startWalletDetailsFragment(list.getAdapter().getItemId(position));
            }

            @Override
            public void onDismiss(int[] reverseSortedPositions) {
                for (int index : reverseSortedPositions) {
                    walletList.remove(index);
                }
                list.setAdapter(new WalletListAdapter(getActivity(), walletList));
            }
        });
        list.setAdapter(new WalletListAdapter(getActivity(), walletList));
    }

    @Subscribe
    public void deleteWalletEvent(DeleteWalletEvent event) {
        showDeleteConfirmationAlert(event.getId());
    }

    @Override
    public void onStart() {
        super.onStart();
        bus.register(this);
    }

    @Override
    public void onStop() {
        bus.unregister(this);
        super.onStop();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(info.korzeniowski.walletplus.R.menu.action_new, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_new) {
            startWalletDetailsFragment(0L);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startWalletDetailsFragment(Long id) {
        Fragment fragment = new WalletDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(WalletDetailsFragment.WALLET_ID, id);
        fragment.setArguments(bundle);
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setContentFragment(fragment, true, WalletDetailsFragment.TAG);
        }
    }

    private void showDeleteConfirmationAlert(final Long walletId) {
        new AlertDialog.Builder(getActivity())
                .setMessage(getConfirmationMessage(walletId))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tryDelete(walletId);
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private String getConfirmationMessage(Long walletId) {
        int count = (int) localCashFlowService.countAssignedToWallet(walletId);
        String msg = getActivity().getString(R.string.walletDeleteConfirmation);
        return MessageFormat.format(msg, count);
    }

    private void tryDelete(final Long id) {
        localWalletService.deleteById(id);
        final int index = Iterables.indexOf(walletList, new Predicate<Wallet>() {
            @Override
            public boolean apply(Wallet input) {
                return id.equals(input.getId());
            }
        });
        list.dismiss(index);
    }
}
