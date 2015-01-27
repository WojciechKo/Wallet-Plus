package info.korzeniowski.walletplus.ui.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.util.ProfileUtils;

public class ProfileActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new CreateProfileFragment())
                    .commit();
        }

        getActionBarToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public static class CreateProfileFragment extends Fragment {

        @InjectView(R.id.profile_name)
        EditText profileName;

        @Inject
        @Named("local")
        ProfileService localProfileService;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((WalletPlus) getActivity().getApplication()).inject(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_create_profile, container, false);
            ButterKnife.inject(this, view);
            return view;
        }

        @OnTextChanged(value = R.id.profile_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        public void onProfileNameChanged() {
            profileName.setError(null);
        }

        @OnClick(R.id.create_local_profile)
        void onCreateLocalProfileClicked() {
            String name = profileName.getText().toString();
            Profile found = localProfileService.findByName(name);
            if (found == null) {
                Profile actualProfile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));
                Profile profile = new Profile();
                profile.setName(name);
                profile.setAccount(actualProfile.getAccount());
                localProfileService.insert(profile);
                ProfileUtils.setActiveProfileId(getActivity(), profile.getId());
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            } else {
                profileName.setError("Profile name need to be unique");
            }
        }
    }
}
