package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.TradeHistoryModel;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfitLossActivity extends AppCompatActivity {

    private TextView grossPlTxt, roiTxt, currentMonthTxt, tradesCountTxt;
    private GridLayout calendarGrid;
    private RecyclerView historyRecycler;
    private FundDatabase fdb;
    private List<TradeHistoryModel> historyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profit_loss);

        fdb = new FundDatabase(this);
        initViews();
        setupFilters();
        loadData("all");
    }

    private void initViews() {
        grossPlTxt = findViewById(R.id.gross_pl_txt);
        roiTxt = findViewById(R.id.roi_txt);
        currentMonthTxt = findViewById(R.id.current_month_txt);
        tradesCountTxt = findViewById(R.id.trades_count_txt);
        calendarGrid = findViewById(R.id.calendar_grid);
        historyRecycler = findViewById(R.id.history_recycler);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        View downloadBtn = findViewById(R.id.download_btn);
        if (downloadBtn != null) {
            downloadBtn.setOnClickListener(v -> downloadReport());
        }
    }

    private void setupFilters() {
        View realised = findViewById(R.id.btn_realised);
        if (realised != null) realised.setOnClickListener(v -> loadData("all"));
        
        View yesterday = findViewById(R.id.btn_yesterday);
        if (yesterday != null) yesterday.setOnClickListener(v -> loadData("yesterday"));
        
        View last7 = findViewById(R.id.btn_last_7_days);
        if (last7 != null) last7.setOnClickListener(v -> loadData("7days"));
        
        View lastMonth = findViewById(R.id.btn_last_1_month);
        if (lastMonth != null) lastMonth.setOnClickListener(v -> loadData("1month"));
    }

    private void loadData(String period) {
        if (fdb == null) return;
        List<TradeHistoryModel> data = fdb.getTradeHistory();
        historyList = data != null ? data : new ArrayList<>();
        
        updateSummary();
        setupHeatmap();
        setupRecyclerView();
    }

    private void updateSummary() {
        double totalPl = 0;
        double totalInvested = 0;
        for (TradeHistoryModel trade : historyList) {
            totalPl += trade.getPl();
            totalInvested += trade.getBuyAmt();
        }

        if (grossPlTxt != null) {
            grossPlTxt.setText(String.format(Locale.getDefault(), "₹%.2f", totalPl));
            grossPlTxt.setTextColor(totalPl >= 0 ? Color.parseColor("#0F9715") : Color.RED);
        }
        
        if (roiTxt != null) {
            double roi = totalInvested > 0 ? (totalPl / totalInvested) * 100 : 0;
            roiTxt.setText(String.format(Locale.getDefault(), "%.2f%%", roi));
        }

        if (tradesCountTxt != null) {
            tradesCountTxt.setText(String.format(Locale.getDefault(), "%d trades", historyList.size()));
        }
        
        if (currentMonthTxt != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
            currentMonthTxt.setText(sdf.format(new Date()));
        }
    }

    private void setupHeatmap() {
        if (calendarGrid == null) return;
        calendarGrid.removeAllViews();
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = 1; i <= daysInMonth; i++) {
            TextView dayView = new TextView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.height = 80;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(4, 4, 4, 4);
            dayView.setLayoutParams(params);
            dayView.setText(String.valueOf(i));
            dayView.setGravity(android.view.Gravity.CENTER);
            dayView.setBackgroundColor(Color.parseColor("#F5F5F5"));
            
            String dayPrefix = i + "/";
            for (TradeHistoryModel trade : historyList) {
                if (trade.getDate().startsWith(dayPrefix)) {
                    dayView.setBackgroundColor(trade.getPl() >= 0 ? Color.parseColor("#E8F5E9") : Color.parseColor("#FFEBEE"));
                }
            }
            calendarGrid.addView(dayView);
        }
    }

    private void setupRecyclerView() {
        if (historyRecycler == null) return;
        historyRecycler.setLayoutManager(new LinearLayoutManager(this));
        historyRecycler.setAdapter(new RecyclerView.Adapter<HistoryViewHolder>() {
            @androidx.annotation.NonNull
            @Override
            public HistoryViewHolder onCreateViewHolder(@androidx.annotation.NonNull android.view.ViewGroup parent, int viewType) {
                View v = getLayoutInflater().inflate(R.layout.item_trade_history, parent, false);
                return new HistoryViewHolder(v);
            }

            @Override
            public void onBindViewHolder(@androidx.annotation.NonNull HistoryViewHolder holder, int position) {
                TradeHistoryModel trade = historyList.get(position);
                holder.name.setText(trade.getName());
                // Fix: Quantity is double, using %.2f to avoid crash. 
                // Using .0f if we want to show it as integer for stocks.
                if (trade.getQuantity() == (long) trade.getQuantity()) {
                    holder.qty.setText(String.format(Locale.getDefault(), "Qty. %d", (long) trade.getQuantity()));
                } else {
                    holder.qty.setText(String.format(Locale.getDefault(), "Qty. %.2f", trade.getQuantity()));
                }
                holder.pl.setText(String.format(Locale.getDefault(), "₹%.2f (%.2f%%)", trade.getPl(), trade.getPercent()));
                holder.pl.setTextColor(trade.getPl() >= 0 ? Color.parseColor("#0F9715") : Color.RED);
                holder.buyAvg.setText(String.format(Locale.getDefault(), "₹%.2f", trade.getBuyAvg()));
                holder.sellAvg.setText(String.format(Locale.getDefault(), "₹%.2f", trade.getSellAvg()));
                holder.buyAmt.setText(String.format(Locale.getDefault(), "₹%.2f", trade.getBuyAmt()));
                holder.sellAmt.setText(String.format(Locale.getDefault(), "₹%.2f", trade.getSellAmt()));
            }

            @Override
            public int getItemCount() { return historyList.size(); }
        });
    }

    private void downloadReport() {
        StringBuilder csvData = new StringBuilder("Date,Symbol,Qty,Buy Avg,Sell Avg,P&L\n");
        for (TradeHistoryModel trade : historyList) {
            csvData.append(trade.getDate()).append(",")
                  .append(trade.getName()).append(",")
                  .append(trade.getQuantity()).append(",")
                  .append(trade.getBuyAvg()).append(",")
                  .append(trade.getSellAvg()).append(",")
                  .append(trade.getPl()).append("\n");
        }

        try {
            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "TradeReport_" + System.currentTimeMillis() + ".csv");
            FileOutputStream out = new FileOutputStream(file);
            out.write(csvData.toString().getBytes());
            out.close();
            Toast.makeText(this, "Report saved to Downloads folder", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to download", Toast.LENGTH_SHORT).show();
        }
    }

    private static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView name, qty, pl, buyAvg, sellAvg, buyAmt, sellAmt;
        HistoryViewHolder(View v) {
            super(v);
            name = v.findViewById(R.id.hist_stock_name);
            qty = v.findViewById(R.id.hist_qty);
            pl = v.findViewById(R.id.hist_pl);
            buyAvg = v.findViewById(R.id.hist_buy_avg);
            sellAvg = v.findViewById(R.id.hist_sell_avg);
            buyAmt = v.findViewById(R.id.hist_buy_amt);
            sellAmt = v.findViewById(R.id.hist_sell_amt);
        }
    }
}
