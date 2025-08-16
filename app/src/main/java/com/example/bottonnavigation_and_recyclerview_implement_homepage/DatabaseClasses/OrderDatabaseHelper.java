package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OrderDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "orders.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ORDERS = "orders";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ORDER_PRICE = "order_price";
    private static final String COLUMN_STOCK_PRICE = "stock_price";
    private static final String COLUMN_STOCK_QUANTITY = "stock_quantity";
    private static final String COLUMN_EXECUTED_QUANTITY = "executed_quantity";
    private static final String COLUMN_TARGET_PRICE = "target_price";
    private static final String COLUMN_SL_PRICE = "sl_price";
    private static final String COLUMN_STOCK_NAME = "stock_name";
    private static final String COLUMN_ORDER_TYPE = "order_type";
    private static final String COLUMN_DATE = "date";

    public OrderDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_ORDERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ORDER_PRICE + " REAL, "
                + COLUMN_STOCK_PRICE + " REAL, "
                + COLUMN_STOCK_QUANTITY + " REAL, "
                + COLUMN_EXECUTED_QUANTITY + " REAL, "
                + COLUMN_TARGET_PRICE + " REAL, "
                + COLUMN_SL_PRICE + " REAL, "
                + COLUMN_STOCK_NAME + " TEXT, "
                + COLUMN_ORDER_TYPE + " TEXT, "
                + COLUMN_DATE + " TEXT)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        onCreate(db);
    }

    // Get current date in dd-MM-yyyy format
    private String getCurrentDate() {
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }

    // Insert Order
    public long insertOrder(ArrayList<Order_model> order_modelArrayList) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_PRICE, order_modelArrayList.get(0).getOrder_prise());
        values.put(COLUMN_STOCK_PRICE, order_modelArrayList.get(0).getStock_price());
        values.put(COLUMN_STOCK_QUANTITY, order_modelArrayList.get(0).getStock_quantity());
        values.put(COLUMN_EXECUTED_QUANTITY, order_modelArrayList.get(0).getExicuted_quantity());
        values.put(COLUMN_TARGET_PRICE, order_modelArrayList.get(0).getTargate_price());
        values.put(COLUMN_SL_PRICE, order_modelArrayList.get(0).getSl_price());
        values.put(COLUMN_STOCK_NAME, order_modelArrayList.get(0).getStock_name());
        values.put(COLUMN_ORDER_TYPE, order_modelArrayList.get(0).getOrder_type());
        values.put(COLUMN_DATE, getCurrentDate());

        return db.insert(TABLE_ORDERS, null, values);
    }

    // Update Order
    public int updateOrder(int id, double orderPrice, double stockPrice, double stockQuantity,
                           double executedQuantity, double targetPrice, double slPrice,
                           String stockName, String orderType) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ORDER_PRICE, orderPrice);
        values.put(COLUMN_STOCK_PRICE, stockPrice);
        values.put(COLUMN_STOCK_QUANTITY, stockQuantity);
        values.put(COLUMN_EXECUTED_QUANTITY, executedQuantity);
        values.put(COLUMN_TARGET_PRICE, targetPrice);
        values.put(COLUMN_SL_PRICE, slPrice);
        values.put(COLUMN_STOCK_NAME, stockName);
        values.put(COLUMN_ORDER_TYPE, orderType);
        values.put(COLUMN_DATE, getCurrentDate()); // update date on modification

        return db.update(TABLE_ORDERS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Delete Order
    public int deleteOrder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_ORDERS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
    }

    // Get all Orders
    public Cursor getAllOrders() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ORDERS, null);
    }
}
