package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;

import java.util.ArrayList;

public class PortfolioAdapter extends RecyclerView.Adapter<PortfolioAdapter.ViewHolder> {

    private ArrayList<portfolio_stocks_model> arrayList;
    private Context context;

    public PortfolioAdapter(Context context,ArrayList<portfolio_stocks_model> arrayList) {
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
        portfolio_stocks_model portfolioStocksModel = arrayList.get(position);

        holder.stockName.setText(portfolioStocksModel.getStocks_name());
        holder.stockPrice.setText(portfolioStocksModel.getStocks_price());
        holder.stockPercentile.setText(portfolioStocksModel.getPercentage_change());
        holder.quantity.setText(portfolioStocksModel.getStocks_quantity());
        holder.investedAmount.setText(portfolioStocksModel.getInvest_amount());
        holder.PL.setText(portfolioStocksModel.getPl_amount());
        holder.stockPerchesingPrice.setText(portfolioStocksModel.getPercheasing_price());


    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView stockName,stockPrice,stockPercentile,quantity,investedAmount,PL,stockPerchesingPrice;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            stockName = itemView.findViewById(R.id.stock_name_txt);
            stockPrice = itemView.findViewById(R.id.limit_price_txt);
            stockPercentile = itemView.findViewById(R.id.stock_percentage_txt);
            stockPerchesingPrice = itemView.findViewById(R.id.purchasing_price_txt);
            quantity = itemView.findViewById(R.id.Quentity_txt);
            investedAmount = itemView.findViewById(R.id.invested_amount_txt);
            PL = itemView.findViewById(R.id.PL_txt);
        }
    }
}
