package com.engtoolsdev.popmov.utils;

import com.engtoolsdev.popmov.models.Movie;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Jose on 6/6/15.
 */
public interface Api {

    String API_ENDPOINT = "htt" +
            "p://api.themoviedb.org";

    @GET("/3/discover/movie")
    void fetchMovies(@Query("sort_by") String sortCriteria, @Query("api_key") String apiKey, Callback<Response> cb);

}
