package com.engtoolsdev.popmov.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.engtoolsdev.popmov.DetailActivity;
import com.engtoolsdev.popmov.R;
import com.engtoolsdev.popmov.models.Movie;
import com.engtoolsdev.popmov.views.RatingView;

import java.util.ArrayList;

/**
 * Created by Jose on 6/6/15.
 */
public class MovieGridAdapter extends RecyclerView.Adapter<MovieGridAdapter.MovieHolder> {

    Context context;
    ArrayList<Movie> movieItems;
    private static final String POSTER_URL = "http://image.tmdb.org/t/p/%s%s";

    public MovieGridAdapter(Context context, ArrayList<Movie> movies){
        this.context = context;
        this.movieItems = movies;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.movie_item_layout, parent, false);

        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        final Movie movie = getItem(position);

        if(movie != null) {
            holder.titleView.setText(movie.getTitle());
            holder.ratingView.setRating(movie.getVoteAverage());

            holder.posterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra("movie", movie);
                    context.startActivity(intent);
                }
            });

            Glide.with(context).load(String.format(POSTER_URL, "w185", movie.getImageId())).crossFade().into(holder.posterView);
        }
    }

    @Override
    public int getItemCount() {
        return movieItems.size();
    }

    public class MovieHolder extends RecyclerView.ViewHolder{

        ImageView posterView;
        TextView titleView;
        RatingView ratingView;

        public MovieHolder(View itemView) {
            super(itemView);

            posterView = (ImageView) itemView.findViewById(R.id.movie_item_poster_imageview);
            titleView = (TextView) itemView.findViewById(R.id.movie_item_name_textview);
            ratingView = (RatingView) itemView.findViewById(R.id.movie_item_ratingview);
        }
    }

    public Movie getItem(int position){
        if(position < getItemCount()) {
            return movieItems.get(position);
        }
        return null;
    }
}
