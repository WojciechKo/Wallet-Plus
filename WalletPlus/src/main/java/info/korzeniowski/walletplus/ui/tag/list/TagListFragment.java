package info.korzeniowski.walletplus.ui.tag.list;

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
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.ui.tag.details.TagDetailsActivity;

public class TagListFragment extends Fragment {
    public static final String TAG = TagListFragment.class.getSimpleName();

    @InjectView(R.id.list)
    ListView list;

    @Inject
    @Named("local")
    TagService localTagService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((WalletPlus) getActivity().getApplication()).inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tag_list, container, false);
        ButterKnife.inject(this, view);
        setupList();
        return view;
    }

    @OnItemClick(R.id.list)
    void listItemClicked(int position) {
        Intent intent = new Intent(getActivity(), TagDetailsActivity.class);
        intent.putExtra(TagDetailsActivity.EXTRAS_TAG_ID, list.getAdapter().getItemId(position));
        startActivityForResult(intent, TagDetailsActivity.REQUEST_CODE_EDIT_TAG);
    }

    private void setupList() {
        List<Tag> tagList = localTagService.getAll();
        list.setAdapter(new TagListAdapter(getActivity(), tagList));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TagDetailsActivity.REQUEST_CODE_ADD_TAG) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
            }
        } else if (requestCode == TagDetailsActivity.REQUEST_CODE_EDIT_TAG) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    setupList();
                    return;
                case TagDetailsActivity.RESULT_DELETED:
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
