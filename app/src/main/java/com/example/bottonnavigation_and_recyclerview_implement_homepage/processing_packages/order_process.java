package com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;

import java.util.ArrayList;
import java.util.Locale;

public class order_process {
    ArrayList<Order_model> order_models_array;
    ArrayList<Order_model> after_Process_arr;
    Context context;
    private final String CHANNEL_ID = "Order_process104";

    public order_process(Context context,ArrayList<Order_model> order_models_array) {
        this.order_models_array = order_models_array;
        this.context = context;
        createNotificationChannel();
    }

    // variables
    double order_prise;
    double stock_price;
    double stock_quantity;
    double exicuted_quantity;
    double targate_price;
    double sl_price;
    String stock_name;
    String Order_type;

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "MyChannelName";
            String description = "MyChannelDescription";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void showNotification(String stock_name, double price) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Order Exicuted")
                .setContentText(stock_name+" Exicuted at @"+price)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private boolean isOrderExicuted(double currrent_price, double wanted_price){    // order chercking

        if ( wanted_price <= currrent_price ){
            return true;
        }
        return false;
    }

    private boolean isTargateExicuted(double current_price, double wanted_price, String type){      // targate cheacking

        if (type.toUpperCase(Locale.ROOT)=="BUY"){
            if ( wanted_price <= current_price ) {
                return true;
            }
            return false;
        } else if (type.toUpperCase(Locale.ROOT) == "SELL") {
            if (wanted_price >= current_price){
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean isSlExicuted(double current_price, double wanted_price, String type){       // sl cheacking

        if (type.toUpperCase(Locale.ROOT)=="BUY"){
            if ( wanted_price >= current_price ) {
                return true;
            }
            return false;
        } else if (type.toUpperCase(Locale.ROOT) == "SELL") {
            if (wanted_price <= current_price){
                return true;
            }
            return false;
        }
        return false;
    }

    public ArrayList<Order_model> getOrderCurrentReport(){
        int index = 0;
        while (order_models_array!=null){
            order_prise = order_models_array.get(index).getOrder_prise();
            stock_price = order_models_array.get(index).getStock_price();
            stock_quantity = order_models_array.get(index).getStock_quantity();
            exicuted_quantity = order_models_array.get(index).getExicuted_quantity();
            targate_price = order_models_array.get(index).getTargate_price();
            sl_price = order_models_array.get(index).getSl_price();
            stock_name = order_models_array.get(index).getStock_name();
            Order_type = order_models_array.get(index).getOrder_type();

            if (exicuted_quantity!=stock_quantity){
                if (isOrderExicuted(stock_price,order_prise)){  // we exicute the order
                    exicuted_quantity=stock_quantity;
                    after_Process_arr.add(new Order_model(order_prise,stock_price,stock_quantity,exicuted_quantity,targate_price,sl_price,stock_name,Order_type));
                    showNotification(stock_name,order_prise);
                }
            }

            if (targate_price!=0){  // when we have targate price
                if (isTargateExicuted(stock_price,targate_price,Order_type)){
                    sl_price=0;   // we bolck the sl price to cheack it again
                    if (Order_type.toUpperCase(Locale.ROOT)=="BUY"){
                        Order_type="SELL";
                        after_Process_arr.add(new Order_model(order_prise,stock_price,stock_quantity,exicuted_quantity,targate_price,sl_price,stock_name,Order_type));
                    }
                    Order_type="BUY";
                    after_Process_arr.add(new Order_model(order_prise,stock_price,stock_quantity,exicuted_quantity,targate_price,sl_price,stock_name,Order_type));
                    showNotification(stock_name,targate_price);
                }
            }

            if (sl_price!=0){    // when we have sl price
                if (isSlExicuted(stock_price,sl_price,Order_type)){
                    targate_price = 0;  // we bolck the targate price to cheack it again
                    if (isTargateExicuted(stock_price,targate_price,Order_type)) {
                        sl_price = 0;   // we bolck the sl price to cheack it again
                        if (Order_type.toUpperCase(Locale.ROOT) == "BUY") {
                            Order_type = "SELL";
                            after_Process_arr.add(new Order_model(order_prise, stock_price, stock_quantity, exicuted_quantity, targate_price, sl_price, stock_name, Order_type));
                        }
                        Order_type = "BUY";
                        after_Process_arr.add(new Order_model(order_prise, stock_price, stock_quantity, exicuted_quantity, targate_price, sl_price, stock_name, Order_type));
                        showNotification(stock_name, sl_price);
                    }
                }
            }

        }
        return after_Process_arr;
    }

}
