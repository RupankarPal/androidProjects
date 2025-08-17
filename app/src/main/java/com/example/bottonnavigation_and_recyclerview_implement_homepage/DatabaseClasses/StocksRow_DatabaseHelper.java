package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;

import java.util.ArrayList;

public class StocksRow_DatabaseHelper {

    private OHLC_Database dbHelper;

    public StocksRow_DatabaseHelper(Context context) {
        dbHelper = new OHLC_Database(context);
    }

    // Insert single stock (with duplicate check)
    public void insertOrUpdateStock(stocks_row_Model model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("stocks_name", model.getStocks_name());
        values.put("stocks_price", model.getStocks_price());
        values.put("percentile", model.getPercentile());
        values.put("todayChange", model.getTodayChange());

        int rows = db.update("stocks_data", values, "stocks_name=?", new String[]{model.getStocks_name()});
        if (rows == 0) {
            db.insert("stocks_data", null, values);
        }

        db.close();
    }

    // Insert list safely
    public void insertStocksList(ArrayList<stocks_row_Model> stocksList) {
        for (stocks_row_Model model : stocksList) {
            insertOrUpdateStock(model); // safer insert
        }
    }

    // Get a single stock by its symbol (stocks_name)
    public stocks_row_Model getStockBySymbol(String symbol) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        stocks_row_Model stock = null;
        Cursor cursor = null;

        try {
            cursor = db.query(
                    "stocks_data",
                    new String[]{"stocks_name", "stocks_price", "percentile", "todayChange"},
                    "stocks_name=?",
                    new String[]{symbol},
                    null,
                    null,
                    null
            );

            if (cursor.moveToFirst()) {
                stock = new stocks_row_Model(
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("percentile")),
                        cursor.getString(cursor.getColumnIndexOrThrow("todayChange"))
                );
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return stock;
    }

    // Get all
    public ArrayList<stocks_row_Model> getAllStocks() {
        ArrayList<stocks_row_Model> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM stocks_data", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new stocks_row_Model(
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("stocks_price")),
                        cursor.getString(cursor.getColumnIndexOrThrow("percentile")),
                        cursor.getString(cursor.getColumnIndexOrThrow("todayChange"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }
}