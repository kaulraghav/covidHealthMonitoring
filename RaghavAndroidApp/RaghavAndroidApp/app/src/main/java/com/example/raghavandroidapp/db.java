package com.example.raghavandroidapp;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.content.ContentValues;
import android.content.Context;

public class db extends SQLiteOpenHelper implements BaseColumns {

    String TAG = "db";
    public static final int DATABASE_VERSION = 1;

    //Schema for the table
    public static final String db_name = "covid.db";
    public static final String table = "covidDB";
    public static final String col_1 = "heart_rate";
    public static final String col_2 = "resp_rate";
    public static final String col_3 = "nausea";
    public static final String col_4 = "headache";
    public static final String col_5 = "diarrhea";
    public static final String col_6 = "soar_throat";
    public static final String col_7 = "fever";
    public static final String col_8 = "muscle_ache";
    public static final String col_9 = "loss_of_smell";
    public static final String col_10 = "cough";
    public static final String col_11 = "shortness_breath";
    public static final String col_12 = "feeling_tired";
    public static final String col_13 = "gps_coordinates";

    public db(Context context) {
        super(context, db_name, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        //Log.i(TAG, "Creating the database!");
        db.execSQL("CREATE TABLE " + table + " (" + _ID + " INTEGER PRIMARY KEY, " + col_1 + " TEXT, " + col_2 + " TEXT, "
                + col_3 + " INT, " + col_4 + " INT, " + col_5 + " INT, " + col_6 + " INT, "
                + col_7 + " INT, " + col_8 + " INT, " + col_9 + " INT, " + col_10 + " INT, "
                + col_11 + " INT, " + col_12 + " INT, " + col_13 + ")");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + table);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertData(int[] ratings, String heartRate, String respRate, String coordinates) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(col_1, heartRate);
        values.put(col_2, respRate);
        values.put(col_3, ratings[0]);
        values.put(col_4, ratings[1]);
        values.put(col_5, ratings[2]);
        values.put(col_6, ratings[3]);
        values.put(col_7, ratings[4]);
        values.put(col_8, ratings[5]);
        values.put(col_9, ratings[6]);
        values.put(col_10, ratings[7]);
        values.put(col_11, ratings[8]);
        values.put(col_12, ratings[9]);
        values.put(col_13, coordinates);

        long result = db.insert(table, null, values);
        if (result == -1)
            return false;
        else
            return true;
    }
}
