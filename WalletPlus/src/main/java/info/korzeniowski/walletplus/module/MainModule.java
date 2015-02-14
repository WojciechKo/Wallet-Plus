package info.korzeniowski.walletplus.module;

import android.content.Context;

import com.google.gson.GsonBuilder;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import info.korzeniowski.walletplus.WalletPlus;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListActivity;
import info.korzeniowski.walletplus.ui.statistics.list.StatisticListActivityState;
import info.korzeniowski.walletplus.ui.wallets.details.WalletDetailsFragment;
import info.korzeniowski.walletplus.util.PrefUtils;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Module for common objects.
 */
@Module(
        includes = DatabaseModule.class,
        injects = {
                StatisticListActivity.class,

                WalletDetailsFragment.class
        }
)
public class MainModule {
    private final WalletPlus application;

    @Provides
    @Singleton
    Context provideContext() {
        return application;
    }

    public MainModule(WalletPlus application) {
        this.application = application;
    }

    @Provides
    @Named("amount")
    @Singleton
    NumberFormat provideNumberFormat() {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        return new DecimalFormat("0.00", symbols);
    }

    @Provides
    @Singleton
    StatisticListActivityState provideTagListActivityState() {
        return new StatisticListActivityState();
    }

    @Provides
    @Named("read")
    RestAdapter provideReadRestAdapter(final Context context) {
        return new RestAdapter.Builder()
                .setEndpoint("https://www.googleapis.com/drive/v2")
                .setConverter(new GsonConverter(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .setRequestInterceptor(new RequestInterceptor() {
                    @Override
                    public void intercept(RequestInterceptor.RequestFacade request) {
                        request.addQueryParam("access_token", PrefUtils.getGoogleToken(context));
                    }
                })
                .build();
    }

    @Provides
    @Named("upload")
    RestAdapter provideUploadRestAdapter(final Context context) {
        return new RestAdapter.Builder()
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setEndpoint("https://www.googleapis.com/upload/drive/v2")
                .setConverter(new GsonConverter(new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()))
                .build();
    }
}
