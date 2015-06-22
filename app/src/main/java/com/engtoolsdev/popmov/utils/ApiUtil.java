package com.engtoolsdev.popmov.utils;

import android.widget.Toast;

import java.net.UnknownHostException;

import javax.net.ssl.SSLHandshakeException;

import retrofit.ErrorHandler;
import retrofit.RestAdapter;
import retrofit.RetrofitError;

/**
 * Created by Jose on 6/20/15.
 */
public class ApiUtil {

    public static Api getApi(){
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Api.API_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        return restAdapter.create(Api.class);
    }
}
