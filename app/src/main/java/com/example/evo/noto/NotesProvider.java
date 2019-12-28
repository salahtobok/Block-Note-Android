package com.example.evo.noto;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

public class NotesProvider extends ContentProvider {

    //This string will used as authentication key of our incoming uri ,
    // it indicate that this uri it come from our application **here we use a package name
    private static final String AUTHORITY = "com.example.evo.noto.NotesProvider";
    private static final String BASE_PATH = "notes";
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY + "/" + BASE_PATH);

    // Constant to identify the requested operation
    private static final int NOTES = 1;
    private static final int NOTES_ID = 2;
    private static final int NOTES_TAG = 3;

    private static final UriMatcher uriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    public static final String CONTENT_ITEM_TYPE = "Note";

    static {
        uriMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTES_ID);
        uriMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTES_TAG);
    }

    private SQLiteDatabase database;

    //Reference database instance
    @Override
    public boolean onCreate() {

        DBOpenHelper helper = new DBOpenHelper(getContext());
        database = helper.getWritableDatabase();
        return true;
    }

    //This method called when we need to declare a specific database statement
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor = null;


        if (uriMatcher.match(uri) == NOTES_ID) {
            selection = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();
        }


        if (selection != null) {
            if (selection.indexOf(DBOpenHelper.NOTE_TAG) == -1) {
                cursor = database.query(DBOpenHelper.TABLE_NOTES, DBOpenHelper.ALL_COLUMNS,
                        selection, null, null, null,
                        DBOpenHelper.NOTE_CREATED + " DESC");
            }
            if (selection.indexOf(DBOpenHelper.NOTE_TAG) == 0) {
                selection = selection.substring(DBOpenHelper.NOTE_TAG.length() + 1, selection.length());

                Log.i("selection", selection);

                cursor = database.query(DBOpenHelper.TABLE_NOTES, DBOpenHelper.ALL_COLUMNS,
                        DBOpenHelper.NOTE_TAG + " like?" + " or " + DBOpenHelper.NOTE_TEXT + " like?"
                        , new String[]{String.valueOf("%" + selection + "%"), String.valueOf("%" + selection + "%")}, null, null,
                        DBOpenHelper.NOTE_CREATED + " DESC");
            }
        } else {

            cursor = database.query(DBOpenHelper.TABLE_NOTES, DBOpenHelper.ALL_COLUMNS,
                    selection, null, null, null,
                    DBOpenHelper.NOTE_CREATED + " DESC");

        }


        return cursor;


    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    //this method will called when we need to insert a data
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long id = database.insert(DBOpenHelper.TABLE_NOTES,
                null, values);
        return Uri.parse(BASE_PATH + "/" + id);
    }
    //this method will called when we need to delete a data
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return database.delete(DBOpenHelper.TABLE_NOTES, selection, selectionArgs);
    }
    //this method will called when we need to update a record
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return database.update(DBOpenHelper.TABLE_NOTES,
                values, selection, selectionArgs);
    }


}
