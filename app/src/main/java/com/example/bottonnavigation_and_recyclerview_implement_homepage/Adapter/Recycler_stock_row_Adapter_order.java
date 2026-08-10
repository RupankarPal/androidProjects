package com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.OrderOptionsDialogFragment;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class Recycler_stock_row_Adapter_order extends RecyclerView.Adapter<Recycler_stock_row_Adapter_order.ViewHolder> {

    Context context;
    ArrayList<Order_model> orderInfo_arr;
    public Recycler_stock_row_Adapter_order(Context context, ArrayList<Order_model> orderInfo_arr) {
        this.context = context;
        this.orderInfo_arr = orderInfo_arr;
    }

    @NonNull
    @Override
    public Recycler_stock_row_Adapter_order.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.stock_row_orders,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull Recycler_stock_row_Adapter_order.ViewHolder holder, int position) {
        Order_model temp_model =  orderInfo_arr.get(position);
        double sl_price = temp_model.getSl_price();
        double targate_price = temp_model.getTargate_price();
        
        holder.sl_tick_img.setVisibility(View.GONE);
        holder.targate_tick_img.setVisibility(View.GONE);

        holder.stock_name.setText(temp_model.getStock_name());
        holder.orderType.setText(temp_model.getOrder_type());
        holder.targatePrice.setText(String.valueOf(temp_model.getTargate_price()));
        holder.slPrice.setText(String.valueOf(temp_model.getSl_price()));
        
        if (temp_model.getOrder_prise() == 0) {
            holder.limitPrice.setText("Market");
        } else {
            holder.limitPrice.setText(String.valueOf(temp_model.getOrder_prise()));
        }
        
        holder.executedQuantity.setText(String.valueOf(temp_model.getExicuted_quantity()));
        holder.bidQuantity.setText(String.valueOf(temp_model.getStock_quantity()));
        holder.time.setText(temp_model.getTime());

        if (sl_price==0||targate_price!=0){     // targate hit

            holder.targate_tick_img.setVisibility(View.VISIBLE);
        }

        if (targate_price==0||sl_price!=0) {     // sl hit
            holder.sl_tick_img.setVisibility(View.GONE);
        }

        // Only allow cancel/modify for OPEN orders
        if (temp_model.getExicuted_quantity() < temp_model.getStock_quantity()) {
            holder.itemView.setOnClickListener(v -> {
                if (context instanceof AppCompatActivity) {
                    OrderOptionsDialogFragment optionsDialog = OrderOptionsDialogFragment.newInstance(temp_model);
                    optionsDialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "OrderOptionsDialog");
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return orderInfo_arr.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView stock_name,orderType,time,targatePrice,slPrice,limitPrice,executedQuantity,bidQuantity;
        ImageView sl_tick_img,targate_tick_img;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            stock_name = itemView.findViewById(R.id.stock_name_txt);
            orderType = itemView.findViewById(R.id.order_type_BS_txt);
            time = itemView.findViewById(R.id.time_txt);
            targatePrice = itemView.findViewById(R.id.targate_txt);
            slPrice = itemView.findViewById(R.id.sl_txt);
            limitPrice =itemView.findViewById(R.id.limit_price_txt);
            executedQuantity = itemView.findViewById(R.id.exicuted_quantity_txt);
            bidQuantity = itemView.findViewById(R.id.bid_quantity_txt);
            sl_tick_img = itemView.findViewById(R.id.sl_tick_img);
            targate_tick_img = itemView.findViewById(R.id.targate_tick_img);
        }
    }
}
