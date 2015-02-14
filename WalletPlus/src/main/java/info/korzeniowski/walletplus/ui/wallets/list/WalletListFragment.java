package info.korzeniowski.walletplus.ui.wallets.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fortysevendeg.swipelistview.BaseSwipeListViewListener;
import com.fortysevendeg.swipelistview.SwipeListView;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;

public class WalletListFragment extends Fragment {
    public static final String TAG = WalletListFragment.class.getSimpleName();

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    @Named("local")
    WalletService localWalletService;

    private List<Wallet> walletList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_my_wallet_list, container, false);
        ButterKnife.inject(this, view);
        setupList();
        return view;
    }

    private void setupList() {
        walletList = localWalletService.getMyWallets();
        list.setSwipeListViewListener(new BaseSwipeListViewListener() {
            @Override
            public void onClickFrontView(int position) {
                Intent intent = new Intent(getActivity(), WalletDetailsActivity.class);
                intent.putExtra(WalletDetailsActivity.EXTRAS_WALLET_ID, list.getAdapter().getItemId(position));
                startActivityForResult(intent, WalletDetailsActivity.REQUEST_CODE_EDIT_WALLET);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WalletDetailsActivity.REQUEST_CODE_ADD_WALLET) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
            }
        } else if (requestCode == WalletDetailsActivity.REQUEST_CODE_EDIT_WALLET) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
                case WalletDetailsActivity.RESULT_DELETED:
                    setupList();
                    return;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }
}
