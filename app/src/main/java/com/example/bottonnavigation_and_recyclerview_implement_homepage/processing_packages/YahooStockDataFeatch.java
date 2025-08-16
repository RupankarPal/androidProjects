package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class YahooStockDataFeatch {

    public interface StockDataCallback {
        void onSuccess(String symbol, JSONObject stockData);
        void onFailure(String symbol, String error);
    }

    private static final String TAG = "YahooStockDataFeatch";

    public static void fetchStockData(String symbol, StockDataCallback callback) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=5d";

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(symbol, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);
                        callback.onSuccess(symbol, jsonObject);
                    } catch (Exception e) {
                        callback.onFailure(symbol, e.getMessage());
                    }
                } else {
                    callback.onFailure(symbol, "Response failed");
                }
            }
        });
    }
}
