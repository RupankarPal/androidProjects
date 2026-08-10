package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.content.Context;
import android.util.Log;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.TradeHistoryModel;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.MarketTimeManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class order_process {

    private final Context context;

    public order_process(Context context) {
        this.context = context;
    }

    public ArrayList<OHLC_Model> fetchStockDataSync(String symbol) {
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol + "?interval=1d&range=2d";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).addHeader("User-Agent", "Mozilla/5.0").build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JSONObject root = new JSONObject(jsonData);
                JSONObject result = root.getJSONObject("chart").getJSONArray("result").getJSONObject(0);
                JSONObject quote = result.getJSONObject("indicators").getJSONArray("quote").getJSONObject(0);
                JSONArray closes = quote.getJSONArray("close");
                Double close = closes.optDouble(closes.length() - 1, 0.0);

                ArrayList<OHLC_Model> list = new ArrayList<>();
                list.add(new OHLC_Model("0", "0", "0", String.format(Locale.getDefault(), "%.5f", close), "0", symbol));
                return list;
            }
        } catch (Exception e) {
            Log.e("ORDER_PROCESS", "Sync fetch failed: " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public ArrayList<Order_model> getOrderCurrentReport() {
        OrdersDatabaseHelper odb = new OrdersDatabaseHelper(context);
        ArrayList<Order_model> list = odb.getAllOrders();
        odb.close();
        return list;
    }

    private static final Object lock = new Object();
    private static final java.util.Set<Integer> processingOrders = new java.util.HashSet<>();

    public void matchPendingOrders() {
        synchronized (lock) {
            OrdersDatabaseHelper odb = new OrdersDatabaseHelper(context);
            FundDatabase fdb = new FundDatabase(context);
            ArrayList<Order_model> allOrders = odb.getAllOrders();

            for (Order_model order : allOrders) {
                // Skip if already executed or currently being processed by another thread
                if (order.getExicuted_quantity() >= order.getStock_quantity() || processingOrders.contains(order.getId())) {
                    continue;
                }

                MarketTimeManager.MarketType mType = MarketTimeManager.getMarketType(order.getStock_name());
                if (!MarketTimeManager.isMarketOpen(mType)) continue;

                ArrayList<OHLC_Model> ohlcList = fetchStockDataSync(order.getStock_name());
                if (ohlcList.isEmpty()) continue;
                
                double ltp = parseDoubleSafe(ohlcList.get(ohlcList.size() - 1).getClose());
                double limitPrice = order.getOrder_prise();
                double sl = order.getSl_price();
                double target = order.getTargate_price();
                double trailingSl = order.getTrailingSl();

                boolean shouldExecute = false;
                
                // Trailing SL logic
                if (trailingSl > 0) {
                    if (order.getOrder_type().equalsIgnoreCase("BUY")) {
                        double newSl = ltp - trailingSl;
                        if (newSl > sl) {
                            order.setSl_price(newSl);
                            odb.modifyOrder(order.getId(), order);
                            sl = newSl;
                        }
                    } else {
                        double newSl = ltp + trailingSl;
                        if (sl == 0 || newSl < sl) {
                            order.setSl_price(newSl);
                            odb.modifyOrder(order.getId(), order);
                            sl = newSl;
                        }
                    }
                }

                if (order.getOrder_type().equalsIgnoreCase("BUY")) {
                    if (limitPrice == 0 || ltp <= limitPrice) shouldExecute = true;
                    if (sl > 0 && ltp <= sl) shouldExecute = false; 
                } else {
                    if (limitPrice == 0 || ltp >= limitPrice) shouldExecute = true;
                    if (target > 0 && ltp >= target) shouldExecute = true; 
                    if (sl > 0 && ltp <= sl) shouldExecute = true; 
                }

                if (shouldExecute) {
                    processingOrders.add(order.getId());
                    try {
                        executeOrder(odb, fdb, order, ltp);
                    } finally {
                        processingOrders.remove(order.getId());
                    }
                }
            }
            odb.close();
            fdb.close();
        }
    }

    private void executeOrder(OrdersDatabaseHelper odb, FundDatabase fdb, Order_model order, double ltp) {
        order.setExicuted_quantity(order.getStock_quantity());
        odb.modifyOrder(order.getId(), order); 
        
        Calendar cal = Calendar.getInstance();
        String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
        
        double entryPrice = order.getStock_price(); // This is the basis price captured at order creation
        double qty = order.getStock_quantity();
        double pl;
        double percent;

        if (order.getOrder_type().equalsIgnoreCase("BUY")) {
            double currentQtyBefore = fdb.getAvailableQuantity(order.getStock_name());
            if (currentQtyBefore < -0.0001) {
                // Closing a SHORT position (Covering)
                // Profit = (Entry Price - Exit Price) * Qty
                pl = (entryPrice - ltp) * qty; 
                percent = entryPrice != 0 ? ((entryPrice - ltp) / entryPrice) * 100 : 0;
                fdb.addTradeToHistory(new TradeHistoryModel(order.getStock_name(), qty, entryPrice, ltp, entryPrice * qty, ltp * qty, pl, percent, date));
            }
            fdb.addStockToPortfolio(order.getStock_name(), ltp, order.getStock_quantity(), date, order.getProduct_type());
        } else {
            // SELL order
            double currentQtyBefore = fdb.getAvailableQuantity(order.getStock_name());
            if (currentQtyBefore > 0.0001) {
                // Closing a LONG position
                // Profit = (Exit Price - Entry Price) * Qty
                pl = (ltp - entryPrice) * qty; 
                percent = entryPrice != 0 ? ((ltp - entryPrice) / entryPrice) * 100 : 0;
                fdb.addTradeToHistory(new TradeHistoryModel(order.getStock_name(), qty, entryPrice, ltp, entryPrice * qty, ltp * qty, pl, percent, date));
            }
            fdb.sellStockFromPortfolio(order.getStock_name(), order.getStock_quantity(), ltp, date, order.getProduct_type());
        }
        Log.d("ORDER_ENGINE", "Executed " + order.getOrder_type() + " for " + order.getStock_name() + " at " + ltp);
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            String clean = str.replace("₹", "").replace("%", "").replace(",", "").replace("(", "").replace(")", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
