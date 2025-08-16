package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrderDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;

import java.util.ArrayList;

public class BuySellOrderWindow extends AppCompatActivity {


    TextView stockName,stockPrise,stockChangeInPercent,targate_txt,sl_txt,total_cost_txt,balence_txt;
    ImageView back_btn,Qplus,Qminus;
    Button market_btn,limit_btn,smart_btn,buy,sell;
    EditText Qedt,Pedt,Tedt,sledt;
    ArrayList<Order_model> orderInfo_arr = new ArrayList<>();//arraylist for store order data and pass
    int orderType=1; // 1= market_btn order, 2= limit_btn order, 3= smart_btn order
    double exicuted_quantity = 0;

    // if the mode id not smart_btn
    private void notSmart_btn(){
        sledt.setVisibility(View.GONE);
        Tedt.setVisibility(View.GONE);
        targate_txt.setVisibility(View.GONE);
        sl_txt.setVisibility(View.GONE);
    }

    //upper surkit condition cheack
    private boolean isUpperSurkit(double limit_price){
        double sPrice = Double.parseDouble(stockPrise.getText().toString());
        double u_s_Price = sPrice + (sPrice*20/100);
        return limit_price == u_s_Price;
    }

    //lower surkit condition cheack
    private boolean isLowerSurkit(double limit_price){
        double sPrice = Double.parseDouble(stockPrise.getText().toString());
        double l_s_Price = sPrice - (sPrice*20/100);
        return limit_price == l_s_Price;
    }

    //inpit methods cheacking
    private boolean inputCheack(int orderType,Double sQ,Double limit_price,Double targate,Double sl) {
        if (orderType == 1) {
            if (sQ == 0 || sQ == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please fill the Quantity", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else if (orderType == 2) {
            if (sQ == 0 || sQ == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please fill the Quantity", Toast.LENGTH_SHORT).show();
                return false;
            } else if (limit_price == 0 || limit_price == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please set the Limit Price", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isUpperSurkit(limit_price)) {
                Toast.makeText(BuySellOrderWindow.this, "Limit price match the Upper Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isLowerSurkit(limit_price)) {
                Toast.makeText(BuySellOrderWindow.this, "Limit price match the Lower Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else if (orderType == 3) {
            if (sQ == 0 || sQ == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please fill the Quantity", Toast.LENGTH_SHORT).show();
                return false;
            } else if (limit_price == 0 || limit_price == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please set the Limit Price", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isUpperSurkit(limit_price)) {
                Toast.makeText(BuySellOrderWindow.this, "Limit price match the Upper Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isLowerSurkit(limit_price)) {
                Toast.makeText(BuySellOrderWindow.this, "Limit price match the Lower Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (targate == 0 || targate == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please set the Targate Price", Toast.LENGTH_SHORT).show();
                return false;
            }  else if (isUpperSurkit(targate)) {
                Toast.makeText(BuySellOrderWindow.this, "Targate price match the Upper Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isLowerSurkit(targate)) {
                Toast.makeText(BuySellOrderWindow.this, "Targate price match the Lower Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (sl == 0 || sl == null) {
                Toast.makeText(BuySellOrderWindow.this, "Please set the S/L Price", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isUpperSurkit(sl)) {
                Toast.makeText(BuySellOrderWindow.this, "S/L price match the Upper Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else if (isLowerSurkit(sl)) {
                Toast.makeText(BuySellOrderWindow.this, "S/L price match the Lower Circit price.", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                return true;
            }
        } else {
            Toast.makeText(BuySellOrderWindow.this, "input Cheacking Error", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //order place function
    private void orderPlace(int orderType,String type,String sName,double sPrice,double sQ, double limit_price,double targate,double sl){
        if (orderType==1){
            limit_price=sPrice;
            targate=0;
            sl=0;
        } else if (orderType == 3) {
            if(targate==0){
                Toast.makeText(BuySellOrderWindow.this, "Please set the Targate Price", Toast.LENGTH_SHORT).show();
                return;
            } else if (sl==0) {
                Toast.makeText(BuySellOrderWindow.this, "Please set the S/L Price", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // data add in database
        orderInfo_arr.add(new Order_model( limit_price, sPrice, sQ, exicuted_quantity, targate, sl, sName, type));
        OrderDatabaseHelper orderDatabaseHelper = new OrderDatabaseHelper(BuySellOrderWindow.this);
        orderDatabaseHelper.insertOrder(orderInfo_arr);

        //go to main activity
        orderInfo_arr.clear();
        finish();
    }

    // onCreate finction
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_buysell_order_window);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //veriable define
        stockName = findViewById(R.id.stock_name_txt);
        stockPrise = findViewById(R.id.limit_price_txt);
        stockChangeInPercent = findViewById(R.id.stock_percentage_txt);
        targate_txt = findViewById(R.id.targate_txt);
        sl_txt = findViewById(R.id.sl_txt);
        total_cost_txt = findViewById(R.id.total_cost_txt);
        balence_txt = findViewById(R.id.balence_txt);
        back_btn = findViewById(R.id.back_btn_img);
        Qplus = findViewById(R.id.Qplus);
        Qminus = findViewById(R.id.Qminus);
        market_btn = findViewById(R.id.market_btn);
        smart_btn = findViewById(R.id.smart_btn);
        limit_btn = findViewById(R.id.limit_btn);
        buy = findViewById(R.id.buy_btn);
        sell = findViewById(R.id.sell_btn);
        Qedt = findViewById(R.id.Qedt);
        Pedt = findViewById(R.id.pedt);
        Tedt = findViewById(R.id.targate_edt);
        sledt = findViewById(R.id.sl_edt);

        //back button
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //plus function
        Qplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double Q = parseDoubleSafe(Qedt.getText().toString());
                Q = Q +1;
                Qedt.setText(String.valueOf((int) Q));
            }
        });

        //minus function
        Qminus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double Q = parseDoubleSafe(Qedt.getText().toString());
                Q = Q -1;
                if (Q<0){
                    return;
                }
                Qedt.setText(String.valueOf((int) Q));
            }
        });


        Qedt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double Q = parseDoubleSafe(Qedt.getText().toString());
                double currentPrice = parseDoubleSafe(stockPrise.getText().toString());
                double totalCost = Q * currentPrice;
                total_cost_txt.setText(String.valueOf(totalCost+36));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        notSmart_btn(); //the smart_btn view is  not visible

        //default market_btn order type
        Pedt.setFocusable(false);
        Pedt.setClickable(false);
        Pedt.setCursorVisible(false);
        Pedt.setText("");
        Pedt.setHint("Market");
        market_btn.setBackgroundColor(getColor(R.color.violet));
        market_btn.setTextColor(getColor(R.color.white));

        //market_btn order
        market_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderType= 1;
                Pedt.setFocusable(false);
                Pedt.setClickable(false);
                Pedt.setCursorVisible(false);
                Pedt.setHint("Market");
                market_btn.setBackgroundColor(getColor(R.color.violet));
                market_btn.setTextColor(getColor(R.color.white));
                limit_btn.setTextColor(getColor(R.color.black));
                limit_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                smart_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                smart_btn.setTextColor(getColor(R.color.black));
                notSmart_btn();
            }
        });

        //limit_btn order
        limit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderType  = 2;
                Pedt.setFocusable(true);
                Pedt.setClickable(true);
                Pedt.setCursorVisible(true);
                Pedt.setHint("00.00");
                Pedt.setFocusableInTouchMode(true);
                market_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                market_btn.setTextColor(getColor(R.color.black));
                limit_btn.setTextColor(getColor(R.color.white));
                limit_btn.setBackgroundColor(getColor(R.color.violet));
                smart_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                smart_btn.setTextColor(getColor(R.color.black));
                notSmart_btn();
            }
        });

        //smart_btn order
        smart_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orderType  = 3;
                Pedt.setFocusable(true);
                sledt.setVisibility(View.VISIBLE);
                Tedt.setVisibility(View.VISIBLE);
                targate_txt.setVisibility(View.VISIBLE);
                sl_txt.setVisibility(View.VISIBLE);
                Pedt.setClickable(true);
                Pedt.setFocusableInTouchMode(true);
                market_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                market_btn.setTextColor(getColor(R.color.black));
                limit_btn.setBackgroundColor(getColor(R.color.Box_Colour));
                limit_btn.setTextColor(getColor(R.color.black));
                smart_btn.setTextColor(getColor(R.color.white));
                smart_btn.setBackgroundColor(getColor(R.color.violet));
                Pedt.setHint("00.00");
            }
        });

        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double balence = parseDoubleSafe(balence_txt.getText().toString());
                double totalCost = parseDoubleSafe(total_cost_txt.getText().toString());
                if (totalCost > balence) {
                    Toast.makeText(BuySellOrderWindow.this, "Not Sufficient Balance", Toast.LENGTH_SHORT).show();
                    return;
                }

                String sName = stockName.getText().toString();
                double sPrice = parseDoubleSafe(stockPrise.getText().toString());
                double sQ = parseDoubleSafe(Qedt.getText().toString());
                double limit_price = parseDoubleSafe(Pedt.getText().toString());
                double targate = parseDoubleSafe(Tedt.getText().toString());
                double sl = parseDoubleSafe(sledt.getText().toString());

                if (!inputCheack(orderType, sQ, limit_price, targate, sl)) {
                    return;
                }
                orderPlace(orderType, "BUY", sName, sPrice, sQ, limit_price, targate, sl);
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sName = stockName.getText().toString();
                double sPrice = parseDoubleSafe(stockPrise.getText().toString());
                double sQ = parseDoubleSafe(Qedt.getText().toString());
                double limit_price = parseDoubleSafe(Pedt.getText().toString());
                double targate = parseDoubleSafe(Tedt.getText().toString());
                double sl = parseDoubleSafe(sledt.getText().toString());

                if (!inputCheack(orderType, sQ, limit_price, targate, sl)) {
                    return;
                }
                orderPlace(orderType, "SELL", sName, sPrice, sQ, limit_price, targate, sl);
            }
        });


    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.trim().isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

}