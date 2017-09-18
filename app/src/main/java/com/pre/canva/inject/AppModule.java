package com.pre.canva.inject;

import android.content.Context;
import android.content.SharedPreferences;

import com.pre.canva.application.CanvaApplication;
import com.pre.canva.application.PrefKeys;
import com.pre.canva.network.WebAgent;
import com.pre.canva.usecase.DecodeBitmapUseCase;
import com.pre.canva.usecase.MosaicUseCase;

import org.apache.commons.validator.routines.InetAddressValidator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Pre on 15/09/2017.
 */
@Module
public class AppModule {
    private CanvaApplication app;

    public AppModule(CanvaApplication app) {
        this.app = app;
    }

    /**
     * default context is set to application
     * @return
     */
    @Provides
    @Singleton
    DecodeBitmapUseCase.Builder providesDecodeBitmapBuilder() {
        return new DecodeBitmapUseCase
                .Builder()
                .setContext(app);
    }

    @Provides
    @Singleton
    MosaicUseCase.Builder providesMosaicBuilder(WebAgent webAgent) {
        return new MosaicUseCase
                .Builder()
                .setWebAgent(webAgent);
    }

    @Provides
    @Singleton
    SharedPreferences getSharedPreference() {
        return app.getSharedPreferences(PrefKeys.GENERAL_PREF_NAME, Context.MODE_PRIVATE);
    }

    @Provides
    @Singleton
    SharedPreferences.Editor getSharedPreferenceEditor(SharedPreferences sharedPreferences) {
        return sharedPreferences.edit();
    }

    @Provides
    @Singleton
    WebAgent providesWebAgent() {
        String host = getSharedPreference().getString(PrefKeys.HOST_IP, PrefKeys.DEFAULT_HOST_IP);
        return new WebAgent("http://" + host + ":8765/");
    }

    @Provides
    @Singleton
    InetAddressValidator providesIPValidator() {
        return InetAddressValidator.getInstance();
    }
}
