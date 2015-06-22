package com.engtoolsdev.popmov;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.engtoolsdev.popmov.adapters.ReviewListAdapter;
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.models.Review;
import com.engtoolsdev.popmov.utils.Api;
import com.engtoolsdev.popmov.utils.ApiUtil;
import com.engtoolsdev.popmov.utils.SubscriptionCache;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.client.Response;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReviewActivityFragment extends Fragment {

    private static final String MOVIE_EXTRA = "movie";
    private static final String REVIEW_ITEMS = "review_items";
    private static final String KEY_SUBSCRIPTION_CACHE = "review_%s";

    FrameLayout emptyStateView;
    FrameLayout progressView;
    RecyclerView reviewRecyclerView;
    ReviewListAdapter reviewListAdapter;
    ArrayList<Review> reviewsItems = new ArrayList<>();

    Movie movie;

    private Action1<ArrayList<Review>> reviewAction = new Action1<ArrayList<Review>>() {
        @Override
        public void call(ArrayList<Review> reviews) {

            reviewsItems.clear();
            reviewsItems.addAll(reviews);
            reviewListAdapter.notifyDataSetChanged();

            if (reviews.size() == 0) {
                emptyStateView.setVisibility(View.VISIBLE);
            }

            String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());
            SubscriptionCache.getInstance().remove(key);

            showProgress(false);
        }
    };

    public ReviewActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_review, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressView = (FrameLayout)  view.findViewById(R.id.progress_layout);
        emptyStateView = (FrameLayout) view.findViewById(R.id.review_empty_state);

        reviewRecyclerView = (RecyclerView) view.findViewById(R.id.review_recyclerview);

        reviewListAdapter = new ReviewListAdapter(getActivity(), reviewsItems);

        reviewRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        reviewRecyclerView.setAdapter(reviewListAdapter);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Intent intent = getActivity().getIntent();

        if(intent.hasExtra(MOVIE_EXTRA)) {
            movie = getActivity().getIntent().getParcelableExtra(MOVIE_EXTRA);
        }else if(savedInstanceState != null && savedInstanceState.containsKey(MOVIE_EXTRA)){
            movie = savedInstanceState.getParcelable(MOVIE_EXTRA);
        }

        if(savedInstanceState == null) {
            if(movie != null) {
                loadReviews(movie);
            }
        }else{
            if(savedInstanceState.containsKey(REVIEW_ITEMS)) {
                ArrayList<Review> reviews = savedInstanceState.getParcelableArrayList(REVIEW_ITEMS);
                reviewsItems.addAll(reviews);
                reviewListAdapter.notifyDataSetChanged();

                if (reviewsItems.size() == 0) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }

                //showProgress(false);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(REVIEW_ITEMS, reviewsItems);
        outState.putParcelable(MOVIE_EXTRA, movie);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();

        String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());
        if(SubscriptionCache.getInstance().contains(key)) {
            Observable<ArrayList<Review>> request = (Observable<ArrayList<Review>>) SubscriptionCache.getInstance().get(KEY_SUBSCRIPTION_CACHE);
            request.subscribe(reviewAction);
        }

    }

    /**
     * Hides/Show {@link android.widget.ProgressBar}
     * @param show boolean, determine wether to show or not {@link android.widget.ProgressBar}
     */
    private void showProgress(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;

        progressView.setVisibility(visibility);
    }

    @SuppressWarnings("unchecked")
    public void loadReviews(final Movie movie){

        showProgress(true);

        Api api = ApiUtil.getApi();

        String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());

        Observable<ArrayList<Review>> request = null;
        if(!SubscriptionCache.getInstance().contains(key)) {
            request = AppObservable.bindFragment(this, api.fetchReviews(movie.getId(), getString(R.string.api_key)))
                    .map(new Func1<Response, ArrayList<Review>>() {
                        @Override
                        public ArrayList<Review> call(Response response) {

                            ArrayList<Review> results = new ArrayList<>();
                            try {
                                JsonReader jsonReader = new JsonReader(new InputStreamReader(response.getBody().in(), "UTF-8"));
                                jsonReader.beginObject();
                                while (jsonReader.hasNext()){
                                    String name = jsonReader.nextName();
                                    switch (name){
                                        case "results":
                                            results.addAll(readResults(jsonReader));
                                            break;
                                        default:
                                            jsonReader.skipValue();
                                            break;
                                    }
                                }
                                jsonReader.endObject();
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                            return results;
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, ArrayList<Review>>() {
                        @Override
                        public ArrayList<Review> call(Throwable throwable) {
                            return new ArrayList<>();
                        }
                    })
                    .cache();
        }else{
            request = (Observable<ArrayList<Review>>) SubscriptionCache.getInstance().get(key);
        }


        request.subscribe(reviewAction);

    }

    private ArrayList<Review> readResults(JsonReader jsonReader) throws IOException{

        ArrayList<Review> reviews = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()){
            reviews.add(readReview(jsonReader));
        }
        jsonReader.endArray();

        return reviews;
    }

    private Review readReview(JsonReader jsonReader) throws  IOException{

        String id = null;
        String author = null;
        String content = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String name = jsonReader.nextName();
            switch (name){
                case "id":
                    id = jsonReader.nextString();
                    break;
                case "author":
                    author = jsonReader.nextString();
                    break;
                case "content":
                    content = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return new Review(id, author, content);
    }
}
