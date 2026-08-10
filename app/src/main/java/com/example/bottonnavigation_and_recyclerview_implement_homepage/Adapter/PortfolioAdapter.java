package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;

import java.util.ArrayList;
import java.util.Locale;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private ArrayList<portfolio_stocks_model> arrayList;
    private Context context;

    public PortfolioAdapter(Context context, ArrayList<portfolio_stocks_model> arrayList) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stocks_row_portfolio, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        portfolio_stocks_model model = arrayList.get(position);

        holder.stockName.setText(model.getStocks_name());
        holder.stockPrice.setText(model.getStocks_price());
        
        // Format: (+0.45%) LTP
        holder.stockPercentile.setText(String.format(Locale.getDefault(), " (%s) LTP", model.getDaily_change()));
        
        holder.quantity.setText(model.getStocks_quantity());
        holder.investedAmount.setText(model.getInvest_amount());
        
        // Format: -393.36 (-2.48%)
        holder.PL.setText(String.format(Locale.getDefault(), "%s (%s)", model.getPl_amount(), model.getPercentage_change()));
        
        holder.avgPrice.setText(String.format(Locale.getDefault(), "%s Avg", model.getPercheasing_price()));

        // Color coding for Total P&L - Based on percentage change
        double plPercent = parseDoubleSafe(model.getPercentage_change());
        if (plPercent > 0) {
            holder.PL.setTextColor(Color.parseColor("#0F9715"));
        } else if (plPercent < 0) {
            holder.PL.setTextColor(Color.RED);
        } else {
            holder.PL.setTextColor(Color.BLACK);
        }

        // Color coding for LTP (Daily Change) - Based on Daily percentage change
        double dailyChangePercent = parseDoubleSafe(model.getDaily_change());
        int dailyColor = dailyChangePercent > 0 ? Color.parseColor("#0F9715") : (dailyChangePercent < 0 ? Color.RED : Color.BLACK);
        holder.stockPrice.setTextColor(dailyColor);
        holder.stockPercentile.setTextColor(dailyColor);

        // Click to open Detail Popup
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof AppCompatActivity) {
                com.example.bottonnavigation_and_recyclerview_implement_homepage.PositionDetailDialogFragment dialog = 
                    com.example.bottonnavigation_and_recyclerview_implement_homepage.PositionDetailDialogFragment.newInstance(model);
                dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "PositionDetailDialog");
            }
        });
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

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stockName, stockPrice, stockPercentile, quantity, investedAmount, PL, avgPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stockName = itemView.findViewById(R.id.stock_name_txt);
            stockPrice = itemView.findViewById(R.id.share_price_txt);
            stockPercentile = itemView.findViewById(R.id.percentile_txt);
            avgPrice = itemView.findViewById(R.id.purchasing_price_txt);
            quantity = itemView.findViewById(R.id.Quentity_txt);
            investedAmount = itemView.findViewById(R.id.invested_amount_txt);
            PL = itemView.findViewById(R.id.PL_txt);
        }
    }
}
