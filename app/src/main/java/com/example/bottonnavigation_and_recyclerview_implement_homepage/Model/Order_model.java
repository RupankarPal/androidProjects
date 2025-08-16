package com.example.bottonnavigation_and_recyclerview_implement_homepage.Model;

import java.io.Serializable;

public class Order_model implements Serializable {

    private double order_prise;
    private double stock_price;
    private double stock_quantity;
    private double exicuted_quantity;
    private double targate_price;
    private double sl_price;
    private String stock_name;
    private String Order_type;

    public Order_model(double order_prise, double stock_price, double stock_quantity, double exicuted_quantity, double targate_price, double sl_price, String stock_name, String Order_Type) {
        this.order_prise = order_prise;
        this.stock_price = stock_price;
        this.stock_quantity = stock_quantity;
        this.targate_price = targate_price;
        this.sl_price = sl_price;
        this.stock_name = stock_name;
        this.Order_type = Order_Type;
        this.exicuted_quantity = exicuted_quantity;
    }

    public double getExicuted_quantity() {
        return exicuted_quantity;
    }

    public String getOrder_type() {
        return Order_type;
    }

    public double getOrder_prise() {
        return order_prise;
    }

    public double getStock_price() {
        return stock_price;
    }

    public double getStock_quantity() {
        return stock_quantity;
    }

    public double getTargate_price() {
        return targate_price;
    }

    public double getSl_price() {
        return sl_price;
    }

    public String getStock_name() {
        return stock_name;
    }

    public void setOrder_prise(double order_prise) {
        this.order_prise = order_prise;
    }

    public void setStock_price(double stock_price) {
        this.stock_price = stock_price;
    }

    public void setStock_quantity(double stock_quantity) {
        this.stock_quantity = stock_quantity;
    }

    public void setTargate_price(double targate_price) {
        this.targate_price = targate_price;
    }

    public void setSl_price(double sl_price) {
        this.sl_price = sl_price;
    }

    public void setStock_name(String stock_name) {
        this.stock_name = stock_name;
    }

    public void setOrder_type(String order_type) {
        Order_type = order_type;
    }

    public void setExicuted_quantity(double exicuted_quantity) {
        this.exicuted_quantity = exicuted_quantity;
    }
}
