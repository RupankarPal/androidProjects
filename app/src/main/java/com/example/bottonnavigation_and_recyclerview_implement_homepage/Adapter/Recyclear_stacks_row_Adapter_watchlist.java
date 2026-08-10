package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.ChartActivity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;

import java.util.ArrayList;

public class Recyclear_stacks_row_Adapter_watchlist extends RecyclerView.Adapter<Recyclear_stacks_row_Adapter_watchlist.ViewHolder> {

    private final Context context;
    private final ArrayList<stocks_row_Model> arr_stocks_row;

    public Recyclear_stacks_row_Adapter_watchlist(ArrayList<stocks_row_Model> arr_stocks_row, Context context) {
        this.context = context;
        this.arr_stocks_row = arr_stocks_row;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stocks_row_watchlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= 0 && position < arr_stocks_row.size()) {
            stocks_row_Model model = arr_stocks_row.get(position);

            holder.stocks_Name_txt.setText(model.getStocks_name());
            holder.stock_price_txt.setText(model.getStocks_price());
            holder.percentile_txt.setText(String.format("(%s%%)", model.getPercentile()));
            holder.todayChange_txt.setText(model.getTodayChange());

            // Color logic: Red for negative change, Green for positive
            double changePercent = parseDoubleSafe(model.getPercentile());
            int color = context.getResources().getColor(R.color.black, null);
            
            if (changePercent > 0) {
                color = context.getResources().getColor(R.color.Green, null);
            } else if (changePercent < 0) {
                color = context.getResources().getColor(R.color.Red, null);
            }
            
            holder.stock_price_txt.setTextColor(color);
            holder.percentile_txt.setTextColor(color);
            holder.todayChange_txt.setTextColor(color);

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ChartActivity.class);
                intent.putExtra("symbol", model.getStocks_name());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return arr_stocks_row.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView stocks_Name_txt, stock_price_txt, percentile_txt, todayChange_txt;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            stocks_Name_txt = itemView.findViewById(R.id.stocks_Name_txt);
            stock_price_txt = itemView.findViewById(R.id.limit_price_txt);
            percentile_txt = itemView.findViewById(R.id.percentile_txt);
            todayChange_txt = itemView.findViewById(R.id.todayChange_txt);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            // CRITICAL: Preserve the negative sign!
            String clean = str.replace("₹", "").replace("%", "").replace(",", "").replace("+", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
