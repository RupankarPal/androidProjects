package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment.fragment_dialog.BuySellDialogFragment;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class ChartActivity extends AppCompatActivity {

    private TextView stockNameTxt, priceTxt, changeTxt;
    private String symbol;

    private ChartFragment chartFragment;
    private FundamentalFragment fundamentalFragment;
    private AiAnalysisFragment aiAnalysisFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        symbol = getIntent().getStringExtra("symbol");
        if (symbol == null) symbol = "RELIANCE.NS";

        initViews();
        setupTabs();
    }

    private void initViews() {
        stockNameTxt = findViewById(R.id.chart_stock_name);
        priceTxt = findViewById(R.id.chart_price_txt);
        changeTxt = findViewById(R.id.chart_change_txt);

        stockNameTxt.setText(symbol);

        findViewById(R.id.chart_back_btn).setOnClickListener(v -> finish());

        findViewById(R.id.chart_buy_btn).setOnClickListener(v -> {
            BuySellDialogFragment dialog = BuySellDialogFragment.newInstance(symbol, priceTxt.getText().toString(), "0.00");
            dialog.show(getSupportFragmentManager(), "BuySellDialog");
        });

        findViewById(R.id.chart_sell_btn).setOnClickListener(v -> {
            BuySellDialogFragment dialog = BuySellDialogFragment.newInstance(symbol, priceTxt.getText().toString(), "0.00", "SELL", 0);
            dialog.show(getSupportFragmentManager(), "BuySellDialog");
        });
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.asset_detail_tab_layout);

        chartFragment = ChartFragment.newInstance(symbol);
        fundamentalFragment = FundamentalFragment.newInstance(symbol);
        aiAnalysisFragment = AiAnalysisFragment.newInstance(symbol);

        tabLayout.addTab(tabLayout.newTab().setText("Chart"));
        tabLayout.addTab(tabLayout.newTab().setText("Fundamental"));
        tabLayout.addTab(tabLayout.newTab().setText("AI Analysis"));

        // Default tab: Chart
        replaceFragment(chartFragment);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        replaceFragment(chartFragment);
                        break;
                    case 1:
                        replaceFragment(fundamentalFragment);
                        break;
                    case 2:
                        replaceFragment(aiAnalysisFragment);
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null && !isFinishing()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.asset_detail_fragment_container, fragment)
                    .commitAllowingStateLoss();
        }
    }

    public void updateHeaderUI(ArrayList<OHLC_Model> ohlcList, String percent, String change) {
        if (ohlcList == null || ohlcList.isEmpty()) return;
        OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
        if (priceTxt != null) priceTxt.setText(latest.getClose());
        if (changeTxt != null) {
            changeTxt.setText(String.format("%s (%s%%)", change, percent));
            int color = getPriceColor(percent);
            priceTxt.setTextColor(color);
            changeTxt.setTextColor(color);
        }
    }

    private int getPriceColor(String percent) {
        try {
            double p = Double.parseDouble(percent);
            if (p > 0) return getResources().getColor(R.color.Green, null);
            if (p < 0) return getResources().getColor(R.color.Red, null);
        } catch (Exception ignored) {}
        return Color.BLACK;
    }
}
