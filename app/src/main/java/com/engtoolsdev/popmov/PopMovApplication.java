package com.engtoolsdev.popmov;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by Jose on 6/6/15.
 */
public class PopMovApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

    }
}
