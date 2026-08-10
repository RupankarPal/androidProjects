package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Locale;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.TradeHistoryModel;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;

public class FundDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FundManager.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_FUNDS = "Funds";
    private static final String COL_FUND_ID = "id";
    private static final String COL_CURRENCY = "currency";
    private static final String COL_TYPE = "type";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_DATE = "date";

    private static final String TABLE_STOCKS = "StockTransactions";
    private static final String COL_STOCK_ID = "id";
    private static final String COL_STOCK_NAME = "stock_name";
    private static final String COL_STOCK_PRICE = "stock_price";
    private static final String COL_QUANTITY = "quantity";
    private static final String COL_USED_FUND = "used_fund";
    private static final String COL_STOCK_DATE = "date";

    private static final String TABLE_HISTORY = "TradeHistory";
    private static final String COL_HIST_ID = "id";
    private static final String COL_HIST_NAME = "stock_name";
    private static final String COL_HIST_QTY = "quantity";
    private static final String COL_HIST_BUY_AVG = "buy_avg";
    private static final String COL_HIST_SELL_AVG = "sell_avg";
    private static final String COL_HIST_BUY_AMT = "buy_amt";
    private static final String COL_HIST_SELL_AMT = "sell_amt";
    private static final String COL_HIST_PL = "pl";
    private static final String COL_HIST_PERCENT = "percent";
    private static final String COL_HIST_DATE = "date";

    public FundDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FUNDS + " ("
                + COL_FUND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_CURRENCY + " TEXT, "
                + COL_TYPE + " TEXT, "
                + COL_AMOUNT + " REAL, "
                + COL_DATE + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_STOCKS + " ("
                + COL_STOCK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_STOCK_NAME + " TEXT, "
                + COL_STOCK_PRICE + " REAL, "
                + COL_QUANTITY + " REAL, "
                + COL_USED_FUND + " REAL, "
                + COL_STOCK_DATE + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_HISTORY + " ("
                + COL_HIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_HIST_NAME + " TEXT, "
                + COL_HIST_QTY + " REAL, "
                + COL_HIST_BUY_AVG + " REAL, "
                + COL_HIST_SELL_AVG + " REAL, "
                + COL_HIST_BUY_AMT + " REAL, "
                + COL_HIST_SELL_AMT + " REAL, "
                + COL_HIST_PL + " REAL, "
                + COL_HIST_PERCENT + " REAL, "
                + COL_HIST_DATE + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FUNDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_HISTORY);
            onCreate(db);
        }
    }

    public void addFund(String currency, double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            addFundInternal(db, currency, amount, date);
        } finally {
            db.close();
        }
    }

    public void useFund(double amount, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, "use");
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        try {
            db.insert(TABLE_FUNDS, null, values);
        } finally {
            db.close();
        }
    }

    public double getBalance() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(CASE WHEN type='add' THEN " + COL_AMOUNT + " ELSE -" + COL_AMOUNT + " END) FROM " + TABLE_FUNDS, null);
        double balance = 0;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                balance = cursor.getDouble(0);
                cursor.close();
            }
        } finally {
            db.close();
        }
        return balance;
    }

    public void addStockToPortfolio(String stockName, double stockPrice, double quantity, String date, String productType) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            addStockInternal(db, stockName, stockPrice, quantity, date, productType);
        } finally {
            db.close();
        }
    }

    private void addStockInternal(SQLiteDatabase db, String stockName, double stockPrice, double quantity, String date, String productType) {
        double currentQty = getAvailableQuantityInternal(db, stockName);
        
        if (currentQty < -0.0001) {
            double qtyToCover = Math.min(Math.abs(currentQty), quantity);
            double remainingQty = quantity - qtyToCover;
            double avgEntryPrice = getAveragePriceInternal(db, stockName);

            ContentValues values = new ContentValues();
            values.put(COL_STOCK_NAME, stockName);
            values.put(COL_STOCK_PRICE, stockPrice);
            values.put(COL_QUANTITY, qtyToCover);
            values.put(COL_USED_FUND, -(qtyToCover * avgEntryPrice)); 
            values.put(COL_STOCK_DATE, date);
            db.insert(TABLE_STOCKS, null, values);

            double realizedPL = (avgEntryPrice - stockPrice) * qtyToCover;
            double marginToRefund = productType.equalsIgnoreCase("INTRADAY") ? (avgEntryPrice * qtyToCover) / 5.0 : (avgEntryPrice * qtyToCover);
            addFundInternal(db, "USD", marginToRefund + realizedPL, date);
            
            if (remainingQty > 0.0001) {
                addStockInternal(db, stockName, stockPrice, remainingQty, date, productType);
            }
        } else {
            double usedFund = stockPrice * quantity;
            ContentValues values = new ContentValues();
            values.put(COL_STOCK_NAME, stockName);
            values.put(COL_STOCK_PRICE, stockPrice);
            values.put(COL_QUANTITY, quantity);
            values.put(COL_USED_FUND, usedFund);
            values.put(COL_STOCK_DATE, date);
            db.insert(TABLE_STOCKS, null, values);
        }
    }

    public void sellStockFromPortfolio(String symbol, double qtyToSell, double sellPrice, String date, String productType) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            sellStockInternal(db, symbol, qtyToSell, sellPrice, date, productType);
        } finally {
            db.close();
        }
    }

    private void sellStockInternal(SQLiteDatabase db, String symbol, double qtyToSell, double sellPrice, String date, String productType) {
        double currentQty = getAvailableQuantityInternal(db, symbol);
        
        if (currentQty > 0.0001) {
            double qtyToClose = Math.min(currentQty, qtyToSell);
            double remainingQty = qtyToSell - qtyToClose;
            double avgEntryPrice = getAveragePriceInternal(db, symbol);

            ContentValues values = new ContentValues();
            values.put(COL_STOCK_NAME, symbol);
            values.put(COL_STOCK_PRICE, sellPrice);
            values.put(COL_QUANTITY, -qtyToClose); 
            values.put(COL_USED_FUND, -(qtyToClose * avgEntryPrice)); 
            values.put(COL_STOCK_DATE, date);
            db.insert(TABLE_STOCKS, null, values);
            
            double realizedPL = (sellPrice - avgEntryPrice) * qtyToClose;
            double marginToRefund = productType.equalsIgnoreCase("INTRADAY") ? (avgEntryPrice * qtyToClose) / 5.0 : (avgEntryPrice * qtyToClose);
            addFundInternal(db, "USD", marginToRefund + realizedPL, date);
            
            if (remainingQty > 0.0001) {
                sellStockInternal(db, symbol, remainingQty, sellPrice, date, productType);
            }
        } else {
            double usedFund = sellPrice * -qtyToSell; 
            ContentValues values = new ContentValues();
            values.put(COL_STOCK_NAME, symbol);
            values.put(COL_STOCK_PRICE, sellPrice);
            values.put(COL_QUANTITY, -qtyToSell);
            values.put(COL_USED_FUND, usedFund);
            values.put(COL_STOCK_DATE, date);
            db.insert(TABLE_STOCKS, null, values);
        }
    }

    private void addFundInternal(SQLiteDatabase db, String currency, double amount, String date) {
        ContentValues values = new ContentValues();
        values.put(COL_CURRENCY, currency);
        values.put(COL_TYPE, "add");
        values.put(COL_AMOUNT, amount);
        values.put(COL_DATE, date);
        db.insert(TABLE_FUNDS, null, values);
    }

    private double getAvailableQuantityInternal(SQLiteDatabase db, String symbol) {
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_QUANTITY + ") FROM " + TABLE_STOCKS + " WHERE " + COL_STOCK_NAME + "=?", new String[]{symbol});
        double total = 0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    private double getAveragePriceInternal(SQLiteDatabase db, String symbol) {
        Cursor cursor = db.rawQuery("SELECT SUM(" + COL_USED_FUND + ") / SUM(" + COL_QUANTITY + ") FROM " + TABLE_STOCKS + " WHERE " + COL_STOCK_NAME + "=?", new String[]{symbol});
        double avg = 0;
        if (cursor != null && cursor.moveToFirst()) {
            avg = Math.abs(cursor.getDouble(0)); 
            cursor.close();
        }
        return avg;
    }

    public double getAvailableQuantity(String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            return getAvailableQuantityInternal(db, symbol);
        } finally {
            db.close();
        }
    }

    public double getAveragePrice(String symbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            return getAveragePriceInternal(db, symbol);
        } finally {
            db.close();
        }
    }

    public ArrayList<portfolio_stocks_model> getPortfolioHoldings() {
        ArrayList<portfolio_stocks_model> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT " + COL_STOCK_NAME + ", SUM(" + COL_QUANTITY + ") as total_qty, SUM(" + COL_USED_FUND + ") as total_invested FROM " + TABLE_STOCKS + " GROUP BY " + COL_STOCK_NAME + " HAVING ABS(total_qty) > 0.001";
            Cursor cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndexOrThrow(COL_STOCK_NAME);
                int qtyIdx = cursor.getColumnIndexOrThrow("total_qty");
                int invIdx = cursor.getColumnIndexOrThrow("total_invested");
                do {
                    String name = cursor.getString(nameIdx);
                    double qty = cursor.getDouble(qtyIdx);
                    double invested = cursor.getDouble(invIdx);
                    double avgPrice = qty != 0 ? Math.abs(invested / qty) : 0;
                    list.add(new portfolio_stocks_model(name, String.format(Locale.getDefault(), "%.2f", avgPrice), "0.00%", String.format(Locale.getDefault(), "%.2f", avgPrice), String.valueOf(qty), String.format(Locale.getDefault(), "%.2f", invested), "0.00", "0.00%"));
                } while (cursor.moveToNext());
                cursor.close();
            }
        } finally {
            db.close();
        }
        return list;
    }

    public void addTradeToHistory(TradeHistoryModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_HIST_NAME, model.getName());
        cv.put(COL_HIST_QTY, model.getQuantity());
        cv.put(COL_HIST_BUY_AVG, model.getBuyAvg());
        cv.put(COL_HIST_SELL_AVG, model.getSellAvg());
        cv.put(COL_HIST_BUY_AMT, model.getBuyAmt());
        cv.put(COL_HIST_SELL_AMT, model.getSellAmt());
        cv.put(COL_HIST_PL, model.getPl());
        cv.put(COL_HIST_PERCENT, model.getPercent());
        cv.put(COL_HIST_DATE, model.getDate());
        try {
            db.insert(TABLE_HISTORY, null, cv);
        } finally {
            db.close();
        }
    }

    public ArrayList<TradeHistoryModel> getTradeHistory() {
        ArrayList<TradeHistoryModel> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_HISTORY + " ORDER BY " + COL_HIST_ID + " DESC", null);
            if (cursor != null && cursor.moveToFirst()) {
                int idIdx = cursor.getColumnIndexOrThrow(COL_HIST_ID);
                int nameIdx = cursor.getColumnIndexOrThrow(COL_HIST_NAME);
                int qtyIdx = cursor.getColumnIndexOrThrow(COL_HIST_QTY);
                int buyAvgIdx = cursor.getColumnIndexOrThrow(COL_HIST_BUY_AVG);
                int sellAvgIdx = cursor.getColumnIndexOrThrow(COL_HIST_SELL_AVG);
                int buyAmtIdx = cursor.getColumnIndexOrThrow(COL_HIST_BUY_AMT);
                int sellAmtIdx = cursor.getColumnIndexOrThrow(COL_HIST_SELL_AMT);
                int plIdx = cursor.getColumnIndexOrThrow(COL_HIST_PL);
                int pctIdx = cursor.getColumnIndexOrThrow(COL_HIST_PERCENT);
                int dateIdx = cursor.getColumnIndexOrThrow(COL_HIST_DATE);

                do {
                    TradeHistoryModel model = new TradeHistoryModel(
                            cursor.getString(nameIdx),
                            cursor.getDouble(qtyIdx),
                            cursor.getDouble(buyAvgIdx),
                            cursor.getDouble(sellAvgIdx),
                            cursor.getDouble(buyAmtIdx),
                            cursor.getDouble(sellAmtIdx),
                            cursor.getDouble(plIdx),
                            cursor.getDouble(pctIdx),
                            cursor.getString(dateIdx)
                    );
                    model.setId(cursor.getInt(idIdx));
                    list.add(model);
                } while (cursor.moveToNext());
                cursor.close();
            }
        } finally {
            db.close();
        }
        return list;
    }
}
