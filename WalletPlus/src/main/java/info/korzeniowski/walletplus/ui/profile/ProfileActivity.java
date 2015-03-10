package info.korzeniowski.walletplus.ui.profile;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import butterknife.OnTextChanged;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.sync.google.GoogleDriveReadService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.util.KorzeniowskiUtils;
import info.korzeniowski.walletplus.util.PrefUtils;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

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

        @InjectView(R.id.remoteProfiles)
        ListView remoteProfiles;

        @Inject
        ProfileService profileService;

        @Inject
        PrefUtils prefUtils;

        @Inject
        @Named("read")
        RestAdapter googleDriveReadRestAdapter;

        private int lastPosition;
        private List<GoogleDriveReadService.DriveFile> profiles;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((WalletPlus) getActivity().getApplication()).component().inject(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_create_profile, container, false);
            ButterKnife.inject(this, view);
            enableListViewScrolling();
            profiles = Lists.newArrayList();
            remoteProfiles.setAdapter(new RemoteProfileAdapter(getActivity(), profiles));
            return view;
        }

        private void enableListViewScrolling() {
            // http://stackoverflow.com/a/25725568/2399340
            remoteProfiles.setOnTouchListener(new ListView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow ScrollView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow ScrollView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }

                    // Handle ListView touch events.
                    v.onTouchEvent(event);
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            final GoogleDriveReadService googleDriveReadService = googleDriveReadRestAdapter.create(GoogleDriveReadService.class);
            googleDriveReadService.getChildren("appfolder", new Callback<GoogleDriveReadService.FileChildren>() {
                @Override
                public void success(GoogleDriveReadService.FileChildren fileChildren, Response response) {
                    for (GoogleDriveReadService.FileChildren.FileId fileId : fileChildren.getChildren()) {
                        googleDriveReadService.getFile(fileId.getId(), new Callback<GoogleDriveReadService.DriveFile>() {
                            @Override
                            public void success(GoogleDriveReadService.DriveFile driveFile, Response response) {
                                profiles.add(driveFile);
                                ((BaseAdapter) remoteProfiles.getAdapter()).notifyDataSetChanged();
                            }

                            @Override
                            public void failure(RetrofitError error) {
                                Toast.makeText(getActivity(), "Error podczas pobierania informacji o dziecku", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Error pobierania dzieci:\n" + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @OnTextChanged(value = R.id.profile_name, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
        public void onProfileNameChanged() {
            profileName.setError(null);
        }

        @OnClick(R.id.create_local_profile)
        void onCreateLocalProfileClicked() {
            String name = profileName.getText().toString();
            Profile found = profileService.findByName(name);
            if (found == null) {
                Profile actualProfile = profileService.findById(prefUtils.getActiveProfileId());
                Profile profile = new Profile();
                profile.setName(name);
                profile.setAccount(actualProfile.getAccount());
                profileService.insert(profile);
                prefUtils.setActiveProfileId(profile.getId());
                getActivity().setResult(RESULT_OK);
                getActivity().finish();
            } else {
                profileName.setError("Profile name need to be unique");
            }
        }

        public static class RemoteProfileAdapter extends BaseAdapter {
            private Context context;
            private List<GoogleDriveReadService.DriveFile> profiles;

            public RemoteProfileAdapter(Context context, List<GoogleDriveReadService.DriveFile> profiles) {
                this.context = context;
                this.profiles = profiles;
            }

            @Override
            public int getCount() {
                return profiles.size();
            }

            @Override
            public GoogleDriveReadService.DriveFile getItem(int position) {
                return profiles.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = LayoutInflater.from(context).inflate(R.layout.item_profile_list_remote, parent, false);
                GoogleDriveReadService.DriveFile item = getItem(position);

                ((TextView) view.findViewById(R.id.name)).setText(item.getTitle());
                String fileSize = Formatter.formatFileSize(context, item.getFileSize());
                ((TextView) view.findViewById(R.id.size)).setText(fileSize);

                String createdDate =
                        android.text.format.DateFormat.getDateFormat(context).format(item.getCreatedDate())
                                + " "
                                + android.text.format.DateFormat.getTimeFormat(context).format(item.getCreatedDate());
                ((TextView) view.findViewById(R.id.created)).setText(createdDate);

                String modifiedDate =
                        android.text.format.DateFormat.getDateFormat(context).format(item.getModifiedDate())
                                + " "
                                + android.text.format.DateFormat.getTimeFormat(context).format(item.getModifiedDate());
                ((TextView) view.findViewById(R.id.modified)).setText(modifiedDate);
                return view;
            }
        }

        @OnItemClick(R.id.remoteProfiles)
        void onRemoteProfilesItemClicked(View view, int position) {
            remoteProfiles.setItemChecked(lastPosition, false);
            remoteProfiles.setItemChecked(position, true);
            lastPosition = position;
        }

        @OnClick(R.id.downloadProfile)
        void onDownloadProfileClicked() {
            final GoogleDriveReadService.DriveFile selectedProfile = (GoogleDriveReadService.DriveFile) remoteProfiles.getItemAtPosition(lastPosition);
            selectedProfile.getId();

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... params) {
                    OkHttpClient okHttpClient = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(Uri.parse(selectedProfile.getDownloadUrl()).buildUpon().appendQueryParameter("access_token", prefUtils.getGoogleToken()).toString())
                            .build();

                    try {
                        InputStream inputStream = okHttpClient.newCall(request).execute().body().byteStream();
                        Profile newProfile = new Profile()
                                .setName(KorzeniowskiUtils.Files.getBaseName(selectedProfile.getTitle()))
                                .setDriveId(selectedProfile.getId())
                                .setAccount(profileService.findById(prefUtils.getActiveProfileId()).getAccount());

                        profileService.insert(newProfile);
                        ByteStreams.copy(inputStream, new FileOutputStream(newProfile.getDatabaseFilePath()));
                        prefUtils.setActiveProfileId(newProfile.getId());
                        return true;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return false;
                }

                @Override
                protected void onPostExecute(Boolean successed) {
                    if (successed) {
                        getActivity().setResult(RESULT_OK);
                        getActivity().finish();
                    } else {
                        Toast.makeText(getActivity(), "Pobranie pliku z Profilem nie powiodło się.", Toast.LENGTH_SHORT).show();
                    }
                }
            }.execute();
        }
    }
}