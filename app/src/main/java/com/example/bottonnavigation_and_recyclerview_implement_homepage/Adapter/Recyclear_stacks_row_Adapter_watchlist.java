package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.BuySellOrderWindow;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.stocks_row_Model;

import java.util.ArrayList;

public class Recyclear_stacks_row_Adapter_watchlist extends RecyclerView.Adapter<Recyclear_stacks_row_Adapter_watchlist.ViewHolder> {

    Context context;
    ArrayList<stocks_row_Model> arr_stocks_row;

    // Constructor
    public Recyclear_stacks_row_Adapter_watchlist(ArrayList<stocks_row_Model> arr_stocks_row, Context context) {
        this.context = context;
        this.arr_stocks_row = arr_stocks_row;
    }

    // Method to update a single item
    public void updateItem(stocks_row_Model stock) {
        int index = -1;
        // Find the position of the stock in your list
        for (int i = 0; i < arr_stocks_row.size(); i++) {
            if (arr_stocks_row.get(i).getStocks_name().equals(stock.getStocks_name())) {
                index = i;
                break;
            }
        }
        // If the stock is found, update it and notify the adapter
        if (index != -1) {
            arr_stocks_row.set(index, stock);
            notifyItemChanged(index);
        }
    }

    // Method to add a single item
    public void addItem(stocks_row_Model stock) {
        arr_stocks_row.add(stock);
        notifyItemInserted(arr_stocks_row.size() - 1);
    }

    // Method to clear all items
    public void clearItems() {
        int size = arr_stocks_row.size();
        if (size > 0) {
            arr_stocks_row.clear();
            notifyItemRangeRemoved(0, size);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stocks_row_watchlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Check if the position is valid before accessing the list
        if (position >= 0 && position < arr_stocks_row.size()) {
            stocks_row_Model stocks_row_model = arr_stocks_row.get(position);
            Log.d("Adapter", "Binding position: " + position + " for stock: " + stocks_row_model.getStocks_name());

            holder.stocks_Name_txt.setText(stocks_row_model.getStocks_name());
            holder.stock_price_txt.setText(stocks_row_model.getStocks_price());
            holder.percentile_txt.setText("(" + stocks_row_model.getPercentile() + "%)");
            holder.todayChange_txt.setText(stocks_row_model.getTodayChange());

            holder.cardView.setOnClickListener(v -> {
                Intent intent = new Intent(context, BuySellOrderWindow.class);
                intent.putExtra("Stocks_name", stocks_row_model.getStocks_name());
                intent.putExtra("SPrice", stocks_row_model.getStocks_price());
                intent.putExtra("SPercentage", stocks_row_model.getPercentile());

                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return arr_stocks_row.size();
    }

    // ViewHolder class
    public class ViewHolder extends RecyclerView.ViewHolder {
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
}