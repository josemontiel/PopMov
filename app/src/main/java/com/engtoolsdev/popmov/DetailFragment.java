package com.engtoolsdev.popmov;


import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
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
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.views.RatingView;

import java.text.SimpleDateFormat;

import timber.log.Timber;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {

    private static final String MOVIE_EXTRA = "movie";
    private static final String POSTER_URL = "http://image.tmdb.org/t/p/%s%s";


    FrameLayout progressView;
    ImageView posterCover;
    TextView overviewTextView;
    CollapsingToolbarLayout collapsingToolbar;
    Toolbar toolbar;
    TextView yearTextView;
    RatingView ratingView;
    FloatingActionButton actionButton;

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

        posterCover = (ImageView) view.findViewById(R.id.detail_backdrop);
        overviewTextView = (TextView) view.findViewById(R.id.overview_textview);
        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        yearTextView = (TextView) view.findViewById(R.id.detail_year);
        ratingView = (RatingView) view.findViewById(R.id.detail_rating);
        actionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        progressView = (FrameLayout) view.findViewById(R.id.progress_layout);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Movie movie = getActivity().getIntent().getParcelableExtra(MOVIE_EXTRA);

        ((DetailActivity) getActivity()).setSupportActionBar(toolbar);

        ((DetailActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadMovie(movie);

    }

    /**
     * Loads the {@link Movie} data into the UI
     * @param movie {@link Movie} movie passed throught {@link android.content.Intent}
     */

    private void loadMovie(Movie movie) {

        String[] dateSplits = movie.getReleaseDate().split("-");

        collapsingToolbar.setTitle(movie.getTitle());
        overviewTextView.setText(movie.getOverView());
        if(dateSplits.length != 0) {
            yearTextView.setText(dateSplits[0]);
        }
        ratingView.setRating(movie.getVoteAverage());

        Glide.with(getActivity()).load(String.format(POSTER_URL, "w342", movie.getImageId())).asBitmap().listener(new RequestListener<String, Bitmap>() {
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

                        showProgress(false);
                    }
                });

                return false;
            }
        }).into(posterCover);
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
        collapsingToolbar.setBackgroundColor(palette.getMutedColor(getResources().getColor(R.color.primary)));
        collapsingToolbar.setContentScrimColor(palette.getMutedColor(getResources().getColor(R.color.primary)));
        ratingView.setArcColor(palette.getVibrantColor(getResources().getColor(R.color.primary)));
        actionButton.setBackgroundTintList(ColorStateList.valueOf(palette.getVibrantColor(getResources().getColor(R.color.accent))));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(palette.getMutedColor(getResources().getColor(R.color.primary_dark)));
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
}
