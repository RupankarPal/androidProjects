package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OHLC_Database;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;

public class YahooStockDataFeatch {

    public interface StockDataCallback {
        void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList);
        void onFailure(String symbol, String error);
    }

    public static void fetchStockData(Context context, String symbol, StockDataCallback callback) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=2d";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(symbol, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("API_DEBUG", "Response: " + response.toString());
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        Log.d("API_DEBUG", "Raw JSON: " + jsonData);
                        ArrayList<OHLC_Model> ohlcList = new ArrayList<>();

                        JSONObject root = new JSONObject(jsonData);
                        JSONObject chart = root.getJSONObject("chart");
                        JSONArray resultArray = chart.getJSONArray("result");
                        JSONObject result = resultArray.getJSONObject(0);

                        JSONObject indicators = result.getJSONObject("indicators");
                        JSONArray quoteArray = indicators.getJSONArray("quote");
                        JSONObject quote = quoteArray.getJSONObject(0);

                        JSONArray opens = quote.getJSONArray("open");
                        JSONArray highs = quote.getJSONArray("high");
                        JSONArray lows = quote.getJSONArray("low");
                        JSONArray closes = quote.getJSONArray("close");
                        JSONArray volumes = quote.getJSONArray("volume");

                        Double open = opens.getDouble(1);
                        Double high = highs.getDouble(1);
                        Double low = lows.getDouble(1);
                        Double close = closes.getDouble(1);
                        Double Prev_close = closes.getDouble(0);
                        Double volume = volumes.getDouble(1);

                        OHLC_Model temp = new OHLC_Model(
                                String.format("%.2f",open),
                                String.format("%.2f",high),
                                String.format("%.2f",low),
                                String.format("%.2f",close),
                                String.format("%.2f",volume),
                                String.valueOf(symbol)
                        );
                        ohlcList.add(temp);
                        // Store in database (insert if new, update if exists)
                        OHLC_Database db = new OHLC_Database(context);
                        db.insertOrUpdateOHLCList(ohlcList,String.valueOf(Prev_close));


                            Log.d("DB_DEBUG",
                                    "Name: " + symbol +
                                            " | Open: " + open +
                                            " | High: " + high +
                                            " | Low: " + low +
                                            " | Close: " + close +
                                            " | Volume: " + volume
                            );

                        callback.onSuccess(symbol, ohlcList);

                    } catch (Exception e) {
                        callback.onFailure(symbol, e.getMessage());
                        Log.e("API_DEBUG", "Network Failure: " + e.getMessage(), e);
                    }
                } else {
                    callback.onFailure(symbol, "HTTP failed");
                }
            }

        });
    }
}
