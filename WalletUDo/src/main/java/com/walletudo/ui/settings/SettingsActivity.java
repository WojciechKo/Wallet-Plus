package com.walletudo.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.walletudo.R;
import com.walletudo.WalletUDo;
import com.walletudo.model.Profile;
import com.walletudo.service.ProfileService;
import com.walletudo.ui.BaseActivity;
import com.walletudo.util.AndroidUtils;

import javax.inject.Inject;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = NavUtils.getParentActivityIntent(SettingsActivity.this);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                NavUtils.navigateUpTo(SettingsActivity.this, intent);            }
        });
    }

    public static class SettingsFragment extends Fragment {

        @Inject
        ProfileService profileService;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((WalletUDo) getActivity().getApplication()).component().inject(this);
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            super.onCreateView(inflater, container, savedInstanceState);
            LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_settings, container, false);
            inflateDeleteProfile(view);
            return view;
        }

        private View inflateDeleteProfile(ViewGroup root) {
            View view = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_2, root);
            TextView title = (TextView) view.findViewById(android.R.id.text1);
            title.setText("Delete profile");

            final Profile activeProfile = profileService.getActiveProfile();

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new MaterialDialog.Builder(getActivity())
                            .title("Type in the name of actual profile (" + activeProfile.getName() + ") to delete.")
                            .positiveText(R.string.ok)
                            .negativeText(R.string.cancel)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    //TODO: implementation here!
                                    Toast.makeText(getActivity(), "Usunięty!", Toast.LENGTH_SHORT).show();
                                    profileService.deleteById(activeProfile.getId());
                                    AndroidUtils.restartApplication(getActivity());
//                                    dialog.dismiss();
                                }

                                @Override
                                public void onNegative(MaterialDialog dialog) {
                                    dialog.dismiss();
                                }
                            })
                            .input("profile name", "", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog materialDialog, CharSequence charSequence) {
                                    View positiveButton = materialDialog.getActionButton(DialogAction.POSITIVE);
                                    positiveButton.setEnabled(isValid(charSequence));
                                }

                                private boolean isValid(CharSequence charSequence) {
                                    return charSequence.toString().equals(activeProfile.getName());
                                }
                            })
                            .alwaysCallInputCallback()
                            .autoDismiss(false)
                            .show();

                }
            });
            return view;
        }
    }
}
