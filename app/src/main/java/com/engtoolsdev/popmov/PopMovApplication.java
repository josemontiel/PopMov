package com.engtoolsdev.popmov;

import android.app.Application;

import com.engtoolsdev.popmov.sql.DatabaseHelper;
import com.engtoolsdev.popmov.sql.DatabaseManager;

import timber.log.Timber;

/**
 * Created by Jose on 6/6/15.
 */
public class PopMovApplication extends Application{

    @Override
    public void onCreate() {
        super.onCreate();

        //We make sure to not print logs on release build
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        //Initializes Database Manager
        DatabaseManager.initializeInstance(new DatabaseHelper(getApplicationContext()));

    }
}
