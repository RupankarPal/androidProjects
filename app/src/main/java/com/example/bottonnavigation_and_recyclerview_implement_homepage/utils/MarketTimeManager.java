package com.example.bottonnavigation_and_recyclerview_implement_homepage.utils;

import java.util.Calendar;
import java.util.TimeZone;

public class MarketTimeManager {

    public enum MarketType {
        INDIAN, US, CRYPTO, ALL
    }


    public static boolean isMarketOpen(MarketType type) {
        if (type == MarketType.CRYPTO) {
            return true; // Crypto is 24/7
        }

        Calendar cal;
        int dayOfWeek;
        int hour;
        int minute;

        if (type == MarketType.INDIAN) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return false;

            int timeInMinutes = hour * 60 + minute;
            // 9:15 AM to 3:30 PM (IST)
            return timeInMinutes >= (9 * 60 + 15) && timeInMinutes <= (15 * 60 + 30);

        } else if (type == MarketType.US) {
            cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
            dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            hour = cal.get(Calendar.HOUR_OF_DAY);
            minute = cal.get(Calendar.MINUTE);

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) return false;

            int timeInMinutes = hour * 60 + minute;
            // 9:30 AM to 4:00 PM ET
            return timeInMinutes >= (9 * 60 + 30) && timeInMinutes <= (16 * 60);
        }

        return false;
    }

    public static MarketType getMarketType(String symbol) {
        if (symbol == null) return MarketType.INDIAN;
        
        if (symbol.endsWith("-USD") || symbol.contains("BTC") || symbol.contains("ETH") || symbol.contains("BNB") || symbol.contains("DOGE")) {
            return MarketType.CRYPTO;
        } else if (symbol.startsWith("^") || symbol.endsWith(".NS") || symbol.endsWith(".BO")) {
            return MarketType.INDIAN;
        } else {
            // Default to US for symbols like AAPL, MSFT, etc.
            return MarketType.US;
        }
    }
}
