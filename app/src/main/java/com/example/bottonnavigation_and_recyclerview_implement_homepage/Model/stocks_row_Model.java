package com.example.bottonnavigation_and_recyclerview_implement_homepage.Model;

import android.widget.Button;

public class stocks_row_Model {

    private String stocks_name;
    private String stocks_price;
    private String percentile;
    private String todayChange;

    public stocks_row_Model(String stocks_name, String stocks_price, String percentile, String todayChange){
        this.stocks_name = stocks_name;
        this.stocks_price = stocks_price;
        this.percentile = percentile;
        this.todayChange = todayChange;
    }

    public String getStocks_name() {
        return stocks_name;
    }

    public String getStocks_price() {
        return stocks_price;
    }

    public String getPercentile() {
        return percentile;
    }

    public String getTodayChange() {
        return todayChange;
    }
}
