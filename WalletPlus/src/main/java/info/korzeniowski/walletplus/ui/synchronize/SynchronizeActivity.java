package info.korzeniowski.walletplus.ui.synchronize;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.sync.google.GoogleDriveReadService;
import info.korzeniowski.walletplus.sync.google.GoogleDriveUploadService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.util.PrefUtils;
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
    protected DrawerItemType getSelfNavDrawerItem() {
        return DrawerItemType.SYNCHRONIZE;
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
            ((WalletPlus) getActivity().getApplication()).component().inject(this);
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
            metadata.setTitle(activeProfile.getName() + ".db");
            metadata.setParentId("appfolder");
            TypedFile typedFile = new TypedFile("application/x-sqlite3", new File(activeProfile.getDatabaseFilePath()));
            googleDriveUploadService.insert(metadata, typedFile, "Bearer " + prefUtils.getGoogleToken(), new Callback<GoogleDriveUploadService.FileMetadata>() {
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

            TypedFile typedFile = new TypedFile("application/x-sqlite3", new File(activeProfile.getDatabaseFilePath()));
            googleDriveUploadService.update(activeProfile.getDriveId(), typedFile, "Bearer " + prefUtils.getGoogleToken(), new Callback<GoogleDriveUploadService.FileMetadata>() {
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
                                    .url(Uri.parse(driveFile.getDownloadUrl()).buildUpon().appendQueryParameter("access_token", prefUtils.getGoogleToken()).toString())
                                    .build();

                            try {
                                InputStream inputStream = okHttpClient.newCall(request).execute().body().byteStream();
                                OutputStream outputStream = new FileOutputStream(activeProfile.getDatabaseFilePath());
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
                        String mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                        Profile activeProfile = profileService.getActiveProfile();
                        profileService.update(activeProfile.setGmailAccount(mEmail));

                        // With the account name acquired, go get the auth token
                        if (mEmail == null) {
                            pickUserAccount();
                        } else {
                            if (isDeviceOnline()) {
                                new FetchGoogleToken(getActivity(), mEmail, SCOPE_PREFIX + SCOPE_APPDATA, prefUtils).execute();
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
            PrefUtils prefUtils;
            Activity mActivity;
            String mScope;
            String mEmail;

            FetchGoogleToken(Activity activity, String name, String scope, PrefUtils prefUtils) {
                this.mActivity = activity;
                this.mScope = scope;
                this.mEmail = name;
                this.prefUtils = prefUtils;
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    return fetchToken();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            protected String fetchToken() throws IOException {
                try {
                    return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);

                } catch (UserRecoverableAuthException userRecoverableException) {
                    Intent recoveryIntent = userRecoverableException.getIntent();
                    mActivity.startActivityForResult(recoveryIntent, RC_PICK_ACCOUNT);

                } catch (GoogleAuthException fatalException) {
                    fatalException.printStackTrace();

                }
                return null;
            }

            @Override
            protected void onPostExecute(String token) {
                prefUtils.setGoogleToken(token);
                Toast.makeText(mActivity, "Zalogowano do konta: " + mEmail, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
