package com.pre.canva.application;

import android.app.Application;

import com.pre.canva.inject.AppComponent;
import com.pre.canva.inject.AppModule;
import com.pre.canva.inject.DaggerAppComponent;
import com.squareup.leakcanary.LeakCanary;

/**
 * Created by Pre on 14/06/2017.
 */

public class CanvaApplication extends Application {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        initDependencyInjection();

        //Leak Canary install
        if (!LeakCanary.isInAnalyzerProcess(this)) {
            LeakCanary.install(this);
        }
    }

    private void initDependencyInjection() {
        appComponent =
                DaggerAppComponent
                        .builder()
                        .appModule(new AppModule(this))
                        .build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
