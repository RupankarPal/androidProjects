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

        // Also create stocks_data table used by StocksRow_DatabaseHelper
        String CREATE_STOCKS_DATA = "CREATE TABLE IF NOT EXISTS stocks_data (" +
                "stocks_name TEXT PRIMARY KEY, " +
                "stocks_price TEXT, " +
                "percentile TEXT, " +
                "todayChange TEXT)";
        db.execSQL(CREATE_STOCKS_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Safely add prev_close if it doesn't exist
            if (!isColumnExists(db, TABLE_NAME, COL_PREV_CLOSE)) {
                db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_PREV_CLOSE + " TEXT");
            }
            
            // Ensure stocks_data table exists
            db.execSQL("CREATE TABLE IF NOT EXISTS stocks_data (" +
                    "stocks_name TEXT PRIMARY KEY, " +
                    "stocks_price TEXT, " +
                    "percentile TEXT, " +
                    "todayChange TEXT)");
        }
    }

    private boolean isColumnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    if (columnName.equalsIgnoreCase(name)) {
                        return true;
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            cursor.close();
        }
        return false;
    }


    // Insert or update list of OHLC data
    public void UpdateOHLCList(OHLC_Model ohlcList, String prev_close) {
        SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(COL_OPEN, ohlcList.getOpen());
            values.put(COL_HIGH, ohlcList.getHigh());
            values.put(COL_LOW, ohlcList.getLow());
            values.put(COL_CLOSE, ohlcList.getClose());
            values.put(COL_PREV_CLOSE, prev_close);
            values.put(COL_VOLUME, ohlcList.getVolume());
            values.put(COL_NAME, ohlcList.getName());

            //  update the data base
            db.update(TABLE_NAME, values, COL_NAME + "=?", new String[]{ohlcList.getName()});


        db.close();
    }

    public void insertOHLCList(OHLC_Model ohlcList, String prev_close) {
        SQLiteDatabase db = this.getWritableDatabase();


            ContentValues values = new ContentValues();
            values.put(COL_OPEN, ohlcList.getOpen());
            values.put(COL_HIGH, ohlcList.getHigh());
            values.put(COL_LOW, ohlcList.getLow());
            values.put(COL_CLOSE, ohlcList.getClose());
            values.put(COL_PREV_CLOSE, prev_close);
            values.put(COL_VOLUME, ohlcList.getVolume());
            values.put(COL_NAME, ohlcList.getName());

            db.insert(TABLE_NAME, null, values);


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

    // Cache methods for UI persistence
    public void updateStockCache(String symbol, String price, String percentile, String change) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("stocks_name", symbol);
        values.put("stocks_price", price);
        values.put("percentile", percentile);
        values.put("todayChange", change);

        int rows = db.update("stocks_data", values, "stocks_name=?", new String[]{symbol});
        if (rows == 0) {
            db.insert("stocks_data", null, values);
        }
        db.close();
    }

    public ArrayList<OHLC_Model> getAllCachedStocks() {
        ArrayList<OHLC_Model> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stocks_data", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new OHLC_Model(
                        "0", "0", "0",
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_price")),
                        "0",
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_name"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}
