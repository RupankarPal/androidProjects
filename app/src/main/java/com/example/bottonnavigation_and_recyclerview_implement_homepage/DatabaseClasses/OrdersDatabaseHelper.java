package com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;

import java.util.ArrayList;

public class OrdersDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "OrdersDB.db";
    private static final int DATABASE_VERSION = 4;



    private static final String TABLE_NAME = "Orders";

    // Columns
    private static final String COL_ID = "id";
    private static final String COL_ORDER_PRICE = "order_price";
    private static final String COL_STOCK_PRICE = "stock_price";
    private static final String COL_QUANTITY = "stock_quantity";
    private static final String COL_EXECUTED = "executed_quantity";
    private static final String COL_TARGET = "target_price";
    private static final String COL_SL = "sl_price";
    private static final String COL_NAME = "stock_name";
    private static final String COL_TYPE = "order_type";
    private static final String COL_TIME = "order_time";
    private static final String COL_TRAILING_SL = "trailing_sl";
    private static final String COL_PRODUCT_TYPE = "product_type";



    public OrdersDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ORDER_PRICE + " REAL, " +
                COL_STOCK_PRICE + " REAL, " +
                COL_QUANTITY + " REAL, " +
                COL_EXECUTED + " REAL, " +
                COL_TARGET + " REAL, " +
                COL_SL + " REAL, " +
                COL_NAME + " TEXT, " +
                COL_TYPE + " TEXT, " +
                COL_TIME + " TEXT, " +
                COL_TRAILING_SL + " REAL, " +
                COL_PRODUCT_TYPE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_TIME + " TEXT");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_TRAILING_SL + " REAL DEFAULT 0");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_PRODUCT_TYPE + " TEXT DEFAULT 'DELIVERY'");
        }
    }



    // Insert single order
    public boolean insertOrder(Order_model order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_ORDER_PRICE, order.getOrder_prise());
        cv.put(COL_STOCK_PRICE, order.getStock_price());
        cv.put(COL_QUANTITY, order.getStock_quantity());
        cv.put(COL_EXECUTED, order.getExicuted_quantity());
        cv.put(COL_TARGET, order.getTargate_price());
        cv.put(COL_SL, order.getSl_price());
        cv.put(COL_NAME, order.getStock_name());
        cv.put(COL_TYPE, order.getOrder_type());
        cv.put(COL_TIME, order.getTime());
        cv.put(COL_TRAILING_SL, order.getTrailingSl());
        cv.put(COL_PRODUCT_TYPE, order.getProduct_type());

        long result = db.insert(TABLE_NAME, null, cv);


        db.close();
        return result != -1;

    }

    // Insert list of orders
    public void insertOrders(ArrayList<Order_model> orders) {
        for (Order_model order : orders) {
            insertOrder(order);
        }
    }

    // Get all orders as ArrayList<Order_model>
    public ArrayList<Order_model> getAllOrders() {
        ArrayList<Order_model> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);

        if (cursor.moveToFirst()) {
            do {
                double orderPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_ORDER_PRICE));
                double stockPrice = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_STOCK_PRICE));
                double quantity = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                double executed = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_EXECUTED));
                double target = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TARGET));
                double sl = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_SL));
                String stockName = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String orderType = cursor.getString(cursor.getColumnIndexOrThrow(COL_TYPE));
                String time = cursor.getString(cursor.getColumnIndexOrThrow(COL_TIME));
                double trailingSl = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TRAILING_SL));
                String productType = cursor.getString(cursor.getColumnIndexOrThrow(COL_PRODUCT_TYPE));
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));

                Order_model order = new Order_model(id, orderPrice, stockPrice, quantity, executed, target, sl, stockName, orderType, time, trailingSl, productType);
                list.add(order);



            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;

    }

    // Cancel order (delete by ID)
    public int cancelOrder(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_NAME, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows;
    }


    // Modify order (update fields by ID)
    public int modifyOrder(int id, Order_model order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_ORDER_PRICE, order.getOrder_prise());
        cv.put(COL_STOCK_PRICE, order.getStock_price());
        cv.put(COL_QUANTITY, order.getStock_quantity());
        cv.put(COL_EXECUTED, order.getExicuted_quantity());
        cv.put(COL_TARGET, order.getTargate_price());
        cv.put(COL_SL, order.getSl_price());
        cv.put(COL_NAME, order.getStock_name());
        cv.put(COL_TYPE, order.getOrder_type());
        cv.put(COL_TIME, order.getTime());
        cv.put(COL_TRAILING_SL, order.getTrailingSl());
        cv.put(COL_PRODUCT_TYPE, order.getProduct_type());

        int rows = db.update(TABLE_NAME, cv, COL_ID + "=?", new String[]{String.valueOf(id)});


        db.close();
        return rows;
    }

    public void clearAllOrders() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
    }

}

