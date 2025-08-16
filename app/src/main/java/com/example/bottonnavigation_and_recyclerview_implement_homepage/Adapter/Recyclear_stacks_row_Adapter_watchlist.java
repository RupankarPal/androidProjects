package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

    // Modify the constructor to accept and assign context
    public Recyclear_stacks_row_Adapter_watchlist(ArrayList<stocks_row_Model> arr_stocks_row, Context context) {
        this.context = context;  // Initialize context
        this.arr_stocks_row = arr_stocks_row;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the passed context to inflate the layout
        View view = LayoutInflater.from(context).inflate(R.layout.stocks_row_watchlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind the data to the views
        stocks_row_Model stocks_row_model = arr_stocks_row.get(position);
        Log.d("Adapter", "Binding position: " + position);
        holder.stocks_Name_txt.setText(stocks_row_model.getStocks_name());
        holder.stock_price_txt.setText(stocks_row_model.getStocks_price());
        holder.percentile_txt.setText( "(" + stocks_row_model.getPercentile() +"%)" );
        holder.todayChange_txt.setText( "(" + stocks_row_model.getTodayChange() +")" );

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, BuySellOrderWindow.class);
                intent.putExtra("Stocks_name",arr_stocks_row.get(position).getStocks_name());
                intent.putExtra("SPrice",arr_stocks_row.get(position).getStocks_price());       //SPrice mean stock price
                intent.putExtra("SPercentage",arr_stocks_row.get(position).getPercentile());
                Bundle bundle = new Bundle();
                startActivity(context,intent,bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("Adapter", "Item count: " + arr_stocks_row.size());
        return arr_stocks_row.size();
    }

    // ViewHolder class for managing individual rows

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView stocks_Name_txt, stock_price_txt;
        TextView percentile_txt,todayChange_txt;
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
