package com.example.bottonnavigation_and_recyclerview_implement_homepage.utils;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.tradingview.lightweightcharts.api.series.models.LineData;
import com.tradingview.lightweightcharts.api.series.models.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FinancialUtils {

    public static List<LineData> calculateEMA(List<OHLC_Model> ohlcList, int period) {
        List<LineData> emaList = new ArrayList<>();
        if (ohlcList == null || ohlcList.size() < period) {
            return emaList;
        }

        double multiplier = 2.0 / (period + 1);
        double ema = 0;

        // Calculate initial SMA for the first EMA value
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += Double.parseDouble(ohlcList.get(i).getClose());
        }
        ema = sum / period;

        // Add first EMA data point
        long firstTime = ohlcList.get(period - 1).getTimestamp();
        emaList.add(new LineData(new Time.Utc(firstTime), (float) ema));

        // Calculate remaining EMAs
        for (int i = period; i < ohlcList.size(); i++) {
            double close = Double.parseDouble(ohlcList.get(i).getClose());
            ema = (close - ema) * multiplier + ema;
            long time = ohlcList.get(i).getTimestamp();
            emaList.add(new LineData(new Time.Utc(time), (float) ema));
        }

        return emaList;
    }

    public static List<LineData> calculateSMA(List<OHLC_Model> ohlcList, int period) {
        List<LineData> smaList = new ArrayList<>();
        if (ohlcList == null || ohlcList.size() < period) return smaList;

        for (int i = period - 1; i < ohlcList.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += Double.parseDouble(ohlcList.get(j).getClose());
            }
            smaList.add(new LineData(new Time.Utc(ohlcList.get(i).getTimestamp()), (float) (sum / period)));
        }
        return smaList;
    }

    public static List<LineData> calculateVWAP(List<OHLC_Model> ohlcList) {
        List<LineData> vwapList = new ArrayList<>();
        if (ohlcList == null || ohlcList.isEmpty()) return vwapList;

        double totalVolumePrice = 0;
        double totalVolume = 0;

        for (OHLC_Model model : ohlcList) {
            double high = Double.parseDouble(model.getHigh());
            double low = Double.parseDouble(model.getLow());
            double close = Double.parseDouble(model.getClose());
            double volume = Double.parseDouble(model.getVolume());

            double typicalPrice = (high + low + close) / 3.0;
            totalVolumePrice += typicalPrice * volume;
            totalVolume += volume;

            if (totalVolume > 0) {
                vwapList.add(new LineData(new Time.Utc(model.getTimestamp()), (float) (totalVolumePrice / totalVolume)));
            }
        }
        return vwapList;
    }

    public static List<List<LineData>> calculateBollingerBands(List<OHLC_Model> ohlcList, int period, double stdDevMultiplier) {
        List<LineData> middle = new ArrayList<>();
        List<LineData> upper = new ArrayList<>();
        List<LineData> lower = new ArrayList<>();

        if (ohlcList == null || ohlcList.size() < period) {
            return java.util.Arrays.asList(middle, upper, lower);
        }

        for (int i = period - 1; i < ohlcList.size(); i++) {
            double sum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                sum += Double.parseDouble(ohlcList.get(j).getClose());
            }
            double avg = sum / period;

            double sumSquares = 0;
            for (int j = i - period + 1; j <= i; j++) {
                double diff = Double.parseDouble(ohlcList.get(j).getClose()) - avg;
                sumSquares += diff * diff;
            }
            double stdDev = Math.sqrt(sumSquares / period);

            Time time = new Time.Utc(ohlcList.get(i).getTimestamp());
            middle.add(new LineData(time, (float) avg));
            upper.add(new LineData(time, (float) (avg + stdDevMultiplier * stdDev)));
            lower.add(new LineData(time, (float) (avg - stdDevMultiplier * stdDev)));
        }

        return java.util.Arrays.asList(middle, upper, lower);
    }

    public static String formatVolume(long value) {
        if (value < 1000) return String.valueOf(value);
        int exp = (int) (Math.log(value) / Math.log(1000));
        char pre = "KMBT".charAt(exp - 1);
        return String.format(Locale.getDefault(), "%.1f%c", value / Math.pow(1000, exp), pre);
    }
}
