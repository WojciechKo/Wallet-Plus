package pl.net.korzeniowski.walletplus.module;

import com.google.gson.GsonBuilder;

import dagger.Module;
import dagger.Provides;
import pl.net.korzeniowski.walletplus.sync.google.GoogleDriveReadService;
import pl.net.korzeniowski.walletplus.sync.google.GoogleDriveUploadService;
import pl.net.korzeniowski.walletplus.util.PrefUtils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

@Module
public class GoogleDriveRestModule {
    @Provides
    GoogleDriveReadService provideReadRestAdapter(final PrefUtils prefUtils) {
        return new RestAdapter.Builder()
                .setEndpoint("https://www.googleapis.com/drive/v2")
                .setConverter(new GsonConverter(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        request.addQueryParam("access_token", prefUtils.getGoogleToken());
                    }
                })
                .build()
                .create(GoogleDriveReadService.class);
    }

    @Provides
    GoogleDriveUploadService provideUploadRestAdapter() {
        return new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://www.googleapis.com/upload/drive/v2")
                .setConverter(new GsonConverter(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .build()
                .create(GoogleDriveUploadService.class);
    }
}
