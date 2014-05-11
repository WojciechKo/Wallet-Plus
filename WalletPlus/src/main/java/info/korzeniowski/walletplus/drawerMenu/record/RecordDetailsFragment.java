package info.korzeniowski.walletplus.drawermenu.record;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.googlecode.androidannotations.annotations.AfterInject;
import com.googlecode.androidannotations.annotations.AfterViews;
import com.googlecode.androidannotations.annotations.EFragment;
import com.googlecode.androidannotations.annotations.OptionsItem;
import com.googlecode.androidannotations.annotations.OptionsMenu;

import javax.inject.Inject;
import javax.inject.Named;

import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.datamanager.RecordDataManager;
import info.korzeniowski.walletplus.model.Category;
import info.korzeniowski.walletplus.model.Record;

@EFragment(R.layout.record_details_fragment)
@OptionsMenu(R.menu.action_save)
public class RecordDetailsFragment extends Fragment {
    private enum DetailsType {ADD, EDIT}
    static final public String RECORD_ID = "RECORD_ID";
    @Inject @Named("local")
    RecordDataManager localRecordDataManager;
    private Long recordId;
    private DetailsType type;
    private Record record;

    @AfterInject
    void daggerInject() {
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        recordId = getArguments().getLong(RECORD_ID);
        type = recordId == 0L ? DetailsType.ADD : DetailsType.EDIT;
        if (type.equals(DetailsType.EDIT)) {
            record = getRecord();
        }
        return null;
    }

    private Record getRecord() {
        return localRecordDataManager.getById(recordId);
    }

    @AfterViews
    void setupViews() {
        Log.d("WalletPlus", "CategoryDetails.setupViews");
        setupAdapters();
        setupListeners();
        if (type.equals(DetailsType.ADD)) {
            record = new Record();
        } else if (type.equals(DetailsType.EDIT)) {
            fillViewsWithData();
        }
    }

    private void setupAdapters() {

    }

    private void setupListeners() {

    }

    private void fillViewsWithData() {

    }

    public void getDataFromViews() {

    }
    @OptionsItem(R.id.menu_save)
    void actionSave() {
        Log.d("WalletPlus", "CategoryDetails.actionSave");
        getDataFromViews();
        if (DetailsType.ADD.equals(type)) {
            localRecordDataManager.insert(record);
        } else if (DetailsType.EDIT.equals(type)) {
            localRecordDataManager.update(record);
        }
        getActivity().getSupportFragmentManager().popBackStack();
    }


}
