package com.example.bottonnavigation_and_recyclerview_implement_homepage.Model;

public class TradeHistoryModel {
    private int id;
    private String name;
    private double quantity;
    private double buyAvg;
    private double sellAvg;
    private double buyAmt;
    private double sellAmt;
    private double pl;
    private double percent;
    private String date;

    public TradeHistoryModel(String name, double quantity, double buyAvg, double sellAvg, double buyAmt, double sellAmt, double pl, double percent, String date) {
        this.name = name;
        this.quantity = quantity;
        this.buyAvg = buyAvg;
        this.sellAvg = sellAvg;
        this.buyAmt = buyAmt;
        this.sellAmt = sellAmt;
        this.pl = pl;
        this.percent = percent;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public double getBuyAvg() { return buyAvg; }
    public double getSellAvg() { return sellAvg; }
    public double getBuyAmt() { return buyAmt; }
    public double getSellAmt() { return sellAmt; }
    public double getPl() { return pl; }
    public double getPercent() { return percent; }
    public String getDate() { return date; }
}
