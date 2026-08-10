package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OHLC_Database;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.StocksRow_DatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;

import java.util.ArrayList;
import java.util.Locale;

public class StockDataProcessor {

    public static void processAndStore(Context context, String symbol) {
        OHLC_Database ohlcDb = new OHLC_Database(context);
        ArrayList<OHLC_Model> ohlcList = ohlcDb.getAllOHLC();
        ArrayList<String> pre_close = ohlcDb.getAllPrevClose();

        if (ohlcList.isEmpty()) {
            ohlcDb.close();
            return;
        }

        // Filter only for the given symbol
        OHLC_Model latest = null;
        String preCloseStr = null;
        int track = 0;
        for (OHLC_Model model : ohlcList) {
            if (model.getName().equals(symbol)) {
                latest = model;
                if (track < pre_close.size()) {
                    preCloseStr = pre_close.get(track);
                }
                break;
            }
            track+=1;
        }

        if (latest == null || preCloseStr == null) {
            ohlcDb.close();
            return;
        }

        double close = parseDoubleSafe(latest.getClose());

        double previousClose = parseDoubleSafe(preCloseStr);

        double todayChange = close - previousClose;
        double percentile = previousClose != 0 ? (todayChange / previousClose) * 100 : 0;

        stocks_row_Model stockRow = new stocks_row_Model(
                symbol,
                String.format(Locale.getDefault(), "%.2f", close),
                String.format(Locale.getDefault(), "%.2f", percentile),
                String.format(Locale.getDefault(), "%.2f", todayChange)
        );

        StocksRow_DatabaseHelper stockDb = new StocksRow_DatabaseHelper(context);
        stockDb.insertOrUpdateStock(stockRow); 
        ohlcDb.close();
    }

    private static double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            return Double.parseDouble(str.replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
