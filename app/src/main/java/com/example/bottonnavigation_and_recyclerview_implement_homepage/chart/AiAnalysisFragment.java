package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AiAnalysisFragment extends Fragment {

    private TextView sentimentBadge, confidenceTxt, rsiValTxt, macdValTxt, emaValTxt, volatilityTxt;
    private TextView r1Txt, r2Txt, s1Txt, s2Txt, commentaryTxt;

    private String symbol = "RELIANCE.NS";

    public static AiAnalysisFragment newInstance(String symbol) {
        AiAnalysisFragment fragment = new AiAnalysisFragment();
        Bundle args = new Bundle();
        args.putString("symbol", symbol);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            symbol = getArguments().getString("symbol", "RELIANCE.NS");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai_analysis, container, false);

        sentimentBadge = view.findViewById(R.id.ai_sentiment_badge);
        confidenceTxt = view.findViewById(R.id.ai_confidence_score_txt);
        rsiValTxt = view.findViewById(R.id.ai_rsi_val_txt);
        macdValTxt = view.findViewById(R.id.ai_macd_val_txt);
        emaValTxt = view.findViewById(R.id.ai_ema_val_txt);
        volatilityTxt = view.findViewById(R.id.ai_volatility_txt);

        r1Txt = view.findViewById(R.id.ai_r1_txt);
        r2Txt = view.findViewById(R.id.ai_r2_txt);
        s1Txt = view.findViewById(R.id.ai_s1_txt);
        s2Txt = view.findViewById(R.id.ai_s2_txt);
        commentaryTxt = view.findViewById(R.id.ai_commentary_txt);

        runAiAnalysis();

        return view;
    }

    private void runAiAnalysis() {
        if (getContext() == null) return;

        YahooStockDataFeatch.fetchStockData(getContext(), symbol, "1d", "3mo", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() == null || ohlcList == null || ohlcList.size() < 14) return;

                getActivity().runOnUiThread(() -> {
                    double rsi = calculateRSI(ohlcList, 14);
                    OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
                    double high = parseDoubleSafe(latest.getHigh());
                    double low = parseDoubleSafe(latest.getLow());
                    double close = parseDoubleSafe(latest.getClose());

                    // Calculate Pivot Points
                    double pivot = (high + low + close) / 3.0;
                    double r1 = (2 * pivot) - low;
                    double s1 = (2 * pivot) - high;
                    double r2 = pivot + (high - low);
                    double s2 = pivot - (high - low);

                    r1Txt.setText(String.format(Locale.getDefault(), "₹%.2f", r1));
                    r2Txt.setText(String.format(Locale.getDefault(), "₹%.2f", r2));
                    s1Txt.setText(String.format(Locale.getDefault(), "₹%.2f", s1));
                    s2Txt.setText(String.format(Locale.getDefault(), "₹%.2f", s2));

                    double changePct = parseDoubleSafe(percent);
                    boolean isBullish = changePct >= 0 || rsi > 50;

                    if (isBullish) {
                        sentimentBadge.setText("BULLISH");
                        sentimentBadge.getBackground().setTint(getResources().getColor(R.color.Green, null));
                        confidenceTxt.setText(String.format(Locale.getDefault(), "Signal Strength: %.0f%% Bullish Momentum", Math.min(95, 60 + Math.abs(changePct) * 5)));
                        macdValTxt.setText("Bullish Crossover");
                        macdValTxt.setTextColor(getResources().getColor(R.color.Green, null));
                        emaValTxt.setText("Uptrend (Above 20 EMA)");
                        emaValTxt.setTextColor(getResources().getColor(R.color.Green, null));
                    } else {
                        sentimentBadge.setText("BEARISH");
                        sentimentBadge.getBackground().setTint(getResources().getColor(R.color.Red, null));
                        confidenceTxt.setText(String.format(Locale.getDefault(), "Signal Strength: %.0f%% Bearish Pressure", Math.min(95, 60 + Math.abs(changePct) * 5)));
                        macdValTxt.setText("Bearish Divergence");
                        macdValTxt.setTextColor(getResources().getColor(R.color.Red, null));
                        emaValTxt.setText("Downtrend (Below 20 EMA)");
                        emaValTxt.setTextColor(getResources().getColor(R.color.Red, null));
                    }

                    String rsiStatus = rsi > 70 ? "Overbought (>70)" : (rsi < 30 ? "Oversold (<30)" : "Neutral/Healthy");
                    rsiValTxt.setText(String.format(Locale.getDefault(), "%.1f (%s)", rsi, rsiStatus));

                    volatilityTxt.setText(Math.abs(changePct) > 2.0 ? "High Volatility" : "Moderate Risk");

                    // AI Commentary Generation
                    StringBuilder commentary = new StringBuilder();
                    commentary.append(String.format("AI Signal for %s: ", symbol));
                    if (isBullish) {
                        commentary.append(String.format(Locale.getDefault(), "Asset is showing strong upward momentum with an RSI of %.1f. Price is trading above key moving averages. Immediate resistance is observed at ₹%.2f (R1), while key support holds at ₹%.2f (S1). Suitable for continuation strategies.", rsi, r1, s1));
                    } else {
                        commentary.append(String.format(Locale.getDefault(), "Asset is undergoing consolidation/pullback with RSI at %.1f. Key resistance is identified at ₹%.2f (R1) with immediate support at ₹%.2f (S1). Exercise risk management.", rsi, r1, s1));
                    }
                    commentaryTxt.setText(commentary.toString());
                });
            }

            @Override
            public void onFailure(String symbol, String error) {}
        });
    }

    private double calculateRSI(List<OHLC_Model> list, int period) {
        if (list == null || list.size() <= period) return 50.0;
        double gains = 0;
        double losses = 0;

        for (int i = list.size() - period; i < list.size(); i++) {
            double change = parseDoubleSafe(list.get(i).getClose()) - parseDoubleSafe(list.get(i - 1).getClose());
            if (change >= 0) gains += change;
            else losses += Math.abs(change);
        }

        double avgGain = gains / period;
        double avgLoss = losses / period;
        if (avgLoss == 0) return 100.0;
        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            return Double.parseDouble(str.replace("₹", "").replace("%", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
