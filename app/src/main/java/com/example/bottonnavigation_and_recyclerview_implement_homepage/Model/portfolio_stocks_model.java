package com.example.bottonnavigation_and_recyclerview_implement_homepage.Model;

import java.io.Serializable;

public class portfolio_stocks_model implements Serializable {

    private String stocks_name;
    private String stocks_price;
    private String percentage_change;
    private String percheasing_price;
    private String stocks_quantity;
    private String invest_amount;
    private String pl_amount;
    private String daily_change;

    public portfolio_stocks_model(String stocks_name, String stocks_price, String percentage_change,
                                  String percheasing_price, String stocks_quantity, String invest_amount, String pl_amount, String daily_change) {

        this.stocks_name = stocks_name;
        this.stocks_price = stocks_price;
        this.percentage_change = percentage_change;
        this.percheasing_price = percheasing_price;
        this.stocks_quantity = stocks_quantity;
        this.invest_amount = invest_amount;
        this.pl_amount = pl_amount;
        this.daily_change = daily_change;
    }

    public String getDaily_change() {
        return daily_change;
    }


    public String getStocks_name() {
        return stocks_name;
    }

    public String getStocks_price() {
        return stocks_price;
    }

    public String getPercentage_change() {
        return percentage_change;
    }

    public String getPercheasing_price() {
        return percheasing_price;
    }

    public String getStocks_quantity() {
        return stocks_quantity;
    }

    public String getInvest_amount() {
        return invest_amount;
    }

    public String getPl_amount() {
        return pl_amount;
    }
}
