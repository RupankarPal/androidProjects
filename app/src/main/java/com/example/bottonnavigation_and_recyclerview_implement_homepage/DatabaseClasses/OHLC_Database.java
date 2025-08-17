package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;

import java.util.ArrayList;

public class OHLC_Database extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StockDB.db";
    private static final int DATABASE_VERSION = 2; // Increment version to trigger onUpgrade

    public static final String TABLE_NAME = "ohlc_data";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_OPEN = "open";
    private static final String COL_HIGH = "high";
    private static final String COL_LOW = "low";
    private static final String COL_CLOSE = "close";
    private static final String COL_PREV_CLOSE = "prev_close";
    private static final String COL_VOLUME = "volume";

    public OHLC_Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_OHLC = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT UNIQUE, " +
                COL_OPEN + " TEXT, " +
                COL_HIGH + " TEXT, " +
                COL_LOW + " TEXT, " +
                COL_CLOSE + " TEXT, " +
                COL_PREV_CLOSE + " TEXT, " +
                COL_VOLUME + " INTEGER)";
        db.execSQL(CREATE_OHLC);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This is the key change. We check the old version to see what updates are needed.
        if (oldVersion < 2) {
            // Add the new column "prev_close" to the existing table
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_PREV_CLOSE + " TEXT");
        }
    }

    // Insert or update list of OHLC data
    public void insertOrUpdateOHLCList(ArrayList<OHLC_Model> ohlcList, String prev_close) {
        SQLiteDatabase db = this.getWritableDatabase();

        for (OHLC_Model model : ohlcList) {
            ContentValues values = new ContentValues();
            values.put(COL_OPEN, model.getOpen());
            values.put(COL_HIGH, model.getHigh());
            values.put(COL_LOW, model.getLow());
            values.put(COL_CLOSE, model.getClose());
            values.put(COL_PREV_CLOSE, prev_close);
            values.put(COL_VOLUME, model.getVolume());
            values.put(COL_NAME, model.getName());

            // Try update first
            int rows = db.update(TABLE_NAME, values, COL_NAME + "=?", new String[]{model.getName()});

            // If no row updated, insert new
            if (rows == 0) {
                db.insert(TABLE_NAME, null, values);
            }
        }

        db.close();
    }

    // Get all OHLC records
    public ArrayList<OHLC_Model> getAllOHLC() {
        ArrayList<OHLC_Model> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new OHLC_Model(
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_OPEN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_HIGH)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_LOW)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CLOSE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_VOLUME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME))
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d("DB_DEBUG", "Rows in DB: " + list.size());
        return list;
    }

    // Get all previous close values
    public ArrayList<String> getAllPrevClose() {
        ArrayList<String> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_PREV_CLOSE + " FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(cursor.getString(cursor.getColumnIndexOrThrow(COL_PREV_CLOSE)));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        Log.d("DB_DEBUG", "PrevClose count: " + list.size());
        return list;
    }

}