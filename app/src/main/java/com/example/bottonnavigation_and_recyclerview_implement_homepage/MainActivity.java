package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.content.SharedPreferences;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.validation.SecurityManager;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.validation.loginPage;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.My_acount;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.Portfolio;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.watchlist;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.odders;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.DailyCleanupWorker;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.SquareOffWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.razorpay.Checkout;
import com.razorpay.PaymentData;
import com.razorpay.PaymentResultWithDataListener;

public class MainActivity extends AppCompatActivity implements PaymentResultWithDataListener {

    BottomNavigationView bn_view;
    ConstraintLayout container;
    private boolean doubleBackToExitPressedOnce = false;
    private String url;
    private FirebaseAuth mAuth;
    private boolean isAuthVerified = false;

    @Override
    public void onPaymentSuccess(String s, PaymentData paymentData) {
        SubscriptionManager.setPremium(this, true);
        Toast.makeText(this, "Payment Successful! Premium Activated.", Toast.LENGTH_LONG).show();
        // Refresh the current fragment if it's My_acount
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_activity);
        if (currentFragment instanceof My_acount) {
            ((My_acount) currentFragment).refreshPremiumStatus();
        }
    }

    @Override
    public void onPaymentError(int i, String s, PaymentData paymentData) {
        Toast.makeText(this, "Payment Failed: " + s, Toast.LENGTH_SHORT).show();
    }

    // Fragment loading function
    public void loadFragment(Fragment fragment, int itemId, Bundle info, boolean addToBackStack) {
        if (!isAuthVerified) return; // Prevent loading fragments if security check failed
        if (info != null) {
            fragment.setArguments(info);
        }
        FragmentManager fm = getSupportFragmentManager();
        
        // CRITICAL: When switching tabs, clear the backstack to prevent navigation loops
        if (!addToBackStack) {
            while (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate();
            }
        }

        FragmentTransaction ft = fm.beginTransaction();
        String tag = String.valueOf(itemId);
        ft.replace(R.id.main_activity, fragment, tag);
        if (addToBackStack) {
            ft.addToBackStack(tag);
        }
        ft.commit();
    }

    // arraylist for order data
    ArrayList<Order_model> orderInfo_arr = new ArrayList<>();
    private boolean OrderData_Available(){
        orderInfo_arr = (ArrayList<Order_model>) getIntent().getSerializableExtra("OderInfo");
        if (orderInfo_arr == null) {
            return false;
        }
        Bundle OrderInfo_bundle = new Bundle();
        OrderInfo_bundle.putSerializable("OrderInfo_From_Main_Activity",orderInfo_arr);
        loadFragment(new odders(),R.id.odders,OrderInfo_bundle, true);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Checkout.preload(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // Not logged in, redirect to Login
            startActivity(new Intent(this, loginPage.class));
            finish();
            return;
        }

        // Logged in, show Biometric Prompt
        com.example.bottonnavigation_and_recyclerview_implement_homepage.validation.SecurityManager.promptBiometricAuth(this, new SecurityManager.AuthCallback() {
            @Override
            public void onAuthenticated() {
                isAuthVerified = true;
                runOnUiThread(() -> initMainActivity(savedInstanceState));
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Security Authentication Failed: " + error, Toast.LENGTH_LONG).show();
                finish(); // Close app if security check fails
            }
        });
    }

    private void initMainActivity(Bundle savedInstanceState) {
        // Bottom navigation code
        bn_view = findViewById(R.id.bottom_navigation);
        if (OrderData_Available()){
            bn_view.setSelectedItemId(R.id.odders);
        }
        bn_view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Fragment selectedFragment = null;

                if (id == R.id.watchlist) {
                    selectedFragment = new watchlist();
                } else if (id == R.id.odders) {
                    selectedFragment = new odders();
                } else if (id == R.id.portfolio) {
                    selectedFragment = new Portfolio();
                } else if (id == R.id.my_acount) {
                    selectedFragment = new My_acount();
                } else {
                    selectedFragment = new watchlist();
                }
                // Tabs should NOT be added to backstack to avoid navigation loops
                loadFragment(selectedFragment, id, null, false);
                return true;
            }
        });

        if (savedInstanceState == null) {
            bn_view.setSelectedItemId(R.id.watchlist);
            loadFragment(new watchlist(), R.id.watchlist, null, false); // Don't add first fragment to backstack
        }

        scheduleDailyCleanup();
        scheduleAutoSquareOff();
        checkMidnightCleanup();
    }

    private void checkMidnightCleanup() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String lastCleanupDate = prefs.getString("LastCleanupDate", "");
        java.util.Calendar cal = java.util.Calendar.getInstance();
        String currentDate = cal.get(java.util.Calendar.DAY_OF_MONTH) + "-" + cal.get(java.util.Calendar.MONTH) + "-" + cal.get(java.util.Calendar.YEAR);

        if (!lastCleanupDate.equals(currentDate)) {
            new Thread(() -> {
                OrdersDatabaseHelper odb = new OrdersDatabaseHelper(this);
                odb.clearAllOrders();
                odb.close();
                prefs.edit().putString("LastCleanupDate", currentDate).apply();
            }).start();
        }
    }

    private void scheduleDailyCleanup() {
        PeriodicWorkRequest cleanupRequest = new PeriodicWorkRequest.Builder(
                DailyCleanupWorker.class,
                24, java.util.concurrent.TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyOrderCleanup",
                ExistingPeriodicWorkPolicy.KEEP,
                cleanupRequest);
    }

    private void scheduleAutoSquareOff() {
        PeriodicWorkRequest squareOffRequest = new PeriodicWorkRequest.Builder(
                SquareOffWorker.class,
                24, java.util.concurrent.TimeUnit.HOURS)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AutoSquareOff",
                ExistingPeriodicWorkPolicy.KEEP,
                squareOffRequest);
    }



    // Back button double press function
    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            // Wait for pop to finish then sync UI
            new Handler().postDelayed(() -> {
                Fragment f = fm.findFragmentById(R.id.main_activity);
                if (f instanceof watchlist) bn_view.setSelectedItemId(R.id.watchlist);
                else if (f instanceof odders) bn_view.setSelectedItemId(R.id.odders);
                else if (f instanceof Portfolio) bn_view.setSelectedItemId(R.id.portfolio);
                else if (f instanceof My_acount) bn_view.setSelectedItemId(R.id.my_acount);
            }, 100);
            return;
        }

        Fragment currentFragment = fm.findFragmentById(R.id.main_activity);
        
        // If not on watchlist, go to watchlist first instead of exiting
        if (!(currentFragment instanceof watchlist)) {
            bn_view.setSelectedItemId(R.id.watchlist);
            loadFragment(new watchlist(), R.id.watchlist, null, false);
            return;
        }

        // On watchlist, handle exit logic
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed(); // standard exit
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
    }
}
