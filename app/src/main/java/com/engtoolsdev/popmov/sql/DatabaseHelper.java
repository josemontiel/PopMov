package com.engtoolsdev.popmov.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.engtoolsdev.popmov.sql.contracts.FavoriteContract;

/**
 * Created by Jose on 6/20/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final int   DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "popmov.db";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FavoriteContract.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(FavoriteContract.DELETE_TABLE);
        onCreate(db);
    }
}
