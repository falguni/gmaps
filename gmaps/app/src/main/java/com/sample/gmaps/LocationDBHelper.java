package com.sample.gmaps;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

/**
 * Custom LocationDBHelper class that extends SQLiteOpenHelper
 */
public class LocationDBHelper extends SQLiteOpenHelper {

    private static LocationDBHelper dbInstance;
    private static SQLiteDatabase db;

    // Database Version
    private static final int DATABASE_VERSION = 1;

    private final static String CREATE_LOCATION_TABLE_STMT = "CREATE TABLE IF NOT EXISTS  " + Constants.LOCATION_TABLE_NAME + " ("
            + LocationDBColNames.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + LocationDBColNames.NAME + " TEXT NOT NULL,"
            + LocationDBColNames.LATITUDE + " DOUBLE NOT NULL,"
            + LocationDBColNames.LONGITUDE + " DOUBLE NOT NULL,"
            + LocationDBColNames.CATEGORY + " TEXT NOT NULL"
            + ")";

    public static synchronized LocationDBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (dbInstance == null) {
            dbInstance = new LocationDBHelper(context.getApplicationContext());
            db = dbInstance.getWritableDatabase();
        }
        return dbInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private LocationDBHelper(Context context) {
        super(context, Constants.LOCATION_SQL_DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_LOCATION_TABLE_STMT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Constants.LOCATION_TABLE_NAME);
        onCreate(db);
    }

    public synchronized boolean executeInsertQuery(String query) {
        boolean isInsertSuccess;
        SQLiteStatement sqlStmt = null;
        try {
            sqlStmt = db.compileStatement(query);
            sqlStmt.executeInsert();
            isInsertSuccess = true;
        } catch (Exception e) {
            Log.w("UNEXPECTED_EXCEPTION","Exception in SQLiteAccess.executeInsertQuery()");
            e.printStackTrace();
            isInsertSuccess = false;
        } finally {
            if (sqlStmt != null) {
                sqlStmt.close();
            }
        }
        return isInsertSuccess;
    }

    public synchronized Cursor getRecords(String query) {
        Cursor cursor = null;
        try {
            if (db == null) {
                db = getReadableDatabase();
            }
            cursor = db.rawQuery(query, null);
        } catch (Exception e) {
            Log.w("UNEXPECTED_EXCEPTION","Exception in SQLiteAccess.getRecords()");
            e.printStackTrace();
        }
        return cursor;
    }

    public synchronized boolean deleteRecords(String query) {
        boolean isDeleteSuccess = false;
        try {
            if (db == null) {
                db = getReadableDatabase();
            }
            isDeleteSuccess = db.rawQuery(query, null).moveToFirst();
        } catch (Exception e) {
            Log.w("UNEXPECTED_EXCEPTION","Exception in SQLiteAccess.getRecords()");
            e.printStackTrace();
        }
        return isDeleteSuccess;
    }
}