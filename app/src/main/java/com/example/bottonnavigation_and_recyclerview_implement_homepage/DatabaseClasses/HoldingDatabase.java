package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HoldingDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "HoldingsDB.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Holdings";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_NAME = "stock_name";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_BUY_PRICE = "buy_price";
    private static final String COL_INVESTMENT = "investment_amount";
    private static final String COL_CUR_PRICE = "current_price";
    private static final String COL_PREV_CLOSE = "prev_close";

    public HoldingDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_QUANTITY + " INTEGER NOT NULL, " +
                COL_BUY_PRICE + " REAL NOT NULL, " +
                COL_INVESTMENT + " REAL NOT NULL, " +
                COL_CUR_PRICE + " REAL NOT NULL, " +
                COL_PREV_CLOSE + " REAL NOT NULL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    // Insert Holding
    public boolean insertHolding(String stockName, int quantity, double buyPrice, double currentPrice, double prevClose) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        double investment = quantity * buyPrice;

        cv.put(COL_NAME, stockName);
        cv.put(COL_QUANTITY, quantity);
        cv.put(COL_BUY_PRICE, buyPrice);
        cv.put(COL_INVESTMENT, investment);
        cv.put(COL_CUR_PRICE, currentPrice);
        cv.put(COL_PREV_CLOSE, prevClose);

        long result = db.insert(TABLE_NAME, null, cv);
        return result != -1;
    }

    // Update Current Price & Prev Close (when market updates)
    public int updatePrices(int id, double currentPrice, double prevClose) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_CUR_PRICE, currentPrice);
        cv.put(COL_PREV_CLOSE, prevClose);

        return db.update(TABLE_NAME, cv, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // 1. Total Profit/Loss
    public double getTotalPL() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM((" + COL_CUR_PRICE + " - " + COL_BUY_PRICE + ") * " + COL_QUANTITY + ") FROM " + TABLE_NAME, null);

        double result = 0;
        if (cursor.moveToFirst()) result = cursor.getDouble(0);
        cursor.close();
        return result;
    }

    // 2. Total Investment
    public double getInvestmentAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_INVESTMENT + ") FROM " + TABLE_NAME, null);

        double result = 0;
        if (cursor.moveToFirst()) result = cursor.getDouble(0);
        cursor.close();
        return result;
    }

    // 3. Current Amount
    public double getCurrentAmount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_CUR_PRICE + " * " + COL_QUANTITY + ") FROM " + TABLE_NAME, null);

        double result = 0;
        if (cursor.moveToFirst()) result = cursor.getDouble(0);
        cursor.close();
        return result;
    }

    // 4. Today's P/L
    public double getTodaysPL() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM((" + COL_CUR_PRICE + " - " + COL_PREV_CLOSE + ") * " + COL_QUANTITY + ") FROM " + TABLE_NAME, null);

        double result = 0;
        if (cursor.moveToFirst()) result = cursor.getDouble(0);
        cursor.close();
        return result;
    }
}
