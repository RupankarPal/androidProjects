package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OHLC_Database;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;

import java.util.HashMap;

public class YahooStockDataFeatch {

    // In-memory cache for fast retrieval
    private static final HashMap<String, ArrayList<OHLC_Model>> dataCache = new HashMap<>();

    public interface StockDataCallback {
        void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change);
        void onFailure(String symbol, String error);
    }

    public static Boolean isSymbolExistInDB(OHLC_Model fetchData, ArrayList<OHLC_Model> DbData){
        if (DbData == null) return false;
        for (OHLC_Model model : DbData) {
            if (model.getName().equals(fetchData.getName())) {
                return true;
            }
        }
        return false;
    }


    public static void fetchStockData(Context context, String symbol, StockDataCallback callback) {
        fetchStockData(context, symbol, "1d", "2d", callback);
    }

    public static void fetchStockData(Context context, String symbol, String interval, String range, StockDataCallback callback) {
        // Check cache first for "immediate" feedback if data is fresh enough (e.g. less than 1 min old)
        // For simplicity in this demo, we'll just check if it exists.
        if (dataCache.containsKey(symbol + interval + range)) {
            ArrayList<OHLC_Model> cached = dataCache.get(symbol + interval + range);
            if (cached != null && !cached.isEmpty()) {
                // We still fetch fresh data but callback immediately with cached
                callback.onSuccess(symbol, cached, "0.00", "0.00");
            }
        }

        String encodedSymbol = symbol;
        try {
            encodedSymbol = java.net.URLEncoder.encode(symbol, "UTF-8");
        } catch (Exception e) {
            Log.e("API_DEBUG", "Encoding failed: " + e.getMessage());
        }
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + encodedSymbol + "?interval=" + interval + "&range=" + range;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0") 
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
                        ArrayList<OHLC_Model> ohlcList = new ArrayList<>();

                        JSONObject root = new JSONObject(jsonData);
                        JSONObject chart = root.getJSONObject("chart");
                        JSONArray resultArray = chart.getJSONArray("result");
                        JSONObject result = resultArray.getJSONObject(0);
                        
                        JSONObject meta = result.getJSONObject("meta");
                        double prevClose = meta.optDouble("chartPreviousClose", 0.0);

                        JSONObject indicators = result.getJSONObject("indicators");
                        JSONArray quoteArray = indicators.getJSONArray("quote");
                        JSONObject quote = quoteArray.getJSONObject(0);

                        JSONArray opens = quote.getJSONArray("open");
                        JSONArray highs = quote.getJSONArray("high");
                        JSONArray lows = quote.getJSONArray("low");
                        JSONArray closes = quote.getJSONArray("close");
                        JSONArray volumes = quote.getJSONArray("volume");
                        
                        // Timestamps for accurate chart
                        JSONArray timestamps = result.optJSONArray("timestamp");

                        double regularPrice = meta.optDouble("regularMarketPrice", 0.0);
                        
                        for (int i = 0; i < closes.length(); i++) {
                            if (closes.isNull(i)) continue;
                            
                            double o = opens.optDouble(i, prevClose);
                            double h = highs.optDouble(i, o);
                            double l = lows.optDouble(i, o);
                            double c = closes.optDouble(i, o);
                            double v = volumes.optDouble(i, 0.0);
                            long t = (timestamps != null) ? timestamps.optLong(i, 0) : 0;

                            // If this is the last item and we have a regularPrice, use it for accuracy
                            if (i == closes.length() - 1 && regularPrice > 0) {
                                c = regularPrice;
                            }

                            OHLC_Model model = new OHLC_Model(
                                    String.format(Locale.getDefault(), "%.5f", o),
                                    String.format(Locale.getDefault(), "%.5f", h),
                                    String.format(Locale.getDefault(), "%.5f", l),
                                    String.format(Locale.getDefault(), "%.5f", c),
                                    String.format(Locale.getDefault(), "%.5f", v),
                                    symbol
                            );
                            // We use volume field to store timestamp for ChartActivity to consume if needed
                            if (t > 0) model.setVolume(String.valueOf(t)); 
                            ohlcList.add(model);
                        }

                        if (ohlcList.isEmpty()) {
                            callback.onFailure(symbol, "No data found");
                            return;
                        }

                        // Update Cache
                        dataCache.put(symbol + interval + range, ohlcList);

                        OHLC_Database db = new OHLC_Database(context);
                        OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
                        
                        if (isSymbolExistInDB(latest, db.getAllOHLC())){
                            db.UpdateOHLCList(latest, String.valueOf(prevClose));
                        } else {
                            db.insertOHLCList(latest, String.valueOf(prevClose));
                        }
                        
                        double currentPrice = Double.parseDouble(latest.getClose());
                        if (prevClose == 0 && closes.length() > 1) {
                            prevClose = closes.optDouble(0, currentPrice);
                        }
                        
                        double change = currentPrice - prevClose;
                        double percent = prevClose != 0 ? (change / prevClose) * 100 : 0;
                        
                        db.updateStockCache(symbol, 
                                String.format(Locale.getDefault(), "%.5f", currentPrice), 
                                String.format(Locale.getDefault(), "%.2f", percent), 
                                String.format(Locale.getDefault(), "%.5f", change));
                        db.close();

                        callback.onSuccess(symbol, ohlcList, String.format(Locale.getDefault(), "%.2f", percent), String.format(Locale.getDefault(), "%.5f", change));

                    } catch (Exception e) {
                        callback.onFailure(symbol, e.getMessage());
                        Log.e("API_DEBUG", "Parse Failure for " + symbol + ": " + e.getMessage());
                    }
                } else {
                    callback.onFailure(symbol, "HTTP failed: " + response.code());
                }
            }
        });
    }
}
