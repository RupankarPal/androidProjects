package com.example.bottonnavigation_and_recyclerview_implement_homepage.utils;

import android.content.Context;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;

import java.util.ArrayList;

public class CurrencyConverter {

    public static final double DEFAULT_USD_TO_INR_RATE = 95.5;

    @FunctionalInterface
    public interface RateCallback {
        void onRateRetrieved(double rateUsdToInr);
    }

    public static void fetchExchangeRate(Context context, RateCallback callback) {
        if (context == null) {
            callback.onRateRetrieved(DEFAULT_USD_TO_INR_RATE);
            return;
        }

        YahooStockDataFeatch.fetchStockData(context, "INR=X", "1d", "1d", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (ohlcList != null && !ohlcList.isEmpty()) {
                    try {
                        double rate = Double.parseDouble(ohlcList.get(ohlcList.size() - 1).getClose());
                        if (rate > 0) {
                            callback.onRateRetrieved(rate);
                            return;
                        }
                    } catch (Exception ignored) {}
                }
                callback.onRateRetrieved(DEFAULT_USD_TO_INR_RATE);
            }

            @Override
            public void onFailure(String symbol, String error) {
                callback.onRateRetrieved(DEFAULT_USD_TO_INR_RATE); // Graceful fallback
            }
        });
    }

    public static double convertInrToUsd(double inrAmount, double rateUsdToInr) {
        double rate = rateUsdToInr <= 0 ? DEFAULT_USD_TO_INR_RATE : rateUsdToInr;
        return inrAmount / rate;
    }

    public static double convertUsdToInr(double usdAmount, double rateUsdToInr) {
        double rate = rateUsdToInr <= 0 ? DEFAULT_USD_TO_INR_RATE : rateUsdToInr;
        return usdAmount * rate;
    }
}
