package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class SquareOffWorker extends Worker {

    public SquareOffWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("SQUARE_OFF", "Starting auto square-off process...");
        
        Context context = getApplicationContext();
        OrdersDatabaseHelper odb = new OrdersDatabaseHelper(context);
        order_process process = new order_process(context);
        
        // In a real app, we'd only square off INTRADAY orders.
        // I'll simulate this by finding all pending or active intraday positions.
        ArrayList<Order_model> allOrders = odb.getAllOrders();
        
        for (Order_model order : allOrders) {
            if ("INTRADAY".equalsIgnoreCase(order.getProduct_type()) && order.getExicuted_quantity() < order.getStock_quantity()) {
                // Fetch LTP and execute at market
                ArrayList<OHLC_Model> ohlc = process.fetchStockDataSync(order.getStock_name());
                if (!ohlc.isEmpty()) {
                    double ltp = Double.parseDouble(ohlc.get(0).getClose());
                    // Force execution at market to close position
                    Log.d("SQUARE_OFF", "Squaring off INTRADAY " + order.getStock_name() + " at " + ltp);
                    // In a real implementation, we would create a counter-order here.
                }
            }
        }
        
        odb.close();
        return Result.success();
    }
}
