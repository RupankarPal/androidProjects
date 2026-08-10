package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.PortfolioAdapter;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.MarketTimeManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Portfolio extends Fragment {

    private static final HashMap<String, portfolio_stocks_model> uiCache = new HashMap<>();

    private TextView invest_amount_txt, current_amount_txt, total_pl_txt, todays_change_txt;
    private View emptyState;
    private RecyclerView recyclerView;
    private Button goToWatchlist_btn, pBtnAll, pBtnIndian, pBtnUs, pBtnCrypto;
    private PortfolioAdapter adapter;
    private final ArrayList<portfolio_stocks_model> allHoldings = new ArrayList<>();
    private final ArrayList<portfolio_stocks_model> filteredList = new ArrayList<>();
    private Context context;

    private MarketTimeManager.MarketType currentFilter = MarketTimeManager.MarketType.ALL;

    private Runnable runnable;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);
        context = getActivity();

        // Initialization
        recyclerView = view.findViewById(R.id.recyclerView);
        invest_amount_txt = view.findViewById(R.id.invested_amount_txt);
        current_amount_txt = view.findViewById(R.id.current_amount_txt);
        total_pl_txt = view.findViewById(R.id.total_pl_txt);
        todays_change_txt = view.findViewById(R.id.todays_change_txt);
        emptyState = view.findViewById(R.id.empty_state);
        goToWatchlist_btn = view.findViewById(R.id.goToWatchlist_btn);

        pBtnAll = view.findViewById(R.id.p_btn_all);
        pBtnIndian = view.findViewById(R.id.p_btn_indian);
        pBtnUs = view.findViewById(R.id.p_btn_us);
        pBtnCrypto = view.findViewById(R.id.p_btn_crypto);

        adapter = new PortfolioAdapter(context, filteredList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        setupCategoryListeners();

        goToWatchlist_btn.setOnClickListener(v -> {
            if (getActivity() != null) {
                BottomNavigationView bnView = getActivity().findViewById(R.id.bottom_navigation);
                bnView.setSelectedItemId(R.id.watchlist);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.main_activity, new watchlist());
                ft.commit();
            }
        });

        runnable = new Runnable() {
            @Override
            public void run() {
                if (isAdded() && getContext() != null) {
                    loadHoldings(); // Periodic refresh to remove sold items
                    fetchRealTimePrices();
                    handler.postDelayed(this, 5000); // Reduced frequency to 5s to save battery and lag
                }
            }
        };

        return view;
    }

    private void setupCategoryListeners() {
        View.OnClickListener listener = v -> {
            resetCategoryColors();
            ((Button)v).setTextColor(getResources().getColor(R.color.violet, null));
            ((Button)v).setTypeface(null, Typeface.BOLD);
            
            int id = v.getId();
            if (id == R.id.p_btn_all) currentFilter = MarketTimeManager.MarketType.ALL;
            else if (id == R.id.p_btn_indian) currentFilter = MarketTimeManager.MarketType.INDIAN;
            else if (id == R.id.p_btn_us) currentFilter = MarketTimeManager.MarketType.US;
            else if (id == R.id.p_btn_crypto) currentFilter = MarketTimeManager.MarketType.CRYPTO;
            
            applyFilter();
        };

        pBtnAll.setOnClickListener(listener);
        pBtnIndian.setOnClickListener(listener);
        pBtnUs.setOnClickListener(listener);
        pBtnCrypto.setOnClickListener(listener);
    }

    private void resetCategoryColors() {
        pBtnAll.setTextColor(Color.BLACK);
        pBtnIndian.setTextColor(Color.BLACK);
        pBtnUs.setTextColor(Color.BLACK);
        pBtnCrypto.setTextColor(Color.BLACK);
        
        pBtnAll.setTypeface(null, Typeface.NORMAL);
        pBtnIndian.setTypeface(null, Typeface.NORMAL);
        pBtnUs.setTypeface(null, Typeface.NORMAL);
        pBtnCrypto.setTypeface(null, Typeface.NORMAL);
    }

    private void loadHoldings() {
        new Thread(() -> {
            if (getContext() == null) return;
            com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase fdb = new com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase(getContext());
            ArrayList<portfolio_stocks_model> holdings = fdb.getPortfolioHoldings();
            fdb.close();
            
            new Handler(Looper.getMainLooper()).post(() -> {
                if (!isAdded()) return;
                allHoldings.clear();
                allHoldings.addAll(holdings);
                applyFilter();
                updateSummary();
            });
        }).start();
    }

    private void applyFilter() {
        filteredList.clear();
        for (portfolio_stocks_model stock : allHoldings) {
            // Apply Cache if exists
            portfolio_stocks_model cached = uiCache.get(stock.getStocks_name());
            portfolio_stocks_model toAdd = (cached != null) ? cached : stock;

            if (currentFilter == MarketTimeManager.MarketType.ALL || MarketTimeManager.getMarketType(toAdd.getStocks_name()) == currentFilter) {
                filteredList.add(toAdd);
            }
        }
        updateUI();
        adapter.notifyDataSetChanged();
    }

    private void fetchRealTimePrices() {
        // Fetch prices for all holdings to keep the global summary updated
        for (int i = 0; i < allHoldings.size(); i++) {
            portfolio_stocks_model stock = allHoldings.get(i);
            final String symbol = stock.getStocks_name();

            MarketTimeManager.MarketType mType = MarketTimeManager.getMarketType(symbol);
            if (!MarketTimeManager.isMarketOpen(mType)) continue;

            com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch.fetchStockData(getContext(), symbol, new com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch.StockDataCallback() {
                @Override
                public void onSuccess(String fetchedSymbol, ArrayList<com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model> ohlcList, String pPercent, String pChange) {
                    if (!isAdded() || ohlcList.isEmpty()) return;
                    com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
                    
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (!isAdded()) return;
                        
                        int globalPos = -1;
                        for (int j = 0; j < allHoldings.size(); j++) {
                            if (allHoldings.get(j).getStocks_name().equals(fetchedSymbol)) {
                                globalPos = j;
                                break;
                            }
                        }

                        if (globalPos != -1) {
                            portfolio_stocks_model currentStock = allHoldings.get(globalPos);
                            double ltp = parseDoubleSafe(latest.getClose());
                            double open = parseDoubleSafe(latest.getOpen());
                            double avgPrice = parseDoubleSafe(currentStock.getPercheasing_price());
                            double qty = parseDoubleSafe(currentStock.getStocks_quantity());
                            
                            double pl = (ltp - avgPrice) * qty;
                            double percent = avgPrice != 0 ? ((ltp - avgPrice) / avgPrice) * 100 : 0;
                            double dailyChangePercent = open != 0 ? ((ltp - open) / open) * 100 : 0;

                            portfolio_stocks_model updated = new portfolio_stocks_model(
                                    fetchedSymbol, 
                                    String.format(Locale.getDefault(), "₹%.5f", ltp),
                                    String.format(Locale.getDefault(), "%.2f%%", percent),
                                    currentStock.getPercheasing_price(),
                                    currentStock.getStocks_quantity(),
                                    currentStock.getInvest_amount(),
                                    String.format(Locale.getDefault(), "₹%.5f", pl),
                                    String.format(Locale.getDefault(), "%.2f%%", dailyChangePercent)
                            );


                            allHoldings.set(globalPos, updated);
                            uiCache.put(fetchedSymbol, updated); // Update Cache
                            
                            // Also update filtered list if it contains this stock
                            for (int k = 0; k < filteredList.size(); k++) {
                                if (filteredList.get(k).getStocks_name().equals(fetchedSymbol)) {
                                    filteredList.set(k, updated);
                                    adapter.notifyItemChanged(k);
                                    break;
                                }
                            }
                            updateSummary();
                        }
                    });
                }

                @Override
                public void onFailure(String symbol, String error) {}
            });
        }
    }

    private void updateSummary() {
        if (!isAdded() || getView() == null) return;
        
        double grandTotalInvested = 0;
        double grandTotalPL = 0;
        
        // Summing up everything from allHoldings for the Grand Total
        for (portfolio_stocks_model stock : allHoldings) {
            grandTotalInvested += parseDoubleSafe(stock.getInvest_amount());
            grandTotalPL += parseDoubleSafe(stock.getPl_amount());
        }
        
        double currentVal = grandTotalInvested + grandTotalPL;
        double totalReturnPercent = grandTotalInvested > 0 ? (grandTotalPL / grandTotalInvested) * 100 : 0;

        invest_amount_txt.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotalInvested));
        current_amount_txt.setText(String.format(Locale.getDefault(), "₹%.2f", currentVal));
        
        total_pl_txt.setText(String.format(Locale.getDefault(), "₹%.2f (%.2f%%)", grandTotalPL, totalReturnPercent));
        total_pl_txt.setTextColor(grandTotalPL >= 0 ? Color.parseColor("#0F9715") : Color.RED);

        todays_change_txt.setText("0.00 (0.00%)");
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            String clean = str.replace("₹", "").replace("%", "").replace(",", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHoldings();
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    private void updateUI() {
        if (!isAdded()) return;
        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
        }
    }
}
