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
import com.engtoolsdev.popmov.models.Review;
import com.engtoolsdev.popmov.views.RatingView;

import java.util.ArrayList;

/**
 * Created by Jose on 6/6/15.
 */
public class ReviewListAdapter extends RecyclerView.Adapter<ReviewListAdapter.ReviewHolder> {

    Context context;
    ArrayList<Review> reviewItems;

    public ReviewListAdapter(Context context, ArrayList<Review> reviews){
        this.context = context;
        this.reviewItems = reviews;
    }

    @Override
    public ReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);

        View view = layoutInflater.inflate(R.layout.review_item_layout, parent, false);

        return new ReviewHolder(view);
    }

    @Override
    public void onBindViewHolder(ReviewHolder holder, int position) {
        final Review review = getItem(position);

        if(review != null) {
            holder.authorView.setText(review.getAuthor());
            holder.contentView.setText(review.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return reviewItems.size();
    }

    public class ReviewHolder extends RecyclerView.ViewHolder{

        TextView authorView;
        TextView contentView;

        public ReviewHolder(View itemView) {
            super(itemView);

            authorView = (TextView) itemView.findViewById(R.id.review_item_author);
            contentView = (TextView) itemView.findViewById(R.id.review_item_content);
        }
    }

    public Review getItem(int position){
        if(position < getItemCount()) {
            return reviewItems.get(position);
        }
        return null;
    }
}
