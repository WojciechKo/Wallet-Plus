package info.korzeniowski.walletplus.ui.profile;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

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

    public static class CreateProfileFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private static final int RC_RESOLVE_CONNECTION = 1217;

        @InjectView(R.id.profile_name)
        EditText profileName;

        @InjectView(R.id.remoteProfiles)
        ListView remoteProfiles;

        @Inject
        @Named("local")
        ProfileService localProfileService;

        private GoogleApiClient mGoogleApiClient;

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

        @Override
        public void onResume() {
            super.onResume();
            if (mGoogleApiClient == null) {
                mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                        .addApi(Drive.API)
                        .addScope(Drive.SCOPE_APPFOLDER)
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
            mGoogleApiClient.connect();
        }

        @Override
        public void onPause() {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
            super.onPause();
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case RC_RESOLVE_CONNECTION:
                    if (resultCode == RESULT_OK) {
                        mGoogleApiClient.connect();
                    }
            }
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

        @Override
        public void onConnected(Bundle bundle) {
            ResultCallback<DriveApi.MetadataBufferResult> resultCallback = new ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult metadataBufferResult) {
                    if (!metadataBufferResult.getStatus().isSuccess()) {
                        Toast.makeText(getActivity(), "Error while trying to create new file contents", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final MetadataBuffer metadataBuffer = metadataBufferResult.getMetadataBuffer();
                    Iterator<Metadata> metadataIterator = metadataBuffer.iterator();
                    List<String> files = Lists.newArrayListWithCapacity(metadataBuffer.getCount());
                    while (metadataIterator.hasNext()) {
                        files.add(metadataIterator.next().getTitle());
                    }
                    remoteProfiles.setAdapter(new BaseAdapter() {
                        @Override
                        public int getCount() {
                            return metadataBuffer.getCount();
                        }

                        @Override
                        public Metadata getItem(int position) {
                            return metadataBuffer.get(position);
                        }

                        @Override
                        public long getItemId(int position) {
                            return position;
                        }

                        @Override
                        public View getView(int position, View convertView, ViewGroup parent) {
                            View view = getActivity().getLayoutInflater().inflate(R.layout.item_profile_list_remote, parent, false);
                            Metadata item = getItem(position);

                            ((TextView) view.findViewById(R.id.name)).setText(item.getTitle());
                            String fileSize = Formatter.formatFileSize(getActivity(), item.getFileSize());
                            ((TextView) view.findViewById(R.id.size)).setText(fileSize);

                            String createdDate =
                                    android.text.format.DateFormat.getDateFormat(getActivity()).format(item.getCreatedDate())
                                            + " "
                                            + android.text.format.DateFormat.getTimeFormat(getActivity()).format(item.getCreatedDate());
                            ((TextView) view.findViewById(R.id.created)).setText(createdDate);

                            String modifiedDate =
                                    android.text.format.DateFormat.getDateFormat(getActivity()).format(item.getModifiedDate())
                                            + " "
                                            + android.text.format.DateFormat.getTimeFormat(getActivity()).format(item.getModifiedDate());
                            ((TextView) view.findViewById(R.id.modified)).setText(modifiedDate);
                            return view;
                        }
                    });
//                    remoteProfiles.setAdapter(new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, files));
                }
            };

            Drive.DriveApi.getAppFolder(mGoogleApiClient).listChildren(mGoogleApiClient).setResultCallback(resultCallback);
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(getActivity(), RC_RESOLVE_CONNECTION);
                } catch (IntentSender.SendIntentException e) {
                    // Unable to resolve, message user appropriately
                }
            } else {
                GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), getActivity(), 0).show();
            }
        }
    }
}
