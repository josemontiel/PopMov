package com.engtoolsdev.popmov.sql.contracts;

import android.provider.BaseColumns;

/**
 * Created by Jose on 6/20/15.
 */
public class FavoriteContract implements BaseColumns{

    public static final String TABLE_NAME = "favorite_table";
    public static final String COLUMN_MOVIE_ID = "movie_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_IMAGE_ID = "image_id";
    public static final String COLUMN_OVERVIEW = "overview";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_VOTE_AVG = "vote_avg";
    public static final String COLUMN_VOTE_COUNT = "vote_count";
    public static final String COLUMN_RELEASE_DATE = "release_date";

    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " +
                    TABLE_NAME+ " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_MOVIE_ID + " TEXT UNIQUE, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_IMAGE_ID + " TEXT, " +
                    COLUMN_OVERVIEW + " TEXT, " +
                    COLUMN_RATING + " DOUBLE, " +
                    COLUMN_VOTE_AVG + " DOUBLE, " +
                    COLUMN_VOTE_COUNT + " INTEGER, " +
                    COLUMN_RELEASE_DATE + " TEXT" + " )";

    public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " +
                    TABLE_NAME;


}
