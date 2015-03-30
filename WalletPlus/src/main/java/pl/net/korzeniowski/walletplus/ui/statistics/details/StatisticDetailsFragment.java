package pl.net.korzeniowski.walletplus.ui.statistics.details;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import pl.net.korzeniowski.walletplus.R;
import pl.net.korzeniowski.walletplus.WalletPlus;
import pl.net.korzeniowski.walletplus.model.Tag;
import pl.net.korzeniowski.walletplus.service.TagService;

public class StatisticDetailsFragment extends Fragment {
    public static final String TAG = StaticticDetailsActivity.class.getSimpleName();

    public static final String ARGUMENT_CATEGORY_ID = "CATEGORY_ID";

    @InjectView(R.id.tagNameLabel)
    TextView categoryNameLabel;

    @InjectView(R.id.tagName)
    EditText categoryName;

    @Inject
    TagService tagService;

    private DetailsAction detailsAction;
    private Optional<Tag> categoryToEdit;

    public static StatisticDetailsFragment newInstance(Long categoryId) {
        StatisticDetailsFragment fragment = new StatisticDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_CATEGORY_ID, categoryId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((WalletPlus) getActivity().getApplication()).component().inject(this);

        Long categoryId = getArguments() == null ? -1 : getArguments().getLong(ARGUMENT_CATEGORY_ID);

        if (categoryId == -1) {
            detailsAction = DetailsAction.ADD;
            categoryToEdit = Optional.absent();
        } else {
            detailsAction = DetailsAction.EDIT;
            categoryToEdit = Optional.of(tagService.findById(categoryId));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tag_details, container, false);
        ButterKnife.inject(this, view);

        List<Tag> mainCategories = tagService.getAll();

        if (savedInstanceState == null && detailsAction == DetailsAction.EDIT) {
            mainCategories.remove(categoryToEdit.get());
            categoryName.setText(categoryToEdit.get().getName());
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.menu_save) {
            onSaveOptionSelected();
            return true;
        }
        return result;
    }

    void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(categoryName.getText().toString())) {
            categoryName.setError(getString(R.string.tagNameIsRequired));
            return;
        }

        if (categoryName.getError() == null) {
            Tag tagToSave = new Tag();
            tagToSave.setName(categoryName.getText().toString());

            if (detailsAction == DetailsAction.ADD) {
                tagService.insert(tagToSave);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();

            } else if (detailsAction == DetailsAction.EDIT) {
                tagToSave.setId(categoryToEdit.get().getId());
                tagService.update(tagToSave);
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        }
    }

    private enum DetailsAction {ADD, EDIT}

    static class ParentCategoryAdapter extends ArrayAdapter<Tag> {
        private final WeakReference<Context> context;

        ParentCategoryAdapter(Context context, List<Tag> mainCategories) {
            super(context, 0);
            this.context = new WeakReference<>(context);
            addAll(mainCategories);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ParentCategoryViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context.get()).inflate(android.R.layout.simple_spinner_item, null);
                holder = new ParentCategoryViewHolder();
                ButterKnife.inject(holder, convertView);
                convertView.setTag(holder);
            } else {
                holder = (ParentCategoryViewHolder) convertView.getTag();
            }

            Tag item = getItem(position);
            holder.categoryName.setText(item.getName());

            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getView(position, convertView, parent);
        }

        class ParentCategoryViewHolder {
            @InjectView(android.R.id.text1)
            TextView categoryName;
        }
    }
}
