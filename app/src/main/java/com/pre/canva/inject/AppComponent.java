package com.pre.canva.inject;

import com.pre.canva.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Pre on 15/09/2017.
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity activity);
}
