package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OHLC_Database;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.StocksRow_DatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;

import java.util.ArrayList;

public class StockDataProcessor {

    public static void processAndStore(Context context, String symbol) {
        OHLC_Database ohlcDb = new OHLC_Database(context);
        ArrayList<OHLC_Model> ohlcList = ohlcDb.getAllOHLC();
        ArrayList<String> pre_close = ohlcDb.getAllPrevClose();

        if (ohlcList.isEmpty()) {return;}

        // Filter only for the given symbol
        OHLC_Model latest = null;
        String preClose = null;
        int track = 0;
        for (OHLC_Model model : ohlcList) {
            if (model.getName().equals(symbol)) {
                latest = model;
                preClose = pre_close.get(track);
            }
            track+=1;
        }

        double close = Double.parseDouble(latest.getClose());
        double open = Double.parseDouble(latest.getOpen());
        Double previousColse = Double.parseDouble(preClose);

        double todayChange = close - previousColse;
        double percentile = previousColse != 0 ? (todayChange / open) * 100 : 0;

        stocks_row_Model stockRow = new stocks_row_Model(
                symbol,
                String.format("%.2f", close),
                String.format("%.2f", percentile),
                String.format("%.2f", todayChange)
        );

        StocksRow_DatabaseHelper stockDb = new StocksRow_DatabaseHelper(context);
        stockDb.insertOrUpdateStock(stockRow); // cleaner
    }
}
