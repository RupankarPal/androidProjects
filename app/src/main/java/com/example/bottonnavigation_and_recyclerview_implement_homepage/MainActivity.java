package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.My_acount;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.Portfolio;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.watchlist;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation.odders;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bn_view;
    ConstraintLayout container;
    private boolean doubleBackToExitPressedOnce = false;
    private String url;

    // Fragment loading function
    public void loadFragment(Fragment fragment, int itemId, Bundle info) {
        if (info != null) {
            fragment.setArguments(info);
        }
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        String tag = String.valueOf(itemId); // Tag fragment based on item ID
        ft.replace(R.id.main_activity, fragment, tag);
        ft.addToBackStack(tag); // Add to back stack to manage back navigation
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
        loadFragment(new odders(),R.id.odders,OrderInfo_bundle);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                loadFragment(selectedFragment, id, null);
                return true;
            }
        });

        if (savedInstanceState == null) {
            bn_view.setSelectedItemId(R.id.watchlist);
        }
    }


    // Back button double press function
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main_activity);

        if(doubleBackToExitPressedOnce){
            finish();
        }
        // Check if we are on the watchlist fragment
        if (currentFragment instanceof watchlist) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        } else {
            // If not on watchlist, load watchlist fragment and set bottom navigation selection
            getSupportFragmentManager().popBackStack(); // Navigate back to previous fragment
            bn_view.setSelectedItemId(R.id.watchlist);
        }
    }
}
