package com.walletudo.ui.synchronize;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.walletudo.R;
import com.walletudo.Walletudo;
import com.walletudo.google.GoogleDriveReadService;
import com.walletudo.google.GoogleDriveUploadService;
import com.walletudo.model.Profile;
import com.walletudo.service.ProfileService;
import com.walletudo.ui.BaseActivity;
import com.walletudo.ui.NavigationDrawerHelper;
import com.walletudo.util.PrefUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class SynchronizeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isFinishing()) {
            return;
        }

        setContentView(R.layout.activity_drawer);

        if (null == savedInstanceState) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new SynchronizeFragment())
                    .commit();
        }

        overridePendingTransition(0, 0);
    }

    @Override
    protected NavigationDrawerHelper.DrawerItemType getSelfNavDrawerItem() {
        return NavigationDrawerHelper.DrawerItemType.SYNCHRONIZE;
    }

    public static class SynchronizeFragment extends Fragment {
        static final int RC_PICK_ACCOUNT = 6955;
        static final String SCOPE_APPDATA = "https://www.googleapis.com/auth/drive.appdata";
        static final String SCOPE_PREFIX = "oauth2:";

        @InjectView(R.id.createBackup)
        Button createBackup;

        @InjectView(R.id.uploadUpdate)
        Button uploadUpdate;

        @InjectView(R.id.downloadUpdate)
        Button downloadUpdate;

        @Inject
        ProfileService profileService;

        @Inject
        PrefUtils prefUtils;

        @Inject
        GoogleDriveReadService googleDriveReadService;

        @Inject
        GoogleDriveUploadService googleDriveUploadService;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ((Walletudo) getActivity().getApplication()).component().inject(this);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_synchronize, container, false);
            ButterKnife.inject(this, view);

            Profile profile = profileService.getActiveProfile();
            setupVisibility(!Strings.isNullOrEmpty(profile.getDriveId()));

            return view;
        }

        private void setupVisibility(boolean isBackupCreated) {
            if (isBackupCreated) {
                createBackup.setVisibility(View.INVISIBLE);
                uploadUpdate.setVisibility(View.VISIBLE);
                downloadUpdate.setVisibility(View.VISIBLE);
            } else {
                createBackup.setVisibility(View.VISIBLE);
                uploadUpdate.setVisibility(View.INVISIBLE);
                downloadUpdate.setVisibility(View.INVISIBLE);
            }
        }

        @OnClick(R.id.signInGoogle)
        void signInGoogle() {
            pickUserAccount();
        }

        private void pickUserAccount() {
            String[] accountTypes = new String[]{"com.google"};
            Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                    accountTypes, false, null, null, null, null);
            startActivityForResult(intent, RC_PICK_ACCOUNT);
        }

        @OnClick(R.id.createBackup)
        void onCreateBackupClicked() {
            final Profile activeProfile = profileService.getActiveProfile();
            GoogleDriveUploadService.FileMetadata metadata = new GoogleDriveUploadService.FileMetadata();
            metadata.setTitle(activeProfile.getName());
            metadata.setParentId("appfolder");
            File databaseFile = getActivity().getDatabasePath(activeProfile.getDatabaseFileName());
            TypedFile typedFile = new TypedFile("application/x-sqlite3", databaseFile);
            googleDriveUploadService.insert(metadata, typedFile, "Bearer " + activeProfile.getGoogleToken(), new Callback<GoogleDriveUploadService.FileMetadata>() {
                @Override
                public void success(GoogleDriveUploadService.FileMetadata metadata, Response response) {
                    activeProfile.setDriveId(metadata.getId());
                    profileService.update(activeProfile);
                    Toast.makeText(getActivity(), "Plik z bazą danych został utworzony na serwerze.", Toast.LENGTH_SHORT).show();
                    setupVisibility(true);
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Nie udało się przesłać pliku na serwer.\n" + error.toString(), Toast.LENGTH_SHORT).show();

                }
            });
        }

        @OnClick(R.id.uploadUpdate)
        void onUploadUpdateClicked() {
            final Profile activeProfile = profileService.getActiveProfile();
            File databaseFile = getActivity().getDatabasePath(activeProfile.getDatabaseFileName());
            TypedFile typedFile = new TypedFile("application/x-sqlite3", databaseFile);
            googleDriveUploadService.update(activeProfile.getDriveId(), typedFile, "Bearer " + activeProfile.getGoogleToken(), new Callback<GoogleDriveUploadService.FileMetadata>() {
                @Override
                public void success(GoogleDriveUploadService.FileMetadata metadata, Response response) {
                    Toast.makeText(getActivity(), "Profil został uaktualniony na serwerze.", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Uaktualnienie Profilu na serwerze nie powiodło się.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @OnClick(R.id.downloadUpdate)
        void onDownloadUpdateClicked() {
            final Profile activeProfile = profileService.getActiveProfile();

            googleDriveReadService.getFile(activeProfile.getDriveId(), new Callback<GoogleDriveReadService.DriveFile>() {
                @Override
                public void success(final GoogleDriveReadService.DriveFile driveFile, Response response) {
                    new AsyncTask<Void, Void, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Void... params) {
                            OkHttpClient okHttpClient = new OkHttpClient();
                            Request request = new Request.Builder()
                                    .url(Uri.parse(driveFile.getDownloadUrl()).buildUpon().appendQueryParameter("access_token", activeProfile.getGoogleToken()).toString())
                                    .build();

                            try {
                                InputStream inputStream = okHttpClient.newCall(request).execute().body().byteStream();
                                File databaseFile = getActivity().getDatabasePath(activeProfile.getDatabaseFileName());
                                OutputStream outputStream = new FileOutputStream(databaseFile);
                                ByteStreams.copy(inputStream, outputStream);

                                return true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return false;
                        }

                        @Override
                        protected void onPostExecute(Boolean successed) {
                            if (successed) {
                                Toast.makeText(getActivity(), "Profil lokalny zosatał uaktualniony.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity(), "Pobranie pliku z Profilem nie powiodło się.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }.execute();
                }

                @Override
                public void failure(RetrofitError error) {
                    Toast.makeText(getActivity(), "Pobranie informacji o pliku z Profilem nie powiodło się.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case RC_PICK_ACCOUNT:
                    // Receiving a result from the AccountPicker
                    if (resultCode == RESULT_OK) {
                        String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        Profile activeProfile = profileService.getActiveProfile();
                        profileService.update(activeProfile.setGoogleAccount(email));

                        // With the account name acquired, go get the auth token
                        if (email == null) {
                            pickUserAccount();
                        } else {
                            if (isDeviceOnline()) {
                                new FetchGoogleToken(getActivity(), email, SCOPE_PREFIX + SCOPE_APPDATA, profileService).execute();
                            } else {
                                Toast.makeText(getActivity(), "Brak połączenia z internetem.", Toast.LENGTH_LONG).show();
                            }
                        }
                    } else if (resultCode == RESULT_CANCELED) {
                        // The account picker dialog closed without selecting an account.
                        // Notify users that they must pick an account to proceed.
                        Toast.makeText(getActivity(), "Wybierz konto", Toast.LENGTH_SHORT).show();
                    }
            }
        }

        private boolean isDeviceOnline() {
            ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        }

        public static class FetchGoogleToken extends AsyncTask<Void, Void, String> {
            private Activity activity;
            private String scope;
            private String email;
            private ProfileService profileService;

            FetchGoogleToken(Activity activity, String email, String scope, ProfileService profileService) {
                this.activity = activity;
                this.email = email;
                this.scope = scope;
                this.profileService = profileService;
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return GoogleAuthUtil.getToken(activity, email, scope);
                } catch (UserRecoverableAuthException userRecoverableException) {
                    Intent recoveryIntent = userRecoverableException.getIntent();
                    activity.startActivityForResult(recoveryIntent, RC_PICK_ACCOUNT);
                } catch (GoogleAuthException fatalException) {
                    fatalException.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String token) {
                if (!Strings.isNullOrEmpty(token)) {
                    Profile activeProfile = profileService.getActiveProfile();
                    profileService.update(activeProfile.setGoogleToken(token));
                    Toast.makeText(activity, "Zalogowano do konta: " + email, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(activity, "Can't obtain google token", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
