package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import the correct Handler
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.Recyclear_stacks_row_Adapter_watchlist;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.searchActivity;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

public class watchlist extends Fragment {

    private ArrayList<stocks_row_Model> stocksRowModels_arr = new ArrayList<>();
    private RecyclerView recyclerView;
    private Recyclear_stacks_row_Adapter_watchlist adapter;
    private ImageView searchImgBtn, sortedImgBtn;
    private Context context;
    private RequestQueue requestQueue;

    private Runnable runnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);

        context = getActivity();    //context featching the activity

        // Set up search button
        searchImgBtn = view.findViewById(R.id.search_img_btn);
        searchImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, searchActivity.class);
            startActivity(intent);
        });

        // Set up sorted button
        sortedImgBtn = view.findViewById(R.id.shorted_img_btn);
        sortedImgBtn.setOnClickListener(v -> Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show());

        recyclerView = view.findViewById(R.id.recicle_watchlist);
        adapter = new Recyclear_stacks_row_Adapter_watchlist(stocksRowModels_arr, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        // Initialize Volley request queue
        requestQueue = Volley.newRequestQueue(context);

    /*    String[] stockSymbols = {
                "AAPL", "GOOG", "MSFT", "TSLA", "AMZN", "META", "NFLX", "NVDA", "ADBE", "INTC",
                "TCS.NS", "INFY.NS", "RELIANCE.NS", "HDFCBANK.NS", "ICICIBANK.NS", // Indian stocks need .NS
        };

        Handler handlerContinusFeatching = new Handler(Looper.getMainLooper());
        int delayed_1 = 100;       // for 1st handler
        boolean TFLoop = true;
        while(TFLoop) {

            Calendar calendar = Calendar.getInstance();
            int h,min;
            h=calendar.get(Calendar.HOUR_OF_DAY);
            min=calendar.get(Calendar.MINUTE);
            if(h==15&&min==30||h>15){
                TFLoop = false;
            }

            handlerContinusFeatching.postDelayed(new Runnable() {
                @Override
                public void run() {
                    for (String symbol : stockSymbols) {
                        Handler handlerToDelayFeatching = new Handler(Looper.getMainLooper());
                        int delayed_2 = 500;        //for 2nd handler
                        handlerToDelayFeatching.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                YahooStockDataFeatch.fetchStockData(symbol, new YahooStockDataFeatch.StockDataCallback() {
                                    @Override
                                    public void onSuccess(String symbol, JSONObject stockData) {

                                    }

                                    @Override
                                    public void onFailure(String symbol, String error) {
                                        Log.e("ERROR", "Failed for " + symbol + ": " + error);
                                    }
                                });
                            }
                        }, delayed_2);

                    }
                }
            }, delayed_1);
        }*/
        return view;
    }

}
