package com.engtoolsdev.popmov.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Jose on 6/6/15.
 */
public class Movie implements Parcelable {

    private long id;
    private String title;
    private String imageId;
    private String overView;
    private double rating;
    private double voteAverage;
    private long voteCount;
    private String releaseDate;
    private boolean isFavorite = false;


    /**
     * Creates a new Movie object
     *
     * @param title {@link String} title of the movie.
     * @param imageId {@link String} reference id used to retrieve the Movie's poster image.
     * @param overView {@link String} plot or summary of the movie.
     * @param rating {double} rating given to the movie by viewers.
     * @param releaseDate {@link String} release date of the movie.
     */

    public Movie(long id, String title, String imageId, String overView, double rating, double voteAverage, long voteCount, String releaseDate) {
        this.id = id;
        this.title = title;
        this.imageId = imageId;
        this.overView = overView;
        this.rating = rating;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.releaseDate = releaseDate;
    }


    protected Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        imageId = in.readString();
        overView = in.readString();
        rating = in.readDouble();
        voteAverage = in.readDouble();
        voteCount = in.readLong();
        releaseDate = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getOverView() {
        return overView;
    }

    public void setOverView(String overView) {
        this.overView = overView;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteCount(long voteCount) {
        this.voteCount = voteCount;
    }

    public long getVoteCount() {
        return voteCount;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(imageId);
        dest.writeString(overView);
        dest.writeDouble(rating);
        dest.writeDouble(voteAverage);
        dest.writeLong(voteCount);
        dest.writeString(releaseDate);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}
