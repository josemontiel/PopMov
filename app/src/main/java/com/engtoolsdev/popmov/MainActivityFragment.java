package com.engtoolsdev.popmov;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;

import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.engtoolsdev.popmov.adapters.MovieGridAdapter;
import com.engtoolsdev.popmov.contentprovider.PopMovContentProvider;
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.sql.contracts.FavoriteContract;
import com.engtoolsdev.popmov.utils.Api;
import com.engtoolsdev.popmov.utils.ApiUtil;
import com.engtoolsdev.popmov.utils.SubscriptionCache;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import retrofit.RestAdapter;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    private static final String KEY_SUBSCRIPTION_CACHE = "refresh_movie_list";

    private static final String MOVIE_ITEMS = "movie_items";
    private static final int POPULARITY_SORT = 0;
    private static final int RATING_SORT = 1;
    private static final String SORTING_CRITERIA_EXTRA = "sorting_criteria";

    private boolean multiPane;

    int sortingCriteria;

    RecyclerView recyclerView;
    FrameLayout progressView;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView titleView;

    ArrayList<Movie> movieItems = new ArrayList<>();
    ArrayList<Movie> movieItemsDatabase = new ArrayList<>();
    private MovieGridAdapter adapter;

    private Action1<ArrayList<Movie>> refreshAction = new Action1<ArrayList<Movie>>() {
        @Override
        public void call(ArrayList<Movie> movies) {
            movieItems.clear();
            movieItems.addAll(movies);

            //Sorts the movie items
            sort(sortingCriteria);



            //We hide the progress bar
            showProgress(false);

            //We hide the refresh widget in case it was manually refreshed
            swipeRefreshLayout.setRefreshing(false);



            SubscriptionCache.getInstance().remove(KEY_SUBSCRIPTION_CACHE);
        }
    };

    public MainActivityFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        multiPane = getResources().getBoolean(R.bool.multi_pane);

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

        titleView = ((MainActivity) getActivity()).titleView;

        SwitchCompat faveSwitch = ((MainActivity) getActivity()).faveSwitch;
        faveSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                sortFaves(isChecked);
            }
        });

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
            }else {
                refreshMovies();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();

        if(SubscriptionCache.getInstance().contains(KEY_SUBSCRIPTION_CACHE)) {
            Observable<ArrayList<Movie>> request = (Observable<ArrayList<Movie>>) SubscriptionCache.getInstance().get(KEY_SUBSCRIPTION_CACHE);
            request.subscribe(refreshAction);
        }

        setFavorites(movieItems);
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
    @SuppressWarnings("unchecked")
    private void refreshMovies() {

        Api api = ApiUtil.getApi();

        Observable<ArrayList<Movie>> request = null;

        if(!SubscriptionCache.getInstance().contains(KEY_SUBSCRIPTION_CACHE)) {

            //INTRODUCE YOUR OWN API KEY --->>
            request = AppObservable.bindFragment(this, api.fetchMovies(getString(R.string.popularity_desc), getString(R.string.api_key)))
                    .map(new Func1<Response, ArrayList<Movie>>() {
                        @Override
                        public ArrayList<Movie> call(Response response) {
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

                            setFavorites(results);

                            return results;
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, ArrayList<Movie>>() {
                        @Override
                        public ArrayList<Movie> call(Throwable throwable) {
                            return loadOfflineFavorites();
                        }
                    })
                    .cache();

            SubscriptionCache.getInstance().put(KEY_SUBSCRIPTION_CACHE, request);
        }else{
            request = (Observable<ArrayList<Movie>>) SubscriptionCache.getInstance().get(KEY_SUBSCRIPTION_CACHE);
        }


        request.subscribe(refreshAction);

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

        long id = 0;
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
                case "id":
                    id = jsonReader.nextLong();
                    break;
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

        return new Movie(id, title, imageReference, overview, rating, voteAverage, voteCount, releaseDate);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(movieItems.size() != 0) {
            outState.putParcelableArrayList(MOVIE_ITEMS, movieItems);
        }
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

        Comparator<Movie> sortComparator;

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
     * Filters the Movies by Favorites
     * @param showFaves show or not favorites
     */

    private void sortFaves(boolean showFaves) {

        if(showFaves) {
            movieItemsDatabase = new ArrayList<>(movieItems);

            for (Movie movie : movieItemsDatabase) {
                if (!movie.isFavorite()) {
                    movieItems.remove(movie);
                }
            }
        }else{
            if(movieItemsDatabase != null){
                movieItems.clear();
                movieItems.addAll(movieItemsDatabase);

                movieItemsDatabase = null;
            }
        }

        sort(sortingCriteria);
    }


    /**
     * Changes the {@link android.support.v7.widget.Toolbar} title depending on the sorting criteria selected
     */

    public void setSortingTitle(){

        switch (sortingCriteria) {
            case POPULARITY_SORT:
                titleView.setText(R.string.most_popular_title);
                break;
            case RATING_SORT:
                titleView.setText(R.string.highest_rated_title);
                break;
        }

    }

    public void loadMovie(Movie movie){
        if(!multiPane){
            Intent intent = new Intent(getActivity(), DetailActivity.class);
            intent.putExtra("movie", movie);
            startActivity(intent);
        }else{
            DetailFragment detailFragment = (DetailFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_right);
            detailFragment.setMovie(movie);
        }
    }

    public void setFavorites(ArrayList<Movie> movies){

        for(Movie movie : movies) {

            Cursor cursor= getActivity().getContentResolver().query(PopMovContentProvider.CONTENT_URI, new String[]{FavoriteContract.COLUMN_MOVIE_ID}, FavoriteContract.COLUMN_MOVIE_ID + " = ?", new String[]{String.valueOf(movie.getId())}, null);

            if (cursor.moveToFirst()) {
                movie.setIsFavorite(cursor.getCount() == 1);
            }

            cursor.close();

        }

    }


    /**
     * Loads favorites stored in DB. Used in case user is offline or API returns 0 items
     */
    public ArrayList<Movie> loadOfflineFavorites(){

        ArrayList<Movie> movies = new ArrayList<>();

        String[] projection = new String[]{
                FavoriteContract.COLUMN_MOVIE_ID,
                FavoriteContract.COLUMN_TITLE,
                FavoriteContract.COLUMN_IMAGE_ID,
                FavoriteContract.COLUMN_OVERVIEW,
                FavoriteContract.COLUMN_RATING,
                FavoriteContract.COLUMN_VOTE_AVG,
                FavoriteContract.COLUMN_VOTE_COUNT,
                FavoriteContract.COLUMN_RELEASE_DATE
        };

        Cursor cursor = getActivity().getContentResolver().query(PopMovContentProvider.CONTENT_URI, projection, null, null, null);

        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()){
            long id = Long.valueOf(cursor.getString(cursor.getColumnIndex(FavoriteContract.COLUMN_MOVIE_ID)));
            String title = cursor.getString(cursor.getColumnIndex(FavoriteContract.COLUMN_TITLE));
            String imageId = cursor.getString(cursor.getColumnIndex(FavoriteContract.COLUMN_IMAGE_ID));
            String overview = cursor.getString(cursor.getColumnIndex(FavoriteContract.COLUMN_OVERVIEW));
            double rating = cursor.getDouble(cursor.getColumnIndex(FavoriteContract.COLUMN_RATING));
            double voteAvg = cursor.getDouble(cursor.getColumnIndex(FavoriteContract.COLUMN_VOTE_AVG));
            long count = cursor.getLong(cursor.getColumnIndex(FavoriteContract.COLUMN_VOTE_COUNT));
            String release = cursor.getString(cursor.getColumnIndex(FavoriteContract.COLUMN_RELEASE_DATE));

            Movie movie = new Movie(id, title, imageId, overview, rating, voteAvg, count, release);
            movie.setIsFavorite(true);

            movies.add(movie);
        }

        return movies;
    }
}


