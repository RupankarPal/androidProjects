package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi;
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions;
import com.tradingview.lightweightcharts.api.series.models.CandlestickData;
import com.tradingview.lightweightcharts.api.series.models.Time;
import com.tradingview.lightweightcharts.view.ChartsView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kotlin.Unit;

public class ChartActivity extends AppCompatActivity {

    private ChartsView chartsView;
    private SeriesApi candlestickSeries;
    private TextView stockNameTxt, priceTxt, changeTxt;
    private String symbol;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        symbol = getIntent().getStringExtra("symbol");
        if (symbol == null) symbol = "RELIANCE.NS";

        chartsView = findViewById(R.id.chart_webview);
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

        setupChart();
        fetchInitialData();
        startRealtimeUpdates();
    }

    private void setupChart() {
        chartsView.getApi().addCandlestickSeries(
            new CandlestickSeriesOptions(),
            series -> {
                candlestickSeries = series;
                return Unit.INSTANCE;
            }
        );
    }

    private void fetchInitialData() {
        YahooStockDataFeatch.fetchStockData(this, symbol, "1d", "3mo", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                runOnUiThread(() -> {
                    updateUI(ohlcList, percent, change);
                    updateChartNative(ohlcList);
                });
            }

            @Override
            public void onFailure(String symbol, String error) {
                runOnUiThread(() -> {
                    Toast.makeText(ChartActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void startRealtimeUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchInitialData();
                handler.postDelayed(this, 10000); 
            }
        };
        handler.postDelayed(updateRunnable, 10000);
    }

    private void updateUI(ArrayList<OHLC_Model> ohlcList, String percent, String change) {
        if (ohlcList == null || ohlcList.isEmpty()) return;
        OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
        priceTxt.setText(latest.getClose());
        
        if (percent.equals("0.00") && ohlcList.size() > 1) {
             double current = Double.parseDouble(latest.getClose());
             double prev = Double.parseDouble(ohlcList.get(ohlcList.size()-2).getClose());
             double diff = current - prev;
             double p = (diff / prev) * 100;
             percent = String.format(Locale.getDefault(), "%.2f", p);
             change = String.format(Locale.getDefault(), "%.2f", diff);
        }

        changeTxt.setText(String.format("%s (%s%%)", change, percent));
        
        int color = Color.BLACK;
        try {
            double p = Double.parseDouble(percent);
            if (p > 0) color = getResources().getColor(R.color.Green, null);
            else if (p < 0) color = getResources().getColor(R.color.Red, null);
        } catch (Exception ignored) {}
        
        priceTxt.setTextColor(color);
        changeTxt.setTextColor(color);
    }

    private void updateChartNative(ArrayList<OHLC_Model> ohlcList) {
        if (ohlcList == null || ohlcList.isEmpty() || candlestickSeries == null) return;
        
        List<CandlestickData> dataList = new ArrayList<>();
        for (OHLC_Model model : ohlcList) {
            long time;
            try {
                time = Long.parseLong(model.getVolume());
            } catch (Exception e) {
                continue;
            }
            
            dataList.add(new CandlestickData(
                new Time.Utc(time),
                Float.parseFloat(model.getOpen()),
                Float.parseFloat(model.getHigh()),
                Float.parseFloat(model.getLow()),
                Float.parseFloat(model.getClose()),
                null, null, null
            ));
        }
        candlestickSeries.setData(dataList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}
