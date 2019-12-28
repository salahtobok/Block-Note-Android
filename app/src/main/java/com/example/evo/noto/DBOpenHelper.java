package com.example.evo.noto;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBOpenHelper extends SQLiteOpenHelper{

    //Constants for db name and version
    private static final String DATABASE_NAME = "notes.db";
    //this constant use to represent database version ,so as much we change our database structure we should to upgrade it
    private static final int DATABASE_VERSION = 3;

    //Constants for identifying table and columns
    public static final String TABLE_NOTES = "notes";
    public static final String NOTE_ID = "_id";
    public static final String NOTE_TEXT = "noteText";
    public static final String NOTE_TAG = "noteTag";
    public static final String NOTE_CREATED = "noteCreated";

    public static final String[] ALL_COLUMNS =
            {NOTE_ID, NOTE_TEXT,NOTE_TAG, NOTE_CREATED};

    //SQL to create table
    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_NOTES + " (" +
                    NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    NOTE_TEXT + " TEXT, " +
                    NOTE_TAG + " TEXT, " +
                    NOTE_CREATED + " TEXT default CURRENT_TIMESTAMP" +
                    ")";

    public DBOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //This method called when our application install as a first time
    //use to create database
    //Method that handle database creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    //Method that handle database update
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }
}
