package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.Recyclear_stacks_row_Adapter_watchlist;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.StocksRow_DatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.StockDataProcessor;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.searchActivity;

import java.util.ArrayList;

public class watchlist extends Fragment {

    private ArrayList<stocks_row_Model> stocksRowModels_arr = new ArrayList<>();
    private RecyclerView recyclerView;
    private Recyclear_stacks_row_Adapter_watchlist adapter;
    private ImageView searchImgBtn, sortedImgBtn;
    private Context context;
    private RequestQueue requestQueue;

    private Handler handler;
    private Runnable stockFetcher;

    // Predefined stock symbols
    private final String[] stockSymbols = {
            "NVDA", "MSFT", "AAPL", "GOOGL", "AMZN",
            "META", "AVGO", "TSLA", "BRK.B", "TSM",
            "JPM", "WMT", "ORCL", "V", "LLY",
            "NFLX", "MA", "XOM", "COST", "JNJ",
            "PLTR", "HD", "ABBV", "PG", "BAC",
            "CVX", "KO", "AMD", "TMUS", "GE"
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);
        context = getActivity();
        requestQueue = Volley.newRequestQueue(context);

        // UI setup (no changes needed here)
        searchImgBtn = view.findViewById(R.id.search_img_btn);
        searchImgBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, searchActivity.class);
            startActivity(intent);
        });

        sortedImgBtn = view.findViewById(R.id.shorted_img_btn);
        sortedImgBtn.setOnClickListener(v ->
                Toast.makeText(context, "Work in progress", Toast.LENGTH_SHORT).show()
        );

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recicle_watchlist);
        adapter = new Recyclear_stacks_row_Adapter_watchlist(stocksRowModels_arr, context);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        // Initial data load from the database
        StocksRow_DatabaseHelper db = new StocksRow_DatabaseHelper(context);
        ArrayList<stocks_row_Model> initialStocks = db.getAllStocks();
        if (initialStocks.isEmpty()) {
            // If the database is empty, add placeholder items to prevent a crash
            // and prepare the list for updates.
            for (String symbol : stockSymbols) {
                stocks_row_Model placeholder = new stocks_row_Model(symbol, "--", "--", "--");
                stocksRowModels_arr.add(placeholder);
            }
            adapter.notifyDataSetChanged();
        } else {
            // If data exists, use it to populate the list
            stocksRowModels_arr.addAll(initialStocks);
            adapter.notifyDataSetChanged();
        }

        // Handler for repeated stock fetching
        handler = new Handler(Looper.getMainLooper());
        stockFetcher = new Runnable() {
            @Override
            public void run() {
                for (String symbol : stockSymbols) {
                    YahooStockDataFeatch.fetchStockData(context, symbol, new YahooStockDataFeatch.StockDataCallback() {
                        @Override
                        public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList) {
                            Log.d("WATCHLIST", "Fetched OHLC for: " + symbol);

                            // Process data into DB
                            StockDataProcessor.processAndStore(context, symbol);

                            // Retrieve the updated stock model from the database
                            StocksRow_DatabaseHelper db = new StocksRow_DatabaseHelper(context);
                            stocks_row_Model updatedStock = db.getStockBySymbol(symbol);

                            if (updatedStock != null) {
                                // Find and update the specific item in the list
                                int position = findStockPosition(symbol);
                                if (position != -1) {
                                    stocksRowModels_arr.set(position, updatedStock);
                                    adapter.notifyItemChanged(position);
                                }
                            }
                        }

                        @Override
                        public void onFailure(String symbol, String error) {
                            Log.e("WATCHLIST", "Failed for " + symbol + ": " + error);
                        }
                    });
                }
                handler.postDelayed(this, 2000);
            }
        };

        // Start fetching immediately
        handler.post(stockFetcher);

        return view;
    }

    private int findStockPosition(String symbol) {
        for (int i = 0; i < stocksRowModels_arr.size(); i++) {
            if (stocksRowModels_arr.get(i).getStocks_name().equals(symbol)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (handler != null && stockFetcher != null) {
            handler.removeCallbacks(stockFetcher); // Stop updates when fragment is destroyed
        }
    }
}