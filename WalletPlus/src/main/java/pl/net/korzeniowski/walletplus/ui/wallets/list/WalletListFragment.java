package pl.net.korzeniowski.walletplus.ui.wallets.list;

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

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.model.Wallet;
import pl.net.korzeniowski.walletplus.service.WalletService;
import pl.net.korzeniowski.walletplus.ui.wallets.details.WalletDetailsActivity;

public class WalletListFragment extends Fragment {
    public static final String TAG = WalletListFragment.class.getSimpleName();

    @InjectView(R.id.swipe_list)
    SwipeListView list;

    @Inject
    WalletService walletService;

    private List<Wallet> walletList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).component().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_wallet_list, container, false);
        ButterKnife.inject(this, view);
        setupList();
        return view;
    }

    private void setupList() {
        walletList = walletService.getAll();
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
