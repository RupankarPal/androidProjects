package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.odders;

import java.util.ArrayList;

public class OrderWorker extends Worker {

    public OrderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        order_process process = new order_process(getApplicationContext());
        
        // Execute background order matching
        process.matchPendingOrders();

        ArrayList<Order_model> result = process.getOrderCurrentReport();
        
        // Update the static list in odders (following the original design intent)
        odders.setAfterProcessOrderArr(result);
        
        return Result.success();
    }

}
