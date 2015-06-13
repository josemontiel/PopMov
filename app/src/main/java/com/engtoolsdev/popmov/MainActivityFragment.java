package com.engtoolsdev.popmov;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.engtoolsdev.popmov.adapters.MovieGridAdapter;
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.utils.Api;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String MOVIE_ITEMS = "movie_items";
    private static final int POPULARITY_SORT = 0;
    private static final int RATING_SORT = 1;
    private static final String SORTING_CRITERIA_EXTRA = "sorting_criteria";
    int sortingCriteria;

    RecyclerView recyclerView;
    FrameLayout progressView;
    SwipeRefreshLayout swipeRefreshLayout;

    ArrayList<Movie> movieItems = new ArrayList<>();
    private MovieGridAdapter adapter;

    public MainActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        progressView = (FrameLayout)  view.findViewById(R.id.progress_layout);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        sortingCriteria = sharedPreferences.getInt(SORTING_CRITERIA_EXTRA, POPULARITY_SORT);

        setSortingTitle();

        setUpRecyclerView();

        if(savedInstanceState == null) {
            refreshMovies();

        }else{
            if(savedInstanceState.containsKey(MOVIE_ITEMS)) {
                ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(MOVIE_ITEMS);
                movieItems.addAll(movies);
                adapter.notifyDataSetChanged();

                showProgress(false);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.menu_sort, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {


        int id = item.getItemId();

        if(id == R.id.action_sort){
            changeSorting();

            sort(sortingCriteria);

            setSortingTitle();
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Changes the Sorting Criteria and Stores it in the app's {@link SharedPreferences}
     */
    private void changeSorting() {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (sortingCriteria){
            case POPULARITY_SORT:
                sortingCriteria = RATING_SORT;
                editor.putInt("sorting_criteria", RATING_SORT);
                break;
            case RATING_SORT:
                sortingCriteria = POPULARITY_SORT;
                editor.putInt("sorting_criteria", POPULARITY_SORT);
                break;
        }
        editor.apply();
    }

    /**
     * Calls the API using {@link RestAdapter}
     */
    private void refreshMovies() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Api.API_ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        Api api = restAdapter.create(Api.class);


        api.fetchMovies(getString(R.string.popularity_desc), getString(R.string.api_key), new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

                ArrayList<Movie> results = new ArrayList<>();
                try {
                    JsonReader jsonReader = new JsonReader(new InputStreamReader(response.getBody().in(), getString(R.string.utf_8)));
                    jsonReader.beginObject();
                    while (jsonReader.hasNext()) {
                        String key = jsonReader.nextName();
                        switch (key) {
                            case "results":
                                results.addAll(readResults(jsonReader));
                                break;
                            default:
                                jsonReader.skipValue();
                                break;
                        }
                    }
                    jsonReader.endObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                movieItems.clear();
                movieItems.addAll(results);

                //Sorts the movie items
                sort(sortingCriteria);

                //We hide the progress bar
                showProgress(false);

                //We hide the refresh widget in case it was manually refreshed
                swipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void failure(RetrofitError error) {
                Timber.d(error.getMessage());
            }
        });
    }

    /**
     * Reads the "results" object in the API's response
     * @param jsonReader {@link JsonReader}
     * @return an ArrayList of Movie items to be displayed
     * @throws IOException raised in case of malformed JSON or there is an error while reading
     */
    private ArrayList<Movie> readResults(JsonReader jsonReader) throws IOException{

        ArrayList<Movie> results = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()){
            results.add(readMovie(jsonReader));
        }
        jsonReader.endArray();

        return results;
    }

    /**
     * Parses a {@link Movie} object from the API's response
     * @param jsonReader {@link JsonReader}
     * @return a Movie item to be displayed
     * @throws IOException raised in case of malformed JSON or there is an error while reading
     */
    private Movie readMovie(JsonReader jsonReader) throws IOException{

        String title = null;
        String imageReference = null;
        String overview = null;
        double rating = 0.0;
        double voteAverage = 0.0;
        long voteCount = 0;
        String releaseDate = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String key = jsonReader.nextName();
            switch (key){
                case "original_title":
                    title = jsonReader.nextString();
                    break;
                case "poster_path":
                    imageReference = jsonReader.nextString();
                    break;
                case "overview":
                    overview = jsonReader.nextString();
                    break;
                case "popularity":
                    rating = jsonReader.nextDouble();
                    break;
                case "release_date":
                    releaseDate = jsonReader.nextString();
                    break;
                case "vote_average":
                    voteAverage = jsonReader.nextDouble();
                    break;
                case "vote_count":
                    voteCount = jsonReader.nextLong();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return new Movie(title, imageReference, overview, rating, voteAverage, voteCount, releaseDate);
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(MOVIE_ITEMS, movieItems);
    }

    /**
     * Sets up UI Elements
     */
    private void setUpRecyclerView() {

        adapter = new MovieGridAdapter(getActivity(), movieItems);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(),getResources().getInteger(R.integer.row_items)));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        swipeRefreshLayout.setOnRefreshListener(this);

    }

    /**
     * Hides/Show {@link android.widget.ProgressBar}
     * @param show boolean, determine wether to show or not {@link android.widget.ProgressBar}
     */
    private void showProgress(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;

        progressView.setVisibility(visibility);
    }



    @Override
    public void onRefresh() {
        showProgress(true);
        refreshMovies();
    }

    /**
     * Sorts the Motie items and notifies the {@link MovieGridAdapter}
     * @param criteria criteria of sorting
     */

    private void sort(int criteria) {

        Comparator<Movie> sortComparator = null;

        if (criteria == POPULARITY_SORT) {
            sortComparator = new Comparator<Movie>() {
                @Override
                public int compare(Movie lhs, Movie rhs) {
                    if(lhs.getRating() < rhs.getRating()) return 1;
                    if(lhs.getRating() > rhs.getRating()) return -1;
                    return 0;
                }
            };
        }else{
            sortComparator = new Comparator<Movie>() {
                @Override
                public int compare(Movie lhs, Movie rhs) {
                    if(lhs.getVoteAverage() < rhs.getVoteAverage()) return 1;
                    if(lhs.getVoteAverage() > rhs.getVoteAverage()) return -1;
                    return 0;
                }
            };
        }

        Collections.sort(movieItems, sortComparator);

        adapter.notifyDataSetChanged();
    }

    /**
     * Changes the {@link android.support.v7.widget.Toolbar} title depending on the sorting criteria selected
     */

    public void setSortingTitle(){
        switch (sortingCriteria){
            case POPULARITY_SORT:
                getActivity().setTitle(getString(R.string.most_popular_title));
                break;
            case RATING_SORT:
                getActivity().setTitle(getString(R.string.highest_rated_title));
                break;
        }
    }
}
