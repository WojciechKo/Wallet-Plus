package info.korzeniowski.walletplus.ui.tag.details;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import afzkl.development.colorpickerview.dialog.ColorPickerDialog;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Tag;
import info.korzeniowski.walletplus.service.TagService;
import info.korzeniowski.walletplus.util.PrefUtils;

public class TagDetailsFragment extends Fragment {
    public static final String TAG = TagDetailsFragment.class.getSimpleName();
    public static final String ARGUMENT_TAG_ID = "TAG_ID";

    @InjectView(R.id.tagNameLabel)
    TextView tagNameLabel;

    @InjectView(R.id.tagName)
    EditText tagName;

    @InjectView(R.id.colorPicker)
    Button colorPicker;

    @Inject
    @Named("local")
    TagService localTagService;

    @Inject
    @Named("amount")
    NumberFormat amountFormat;

    private DetailsAction detailsAction;
    private Optional<Tag> tagToEdit;

    public static TagDetailsFragment newInstance(Long tagId) {
        TagDetailsFragment fragment = new TagDetailsFragment();
        Bundle arguments = new Bundle();
        arguments.putLong(ARGUMENT_TAG_ID, tagId);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        ((WalletPlus) getActivity().getApplication()).inject(this);

        Long tagId = getArguments() == null ? -1 : getArguments().getLong(ARGUMENT_TAG_ID);

        if (tagId == -1) {
            detailsAction = DetailsAction.ADD;
            tagToEdit = Optional.absent();
        } else {
            detailsAction = DetailsAction.EDIT;
            tagToEdit = Optional.of(localTagService.findById(tagId));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_tag_details, container, false);
        ButterKnife.inject(this, view);

        int tagColor;
        if (detailsAction == DetailsAction.EDIT) {
            tagName.setText(tagToEdit.get().getName());
            tagColor = tagToEdit.get().getColor();
        } else {
            tagColor = PrefUtils.getNextTagColor(getActivity());
        }
        colorPicker.setBackgroundColor(tagColor);
        colorPicker.setTag(tagColor);
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

    private void onSaveOptionSelected() {
        if (Strings.isNullOrEmpty(tagName.getText().toString())) {
            tagName.setError(getString(R.string.walletNameIsRequired));
        } else {
            tagName.setError(null);
        }

        if (tagName.getError() == null) {
            Tag tag = new Tag();
            tag.setName(tagName.getText().toString());
            tag.setColor((int) colorPicker.getTag());
            if (detailsAction == DetailsAction.ADD) {
                localTagService.insert(tag);
                getActivity().setResult(Activity.RESULT_OK);
            } else if (detailsAction == DetailsAction.EDIT) {
                tag.setId(tagToEdit.get().getId());
                localTagService.update(tag);
                getActivity().setResult(Activity.RESULT_OK);
            }
            getActivity().finish();
        }
    }

    @OnTextChanged(value = R.id.tagName, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    public void afterTagNameChanged(Editable s) {
        if (Strings.isNullOrEmpty(s.toString())) {
            if (tagNameLabel.getVisibility() == View.VISIBLE) {
                tagName.setError(getString(R.string.tagNameIsRequired));
            }
        } else {
            tagName.setError(null);
        }

        tagNameLabel.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.colorPicker)
    public void onColorPickerClicked() {
        final ColorPickerDialog colorPickerDialog = new ColorPickerDialog(getActivity(), (int) colorPicker.getTag());
        colorPickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int selectedColor = colorPickerDialog.getColor();
                colorPicker.setTag(selectedColor);
                colorPicker.setBackgroundColor(selectedColor);
            }
        });
        colorPickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        colorPickerDialog.show();
    }

    private enum DetailsAction {ADD, EDIT}
}
