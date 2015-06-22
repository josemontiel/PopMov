package com.engtoolsdev.popmov;


import android.content.ContentValues;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.engtoolsdev.popmov.contentprovider.PopMovContentProvider;
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.models.Trailer;
import com.engtoolsdev.popmov.sql.contracts.FavoriteContract;
import com.engtoolsdev.popmov.utils.Api;
import com.engtoolsdev.popmov.utils.ApiUtil;
import com.engtoolsdev.popmov.utils.SubscriptionCache;
import com.engtoolsdev.popmov.views.RatingView;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.client.Response;
import rx.Observable;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.functions.Func1;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private static final String KEY_SUBSCRIPTION_CACHE = "detail_%s";

    private static final String MOVIE_EXTRA = "movie";
    private static final String POSTER_URL = "http://image.tmdb.org/t/p/%s%s";
    private static final String YOUTUBE_THUMBNAIL_URL = "http://img.youtube.com/vi/%s/0.jpg";
    private static final String YOUTUBE_VIDEO_URL = "http://www.youtube.com/watch?v=%s";


    private boolean multiPane;

    private Movie movie;

    CoordinatorLayout mainContent;
    FrameLayout progressView;
    ImageView posterCover;
    TextView overviewTextView;
    CollapsingToolbarLayout collapsingToolbar;
    Toolbar toolbar;
    TextView yearTextView;
    RatingView ratingView;
    FloatingActionButton actionButton;
    TextView reviewHeader;

    CardView trailerOne;
    ImageView trailerOneImageView;
    ImageView shareOne;

    CardView trailerTwo;
    ImageView trailerTwoImageView;
    ImageView shareTwo;

    CardView trailerThree;
    ImageView trailerThreeImageView;
    ImageView shareThree;

    private View.OnClickListener trailerClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            playTrailer(v);
        }
    };

    private View.OnClickListener favoriteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(!movie.isFavorite()){
                addFavorite();
            }else{
                removeFavorite();
            }
        }
    };

    private View.OnClickListener reviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            detailReviews();
        }
    };

    private final Action1<ArrayList<Trailer>> trailerAction = new Action1<ArrayList<Trailer>>() {
        @Override
        public void call(ArrayList<Trailer> trailers) {
            showProgress(false);
            showTrailers(trailers);

            //We remove the Observable's Action of the Cache
            String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());
            SubscriptionCache.getInstance().remove(key);
        }
    };

    private final View.OnClickListener shareClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String url = (String) v.getTag();
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_SUBJECT, "Share Trailer");
            share.putExtra(Intent.EXTRA_TEXT, url);
            startActivity(Intent.createChooser(share, "Share Trailer"));
        }
    };

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        multiPane = getResources().getBoolean(R.bool.multi_pane);

        mainContent = (CoordinatorLayout) view.findViewById(R.id.main_content);
        posterCover = (ImageView) view.findViewById(R.id.detail_backdrop);
        overviewTextView = (TextView) view.findViewById(R.id.overview_textview);
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        yearTextView = (TextView) view.findViewById(R.id.detail_year);
        ratingView = (RatingView) view.findViewById(R.id.detail_rating);
        progressView = (FrameLayout) view.findViewById(R.id.progress_layout);

        reviewHeader = (TextView) view.findViewById(R.id.detail_review);
        reviewHeader.setOnClickListener(reviewClickListener);

        actionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        actionButton.setOnClickListener(favoriteClickListener);

        trailerOne = (CardView) view.findViewById(R.id.trailer_card_1);
        trailerOne.setOnClickListener(trailerClickListener);

        shareOne = (ImageView) view.findViewById(R.id.trailer_card_share_1);
        shareOne.setOnClickListener(shareClickListener);

        trailerOneImageView = (ImageView) trailerOne.findViewById(R.id.trailer_card_imageview_1);

        trailerTwo = (CardView) view.findViewById(R.id.trailer_card_2);
        trailerTwo.setOnClickListener(trailerClickListener);

        shareTwo = (ImageView) view.findViewById(R.id.trailer_card_share_2);
        shareTwo.setOnClickListener(shareClickListener);

        trailerTwoImageView = (ImageView) trailerTwo.findViewById(R.id.trailer_card_imageview_2);

        trailerThree = (CardView) view.findViewById(R.id.trailer_card_3);
        trailerThree.setOnClickListener(trailerClickListener);

        shareThree = (ImageView) view.findViewById(R.id.trailer_card_share_3);
        shareThree.setOnClickListener(shareClickListener);

        trailerThreeImageView = (ImageView) trailerThree.findViewById(R.id.trailer_card_imageview_3);

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

        if(!multiPane) {
            ((DetailActivity) getActivity()).setSupportActionBar(toolbar);

            ((DetailActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }


        if(movie != null) {
            loadMovie();

            loadTrailers();
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public void onResume() {
        super.onResume();

        if(movie != null) {

            //We check if there is a pending Observable. If so, we subscribe trailerAction to it.
            String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());

            if (SubscriptionCache.getInstance().contains(key)) {
                Observable<ArrayList<Trailer>> request = (Observable<ArrayList<Trailer>>) SubscriptionCache.getInstance().get(key);
                request.subscribe(trailerAction);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(movie != null){
            outState.putParcelable(MOVIE_EXTRA, movie);
        }
    }

    /**
     * Loads the {@link Movie} data into the UI
     */

    private void loadMovie() {

        showProgress(true);

        String[] dateSplits = movie.getReleaseDate().split("-");

        //There is a bug already filed on the Design Support Library where the CollapsingToolbarLayout only updates the title when scrolled and collapsed/expanded
        collapsingToolbar.setTitle(movie.getTitle());

        overviewTextView.setText(movie.getOverView());
        if(dateSplits.length != 0) {
            yearTextView.setText(dateSplits[0]);
        }
        ratingView.setRating(movie.getVoteAverage());

        Glide.with(getActivity()).load(String.format(POSTER_URL, getString(R.string.cover_res), movie.getImageId())).asBitmap().listener(new RequestListener<String, Bitmap>() {
            @Override
            public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                Palette.from(resource).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        tintElements(palette);
                    }
                });

                return false;
            }
        }).into(posterCover);

        setFavorite();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Tints major UI elements using the {@link Palette} generated from the cover image {@link Bitmap}
     * @param palette {@link Palette} generated from movie poster
     */

    private void tintElements(Palette palette) {

        if(!multiPane) {
            collapsingToolbar.setBackgroundColor(palette.getMutedColor(getResources().getColor(R.color.primary)));
            collapsingToolbar.setContentScrimColor(palette.getMutedColor(getResources().getColor(R.color.primary)));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getActivity().getWindow().setStatusBarColor(palette.getMutedColor(getResources().getColor(R.color.primary_dark)));
            }
        }

        ratingView.setArcColor(palette.getVibrantColor(getResources().getColor(R.color.primary)));
        actionButton.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(getResources().getColor(R.color.accent))));


    }

    /**
     * Hides/Show {@link android.widget.ProgressBar}
     * @param show boolean, determine wether to show or not {@link android.widget.ProgressBar}
     */
    private void showProgress(boolean show){
        int visibilityProgress = show ? View.VISIBLE : View.GONE;
        progressView.setVisibility(visibilityProgress);

        if(mainContent.getVisibility() == View.GONE) {
            mainContent.setVisibility(View.VISIBLE);
        }
    }

    @SuppressWarnings("unchecked")
    /**
     * Retrieves the Trailers of the {@link Movie}
     */
    private void loadTrailers() throws ClassCastException{

        Api api = ApiUtil.getApi();

        showProgress(true);

        String key = String.format(KEY_SUBSCRIPTION_CACHE, movie.getId());

        Observable<ArrayList<Trailer>> request;

        //We check if there is already an un-consumed request. If so, we emit the response without re triggering the network call.
        if(!SubscriptionCache.getInstance().contains(key)) {
            request = AppObservable.bindFragment(this, api.fetchTrailers(movie.getId(), getString(R.string.api_key)))
                    .map(new Func1<Response, ArrayList<Trailer>>() {
                        @Override
                        public ArrayList<Trailer> call(Response response) {
                            ArrayList<Trailer> trailers = new ArrayList<>();
                            try {
                                JsonReader jsonReader = new JsonReader(new InputStreamReader(response.getBody().in(), "UTF-8"));

                                jsonReader.beginObject();
                                while (jsonReader.hasNext()) {
                                    String key = jsonReader.nextName();
                                    switch (key) {
                                        case "results":
                                            trailers = readTrailerResults(jsonReader);
                                            break;
                                        default:
                                            jsonReader.skipValue();
                                            break;
                                    }
                                }
                                jsonReader.endObject();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            return trailers;
                        }
                    })
                    .onErrorReturn(new Func1<Throwable, ArrayList<Trailer>>() {
                        @Override
                        public ArrayList<Trailer> call(Throwable throwable) {
                            return new ArrayList<>();
                        }
                    })
                    .cache();
        }else{
            request = (Observable<ArrayList<Trailer>>) SubscriptionCache.getInstance().get(key);
        }

        //We subscribe the observable
        request.subscribe(trailerAction);

    }

    private ArrayList<Trailer> readTrailerResults(JsonReader jsonReader) throws IOException{

        ArrayList<Trailer> results = new ArrayList<>();

        jsonReader.beginArray();
        while (jsonReader.hasNext()){
            results.add(readTrailer(jsonReader));
        }
        jsonReader.endArray();

        return results;
    }

    private Trailer readTrailer(JsonReader jsonReader) throws IOException{

        String trailerKey = null;
        String name = null;
        String site = null;

        jsonReader.beginObject();
        while (jsonReader.hasNext()){
            String key = jsonReader.nextName();
            switch (key){
                case "key":
                    trailerKey = jsonReader.nextString();
                    break;
                case "name":
                    name = jsonReader.nextString();
                    break;
                case "site":
                    site = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
                    break;
            }
        }
        jsonReader.endObject();

        return new Trailer(trailerKey, name, site);
    }

    /**
     * Attaches the retrieved {@link Trailer}s to Trailer cards.
     * @param trailers
     */
    private void showTrailers(ArrayList<Trailer> trailers) {
        for(int i = 0; i<3; i++){

            //We check if there is a Trailer and set to a cardview. If not, we hide the remaining unset cards
            if(i<trailers.size()) {
                Trailer trailer = trailers.get(i);

                String url = String.format(YOUTUBE_VIDEO_URL, trailer.getKey());
                String imageUrl = String.format(YOUTUBE_THUMBNAIL_URL, trailer.getKey());
                switch (i) {
                    case 0:
                        trailerOne.setTag(url);
                        shareOne.setTag(url);
                        Glide.with(this).load(imageUrl).asBitmap().into(trailerOneImageView);

                        if(trailerOne.getVisibility() == View.GONE){
                            trailerOne.setVisibility(View.VISIBLE);
                        }

                        break;
                    case 1:
                        trailerTwo.setTag(url);
                        shareTwo.setTag(url);
                        Glide.with(this).load(imageUrl).asBitmap().into(trailerTwoImageView);

                        if(trailerTwo.getVisibility() == View.GONE){
                            trailerTwo.setVisibility(View.VISIBLE);
                        }

                        break;
                    case 2:
                        trailerThree.setTag(url);
                        shareThree.setTag(url);
                        Glide.with(this).load(imageUrl).asBitmap().into(trailerThreeImageView);

                        if(trailerThree.getVisibility() == View.GONE){
                            trailerThree.setVisibility(View.VISIBLE);
                        }

                        break;
                }
            }else{
                switch (i) {
                    case 0:
                        trailerOne.setVisibility(View.GONE);
                        break;
                    case 1:
                        trailerTwo.setVisibility(View.GONE);
                        break;
                    case 2:
                        trailerThree.setVisibility(View.GONE);
                        break;
                }
            }

        }
    }

    /**
     * Retrieves the {@link Trailer} url set as tag of its corresponding view.
     * @param v view  clicked
     */
    public void playTrailer(View v){
        String url = (String) v.getTag();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    /**
     * Starts the {@link ReviewActivity}
     */
    public void detailReviews() {
        Intent intent = new Intent(getActivity(), ReviewActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    /**
     * Sets the {@link FloatingActionButton} state depending if the {@link Movie} has bee favorited
     */
    public void setFavorite(){
        if(movie.isFavorite()){
            Drawable drawableCompat = DrawableCompat.wrap(actionButton.getDrawable());
            DrawableCompat.setTint(drawableCompat, getResources().getColor(R.color.favorite_color));
            actionButton.setImageDrawable(drawableCompat);
        }else{
            Drawable drawableCompat = DrawableCompat.wrap(actionButton.getDrawable());
            DrawableCompat.setTint(drawableCompat, getResources().getColor(android.R.color.white));
            actionButton.setImageDrawable(drawableCompat);
        }
    }

    /**
     * Adds a {@link Movie} to our Favorite database
     */
    public void addFavorite(){

        ContentValues values = new ContentValues();
        values.put(FavoriteContract.COLUMN_MOVIE_ID, movie.getId());
        values.put(FavoriteContract.COLUMN_TITLE, movie.getTitle());
        values.put(FavoriteContract.COLUMN_IMAGE_ID, movie.getImageId());
        values.put(FavoriteContract.COLUMN_OVERVIEW, movie.getOverView());
        values.put(FavoriteContract.COLUMN_RATING, movie.getRating());
        values.put(FavoriteContract.COLUMN_VOTE_AVG, movie.getVoteAverage());
        values.put(FavoriteContract.COLUMN_VOTE_COUNT, movie.getVoteCount());
        values.put(FavoriteContract.COLUMN_RELEASE_DATE, movie.getReleaseDate());

        getActivity().getContentResolver().insert(PopMovContentProvider.CONTENT_URI, values);

        movie.setIsFavorite(true);
        setFavorite();
    }


    /**
     * Removes a {@link Movie} from our Favorite database
     */
    public void removeFavorite(){
        Uri uri = Uri.parse(PopMovContentProvider.CONTENT_URI + "/"
                + movie.getId());
        getActivity().getContentResolver().delete(uri, FavoriteContract.COLUMN_MOVIE_ID+ " = ?", new String[]{ String.valueOf(movie.getId()) });
        movie.setIsFavorite(false);

        setFavorite();
    }

    /**
     * Sets the Fragment's {@link Movie}
     * @param movie {@link Movie} selected by the user
     */
    public void setMovie(Movie movie) {
        this.movie = movie;
        loadMovie();
        loadTrailers();
    }
}
