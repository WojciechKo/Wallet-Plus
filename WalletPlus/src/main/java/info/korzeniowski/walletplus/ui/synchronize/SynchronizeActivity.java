package info.korzeniowski.walletplus.ui.synchronize;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.common.base.Strings;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import info.korzeniowski.walletplus.R;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.model.Profile;
import info.korzeniowski.walletplus.service.ProfileService;
import info.korzeniowski.walletplus.ui.BaseActivity;
import info.korzeniowski.walletplus.util.ProfileUtils;

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

    public static class SynchronizeFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
        private static final int RC_RESOLVE_CONNECTION = 1215;

        @InjectView(R.id.signInGoogle)
        SignInButton signInButton;

        @InjectView(R.id.createBackup)
        Button createBackup;

        @InjectView(R.id.uploadUpdate)
        Button uploadUpdate;

        @InjectView(R.id.downloadUpdate)
        Button downloadUpdate;

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
            View view = inflater.inflate(R.layout.fragment_synchronize, container, false);
            ButterKnife.inject(this, view);

            Profile profile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));
            setupViewss(!Strings.isNullOrEmpty(profile.getDriveId()));

            return view;
        }

        private void setupViewss(boolean isBackupCreated) {
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
            ensureIsConnecting();
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

        @OnClick(R.id.signInGoogle)
        void onSignInGoogleClicked() {

        }

        @OnClick(R.id.createBackup)
        void onCreateBackupClicked() {
            final ResultCallback<DriveFolder.DriveFileResult> fileCallback = new ResultCallback<DriveFolder.DriveFileResult>() {
                @Override
                public void onResult(DriveFolder.DriveFileResult result) {
                    if (!result.getStatus().isSuccess()) {
                        Toast.makeText(getActivity(), "Error while trying to create the file", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Profile actualProfile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));
                    actualProfile.setDriveId(result.getDriveFile().getDriveId().encodeToString());
                    localProfileService.update(actualProfile);
                    setupViewss(true);
                    Toast.makeText(getActivity(), "Created a file in App Folder: " + result.getDriveFile().getDriveId(), Toast.LENGTH_SHORT).show();
                }
            };

            final ResultCallback<DriveApi.DriveContentsResult> driveContentsReceived = new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Toast.makeText(getActivity(), "Error while trying to create new file contents", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final DriveContents driveContents = driveContentsResult.getDriveContents();
                    new Thread() {
                        @Override
                        public void run() {

                            Profile activeProfile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));

                            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                    .setTitle(activeProfile.getDatabaseFilePath())
                                    .setMimeType("application/x-sqlite3")
                                    .build();

                            File database = new File(activeProfile.getDatabaseFilePath());
                            try {
                                driveContents.getOutputStream().write(Files.toByteArray(database));
                            } catch (IOException e) {
                                throw new RuntimeException("Nie znaleziono pliku bazy", e);
                            }

                            Drive.DriveApi.getAppFolder(mGoogleApiClient)
                                    .createFile(mGoogleApiClient, changeSet, driveContents)
                                    .setResultCallback(fileCallback);
                        }
                    }.start();
                }
            };
            ensureIsConnecting();

            Drive.DriveApi.newDriveContents(mGoogleApiClient).setResultCallback(driveContentsReceived);
        }

        @OnClick(R.id.uploadUpdate)
        void onUploadUpdateClicked() {
            DriveFile.DownloadProgressListener downloadProgressListener = new DriveFile.DownloadProgressListener() {
                @Override
                public void onProgress(long bytesDownloaded, long bytesExpected) {

                }
            };

            ResultCallback<DriveApi.DriveContentsResult> resultCallback = new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult driveContentsResult) {
                    if (!driveContentsResult.getStatus().isSuccess()) {
                        Toast.makeText(getActivity(), "Error while trying to create new file contents", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    final DriveContents driveContents = driveContentsResult.getDriveContents();

                    Profile activeProfile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));
                    File database = new File(activeProfile.getDatabaseFilePath());
                    try {
                        driveContents.getOutputStream().write(Files.toByteArray(database));
                    } catch (IOException e) {
                        throw new RuntimeException("Nie znaleziono pliku bazy", e);
                    }
                    driveContents.commit(mGoogleApiClient, null);
                }
            };

            Profile actualProfile = localProfileService.findById(ProfileUtils.getActiveProfileId(getActivity()));
            Drive.DriveApi.getFile(mGoogleApiClient, DriveId.decodeFromString(actualProfile.getDriveId())).open(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, downloadProgressListener).setResultCallback(resultCallback);
        }
        private void ensureIsConnecting() {
            if (!mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }

        @Override
        public void onConnected(Bundle bundle) {

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
