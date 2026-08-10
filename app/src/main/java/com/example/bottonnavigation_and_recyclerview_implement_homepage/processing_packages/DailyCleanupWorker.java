package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;

public class DailyCleanupWorker extends Worker {

    public DailyCleanupWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d("CLEANUP", "Starting daily order cleanup...");
        try {
            OrdersDatabaseHelper odb = new OrdersDatabaseHelper(getApplicationContext());
            odb.clearAllOrders();
            odb.close();
            return Result.success();
        } catch (Exception e) {
            Log.e("CLEANUP", "Error during cleanup: " + e.getMessage());
            return Result.failure();
        }
    }
}
