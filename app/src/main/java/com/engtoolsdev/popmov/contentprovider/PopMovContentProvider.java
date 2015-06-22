package com.engtoolsdev.popmov.contentprovider;


import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.engtoolsdev.popmov.sql.DatabaseHelper;
import com.engtoolsdev.popmov.sql.DatabaseManager;
import com.engtoolsdev.popmov.sql.contracts.FavoriteContract;

public class PopMovContentProvider extends ContentProvider {

  //database manager
  private  DatabaseManager database;

  //UriMacher codes
  private static final int POPMOV = 10;
  private static final int MOVIE_ID = 20;

  private static final String AUTHORITY = "com.engtoolsdev.popmov.contentprovider";

  private static final String BASE_PATH = "popmov";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
      + "/" + BASE_PATH);

  public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
      + "/movies";
  public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
      + "/movie";

  private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
  static {
    sURIMatcher.addURI(AUTHORITY, BASE_PATH, POPMOV);
    sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", MOVIE_ID);
  }

  @Override
  public boolean onCreate() {
    DatabaseManager.initializeInstance(new DatabaseHelper(getContext()));
    database = DatabaseManager.getInstance();
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {

    // Uisng SQLiteQueryBuilder instead of query() method
    SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

    // Set the table
    queryBuilder.setTables(FavoriteContract.TABLE_NAME);

    int uriType = sURIMatcher.match(uri);
    switch (uriType) {
    case POPMOV:
      break;
    case MOVIE_ID:
      // adding the ID to the original query
      queryBuilder.appendWhere(FavoriteContract.COLUMN_MOVIE_ID + "="
          + uri.getLastPathSegment());
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SQLiteDatabase db = database.openDatabase();
    Cursor cursor = queryBuilder.query(db, projection, selection,
        selectionArgs, null, null, sortOrder);
    // make sure that potential listeners are getting notified
    cursor.setNotificationUri(getContext().getContentResolver(), uri);

    return cursor;
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.openDatabase();
    int rowsDeleted = 0;
    long id = 0;
    switch (uriType) {
    case POPMOV:
      id = sqlDB.insert(FavoriteContract.TABLE_NAME, null, values);
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return Uri.parse(BASE_PATH + "/" + id);
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.openDatabase();
    int rowsDeleted = 0;
    switch (uriType) {
    case POPMOV:
      rowsDeleted = sqlDB.delete(FavoriteContract.TABLE_NAME, selection,
          selectionArgs);
      break;
    case MOVIE_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsDeleted = sqlDB.delete(FavoriteContract.TABLE_NAME,
            FavoriteContract.COLUMN_MOVIE_ID + "=" + id,
            null);
      } else {
        rowsDeleted = sqlDB.delete(FavoriteContract.TABLE_NAME,
            FavoriteContract.COLUMN_MOVIE_ID + "=" + id
            + " and " + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {

    int uriType = sURIMatcher.match(uri);
    SQLiteDatabase sqlDB = database.openDatabase();
    int rowsUpdated = 0;
    switch (uriType) {
    case POPMOV:
      rowsUpdated = sqlDB.update(FavoriteContract.TABLE_NAME,
          values, 
          selection,
          selectionArgs);
      break;
    case MOVIE_ID:
      String id = uri.getLastPathSegment();
      if (TextUtils.isEmpty(selection)) {
        rowsUpdated = sqlDB.update(FavoriteContract.TABLE_NAME,
            values,
            FavoriteContract.COLUMN_MOVIE_ID + "=" + id,
            null);
      } else {
        rowsUpdated = sqlDB.update(FavoriteContract.TABLE_NAME,
            values,
            FavoriteContract.COLUMN_MOVIE_ID + "=" + id
            + " and " 
            + selection,
            selectionArgs);
      }
      break;
    default:
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return rowsUpdated;
  }

} 