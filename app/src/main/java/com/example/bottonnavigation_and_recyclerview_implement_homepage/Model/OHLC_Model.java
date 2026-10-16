package com.example.bottonnavigation_and_recyclerview_implement_homepage.Model;

import java.io.Serializable;

public class OHLC_Model implements Serializable {
    private String open;
    private String high;
    private String low;
    private String close;
    private String volume;
    private String Name;
    private long timestamp;

    public OHLC_Model(String open, String high, String low, String close, String volume, String Name) {
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.Name = Name;
    }

    public OHLC_Model(String open, String high, String low, String close, String volume, String Name, long timestamp) {
        this(open, high, low, close, volume, Name);
        this.timestamp = timestamp;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getVolume() {
        return volume;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getOpen() {
        return open;
    }

    public String getHigh() {
        return high;
    }

    public String getLow() {
        return low;
    }

    public String getClose() {
        return close;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
