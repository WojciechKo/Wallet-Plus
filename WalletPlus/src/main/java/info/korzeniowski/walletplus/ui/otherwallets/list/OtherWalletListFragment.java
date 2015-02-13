package info.korzeniowski.walletplus.ui.otherwallets.list;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnItemClick;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Wallet;
import info.korzeniowski.walletplus.service.WalletService;
import info.korzeniowski.walletplus.ui.otherwallets.details.OtherWalletDetailsActivity;

public class OtherWalletListFragment extends Fragment {
    public static final String TAG = OtherWalletListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    ListView list;

    @Inject
    @Named("local")
    WalletService localWalletService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_other_wallet_list, container, false);
        ButterKnife.inject(this, view);
        setupList();
        return view;
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        Intent intent = new Intent(getActivity(), OtherWalletDetailsActivity.class);
        intent.putExtra(OtherWalletDetailsActivity.EXTRAS_WALLET_ID, list.getAdapter().getItemId(position));
        startActivityForResult(intent, OtherWalletDetailsActivity.REQUEST_CODE_EDIT_WALLET);
    }

    private void setupList() {
        List<Wallet> walletList = localWalletService.getMyWallets();
        list.setAdapter(new OtherWalletListAdapter(getActivity(), walletList));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == OtherWalletDetailsActivity.REQUEST_CODE_ADD_WALLET) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
            }
        } else if (requestCode == OtherWalletDetailsActivity.REQUEST_CODE_EDIT_WALLET) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
                case OtherWalletDetailsActivity.RESULT_DELETED:
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
