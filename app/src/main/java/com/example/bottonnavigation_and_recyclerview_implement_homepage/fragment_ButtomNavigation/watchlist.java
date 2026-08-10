package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.Recyclear_stacks_row_Adapter_watchlist;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.StocksRow_DatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.searchActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.MarketTimeManager;

import java.util.ArrayList;

public class watchlist extends Fragment {

    private ArrayList<stocks_row_Model> stocksRowModels_arr = new ArrayList<>();
    private RecyclerView recyclerView;
    private Recyclear_stacks_row_Adapter_watchlist adapter;
    private ImageView searchImgBtn;
    private Button btnIndian, btnUs, btnCrypto, btnIndex;
    private Context context;

    private Handler handler;
    private Runnable stockFetcher;

    // Predefined stock symbols for categories
    private final String[] indianSymbols = {"RELIANCE.NS", "TCS.NS", "HDFCBANK.NS", "INFY.NS", "ICICIBANK.NS", "SBIN.NS", "BHARTIARTL.NS", "ITC.NS"};
    private final String[] usSymbols = {"AAPL", "GOOGL", "MSFT", "AMZN", "TSLA", "NFLX", "META", "NVDA"};
    private final String[] cryptoSymbols = {"BTC-USD", "ETH-USD", "BNB-USD", "XRP-USD", "ADA-USD", "SOL-USD", "DOGE-USD", "DOT-USD"};
    private final String[] indexSymbols = {"^NSEI", "^NSEBANK", "^NSEMDCP50", "^CNXIT", "^BSESN"};

    private String[] currentSymbols = indianSymbols;
    private boolean isInitialCategoryFetch = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_watchlist, container, false);
        context = getActivity();

        // UI setup
        searchImgBtn = view.findViewById(R.id.search_img_btn);
        if (searchImgBtn != null) {
            searchImgBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, searchActivity.class);
                startActivity(intent);
            });
        }

        // Category Buttons
        btnIndian = view.findViewById(R.id.btn_indian);
        btnUs = view.findViewById(R.id.btn_us);
        btnCrypto = view.findViewById(R.id.btn_crypto);
        btnIndex = view.findViewById(R.id.btn_index);

        View.OnClickListener categoryListener = v -> {
            resetCategoryButtonColors();
            ((Button)v).setTextColor(getResources().getColor(R.color.violet, null));
            ((Button)v).setTypeface(null, Typeface.BOLD);
            
            int id = v.getId();
            if (id == R.id.btn_indian) currentSymbols = indianSymbols;
            else if (id == R.id.btn_us) currentSymbols = usSymbols;
            else if (id == R.id.btn_crypto) currentSymbols = cryptoSymbols;
            else if (id == R.id.btn_index) currentSymbols = indexSymbols;

            isInitialCategoryFetch = true;
            refreshWatchlist();
        };

        if (btnIndian != null) btnIndian.setOnClickListener(categoryListener);
        if (btnUs != null) btnUs.setOnClickListener(categoryListener);
        if (btnCrypto != null) btnCrypto.setOnClickListener(categoryListener);
        if (btnIndex != null) btnIndex.setOnClickListener(categoryListener);

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recicle_watchlist);
        adapter = new Recyclear_stacks_row_Adapter_watchlist(stocksRowModels_arr, context);
        if (recyclerView != null) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
        }

        refreshWatchlist();

        // Handler for repeated stock fetching
        handler = new Handler(Looper.getMainLooper());
        stockFetcher = new Runnable() {
            @Override
            public void run() {
                if (context == null || !isAdded()) return;
                
                MarketTimeManager.MarketType type = MarketTimeManager.getMarketType(currentSymbols[0]);
                boolean isOpen = MarketTimeManager.isMarketOpen(type);

                // Polling logic: Fetch if market is open OR if data is still missing ("--")
                boolean hasMissingData = false;
                for (stocks_row_Model model : stocksRowModels_arr) {
                    if (model.getStocks_price().equals("--")) {
                        hasMissingData = true;
                        break;
                    }
                }

                if (isOpen || isInitialCategoryFetch || hasMissingData) {
                    for (int i = 0; i < currentSymbols.length; i++) {
                        final String symbol = currentSymbols[i];
                        handler.postDelayed(() -> fetchAndUpdate(symbol), (long) i * 150);
                    }
                    isInitialCategoryFetch = false;
                }
                
                handler.postDelayed(this, isOpen ? 2000 : 5000);
            }
        };

        return view;
    }

    private void resetCategoryButtonColors() {
        int black = getResources().getColor(R.color.black, null);
        if (btnIndian != null) {
            btnIndian.setTextColor(black);
            btnIndian.setTypeface(null, Typeface.NORMAL);
        }
        if (btnUs != null) {
            btnUs.setTextColor(black);
            btnUs.setTypeface(null, Typeface.NORMAL);
        }
        if (btnCrypto != null) {
            btnCrypto.setTextColor(black);
            btnCrypto.setTypeface(null, Typeface.NORMAL);
        }
        if (btnIndex != null) {
            btnIndex.setTextColor(black);
            btnIndex.setTypeface(null, Typeface.NORMAL);
        }
    }

    private void refreshWatchlist() {
        if (context == null) return;
        stocksRowModels_arr.clear();
        StocksRow_DatabaseHelper db = new StocksRow_DatabaseHelper(context);
        ArrayList<stocks_row_Model> cached = db.getAllStocks();
        
        for (String symbol : currentSymbols) {
            boolean found = false;
            for (stocks_row_Model model : cached) {
                if (model.getStocks_name().equals(symbol)) {
                    stocksRowModels_arr.add(model);
                    found = true;
                    break;
                }
            }
            if (!found) {
                stocksRowModels_arr.add(new stocks_row_Model(symbol, "--", "0.00", "0.00"));
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (handler != null && stockFetcher != null) {
            isInitialCategoryFetch = true;
            handler.post(stockFetcher);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (handler != null && stockFetcher != null) {
            handler.removeCallbacks(stockFetcher);
        }
    }

    private void fetchAndUpdate(String symbol) {
        if (context == null || !isAdded()) return;
        
        YahooStockDataFeatch.fetchStockData(context, symbol, new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String fetchedSymbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (!isAdded() || getContext() == null) return;
                
                StocksRow_DatabaseHelper db = new StocksRow_DatabaseHelper(getContext());
                stocks_row_Model updatedStock = db.getStockBySymbol(fetchedSymbol);

                if (updatedStock != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        int position = findStockPosition(fetchedSymbol);
                        if (position != -1 && position < stocksRowModels_arr.size()) {
                            stocksRowModels_arr.set(position, updatedStock);
                            adapter.notifyItemChanged(position);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String symbol, String error) {
                Log.e("WATCHLIST", "Fetch Failed: " + symbol);
            }
        });
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
            handler.removeCallbacks(stockFetcher);
        }
    }
}
