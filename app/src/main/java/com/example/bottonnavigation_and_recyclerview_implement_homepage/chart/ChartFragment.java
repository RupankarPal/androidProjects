package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.FinancialUtils;
import com.tradingview.lightweightcharts.api.chart.models.color.IntColor;
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi;
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.PriceScaleMargins;
import com.tradingview.lightweightcharts.api.series.enums.LineWidth;
import com.tradingview.lightweightcharts.api.series.models.CandlestickData;
import com.tradingview.lightweightcharts.api.series.models.HistogramData;
import com.tradingview.lightweightcharts.api.series.models.PriceFormat;
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId;
import com.tradingview.lightweightcharts.api.series.models.Time;
import com.tradingview.lightweightcharts.view.ChartsView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.Unit;

public class ChartFragment extends Fragment implements IntervalDialogFragment.IntervalListener, IndicatorDialogFragment.IndicatorListener, DrawingDialogFragment.DrawingToolListener {

    private ChartsView chartsView;
    private DrawingOverlayView drawingOverlay;
    private LinearLayout indicatorsContainer;
    private TextView intervalBtn;

    private SeriesApi candlestickSeries;
    private SeriesApi volumeSeries;
    private final Map<String, SeriesApi> activeIndicatorSeries = new HashMap<>();

    private String symbol = "RELIANCE.NS";
    private String currentInterval = "1d";
    private String currentRange = "12mo";

    private final ArrayList<OHLC_Model> chartDataCache = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;

    public static ChartFragment newInstance(String symbol) {
        ChartFragment fragment = new ChartFragment();
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
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        chartsView = view.findViewById(R.id.chart_webview);
        drawingOverlay = view.findViewById(R.id.drawing_overlay);
        indicatorsContainer = view.findViewById(R.id.active_indicators_container);
        intervalBtn = view.findViewById(R.id.btn_interval_selector);

        intervalBtn.setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                IntervalDialogFragment.newInstance(this).show(getParentFragmentManager(), "IntervalDialog");
            }
        });

        view.findViewById(R.id.btn_indicators).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                IndicatorDialogFragment.newInstance(this).show(getParentFragmentManager(), "IndicatorDialog");
            }
        });

        view.findViewById(R.id.btn_drawings).setOnClickListener(v -> {
            if (getParentFragmentManager() != null) {
                DrawingDialogFragment.newInstance(this).show(getParentFragmentManager(), "DrawingDialog");
            }
        });

        setupChart();
        fetchInitialData();
        startRealtimeUpdates();

        return view;
    }

    private void setupChart() {
        if (chartsView == null) return;

        CandlestickSeriesOptions candleOptions = new CandlestickSeriesOptions();
        candleOptions.setScaleMargins(new PriceScaleMargins(0.1f, 0.3f));
        chartsView.getApi().addCandlestickSeries(
                candleOptions,
                series -> {
                    candlestickSeries = series;
                    return Unit.INSTANCE;
                }
        );

        HistogramSeriesOptions volumeOptions = new HistogramSeriesOptions();
        volumeOptions.setPriceScaleId(new PriceScaleId(""));
        volumeOptions.setScaleMargins(new PriceScaleMargins(0.7f, 0.0f));
        volumeOptions.setPriceFormat(PriceFormat.Companion.priceFormatBuiltIn(PriceFormat.Type.VOLUME, 1, 1f));
        volumeOptions.setVisible(false);
        chartsView.getApi().addHistogramSeries(volumeOptions, series -> {
            volumeSeries = series;
            return Unit.INSTANCE;
        });
    }

    @Override
    public void onIntervalSelected(String label, String yahooInterval, String yahooRange) {
        currentInterval = yahooInterval;
        currentRange = yahooRange;
        if (intervalBtn != null) intervalBtn.setText(label);
        fetchInitialData();
    }

    @Override
    public void onIndicatorSelected(String name) {
        if (activeIndicatorSeries.containsKey(name)) {
            if (getContext() != null) Toast.makeText(getContext(), name + " already added", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("Volume".equals(name)) {
            if (volumeSeries != null) {
                HistogramSeriesOptions options = new HistogramSeriesOptions();
                options.setVisible(true);
                volumeSeries.applyOptions(options);
            }
            addIndicatorLabel("Volume", Color.GRAY);
            updateChartNative(chartDataCache);
            return;
        }

        LineSeriesOptions options = new LineSeriesOptions();
        int color;
        if (name.contains("EMA")) color = Color.MAGENTA;
        else if (name.contains("SMA")) color = Color.CYAN;
        else if (name.equals("VWAP")) color = Color.YELLOW;
        else color = Color.BLUE;

        options.setColor(new IntColor(color));
        options.setLineWidth(LineWidth.TWO);

        final int finalColor = color;
        chartsView.getApi().addLineSeries(options, series -> {
            activeIndicatorSeries.put(name, series);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> addIndicatorLabel(name, finalColor));
            }
            updateChartNative(chartDataCache);
            return Unit.INSTANCE;
        });
    }

    private void addIndicatorLabel(String name, int color) {
        if (getContext() == null || indicatorsContainer == null) return;
        View labelView = LayoutInflater.from(getContext()).inflate(R.layout.item_active_indicator, indicatorsContainer, false);
        ((TextView) labelView.findViewById(R.id.indicator_label)).setText(name);
        ((TextView) labelView.findViewById(R.id.indicator_label)).setTextColor(color);

        labelView.findViewById(R.id.btn_remove).setOnClickListener(v -> {
            if ("Volume".equals(name)) {
                if (volumeSeries != null) {
                    HistogramSeriesOptions options = new HistogramSeriesOptions();
                    options.setVisible(false);
                    volumeSeries.applyOptions(options);
                }
            } else {
                SeriesApi series = activeIndicatorSeries.get(name);
                if (series != null) {
                    LineSeriesOptions options = new LineSeriesOptions();
                    options.setVisible(false);
                    series.applyOptions(options);
                }
                activeIndicatorSeries.remove(name);
            }
            indicatorsContainer.removeView(labelView);
        });

        indicatorsContainer.addView(labelView);
    }

    @Override
    public void onToolSelected(String toolName) {
        if (drawingOverlay == null) return;
        if ("Clear All".equals(toolName)) {
            drawingOverlay.clearAll();
            drawingOverlay.setMode(DrawingOverlayView.Mode.CURSOR);
        } else if ("Trend Line".equals(toolName)) {
            drawingOverlay.setMode(DrawingOverlayView.Mode.TREND_LINE);
        } else if ("Brush".equals(toolName)) {
            drawingOverlay.setMode(DrawingOverlayView.Mode.BRUSH);
        } else {
            drawingOverlay.setMode(DrawingOverlayView.Mode.CURSOR);
        }
    }

    private void fetchInitialData() {
        if (getContext() == null) return;
        YahooStockDataFeatch.fetchStockData(getContext(), symbol, currentInterval, currentRange, new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chartDataCache.clear();
                        chartDataCache.addAll(ohlcList);
                        updateChartNative(chartDataCache);
                        fetchAccurateLabels();
                    });
                }
            }

            @Override
            public void onFailure(String symbol, String error) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void fetchAccurateLabels() {
        if (getContext() == null) return;
        YahooStockDataFeatch.fetchStockData(getContext(), symbol, "1d", "2d", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (getActivity() instanceof ChartActivity) {
                            ((ChartActivity) getActivity()).updateHeaderUI(ohlcList, percent, change);
                        }
                        if (!ohlcList.isEmpty() && !chartDataCache.isEmpty()) {
                            OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
                            OHLC_Model cachedLast = chartDataCache.get(chartDataCache.size() - 1);
                            if (latest.getTimestamp() >= cachedLast.getTimestamp()) {
                                if (latest.getTimestamp() == cachedLast.getTimestamp()) chartDataCache.set(chartDataCache.size() - 1, latest);
                                else chartDataCache.add(latest);
                                updateChartNative(chartDataCache);
                            }
                        }
                    });
                }
            }
            @Override public void onFailure(String symbol, String error) {}
        });
    }

    private void startRealtimeUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                fetchAccurateLabels();
                handler.postDelayed(this, 10000);
            }
        };
        handler.postDelayed(updateRunnable, 10000);
    }

    private void updateChartNative(ArrayList<OHLC_Model> ohlcList) {
        if (ohlcList == null || ohlcList.isEmpty() || candlestickSeries == null) return;

        List<CandlestickData> candleDataList = new ArrayList<>();
        List<HistogramData> volumeDataList = new ArrayList<>();

        for (OHLC_Model model : ohlcList) {
            long time = model.getTimestamp();
            if (time == 0) continue;

            float open = Float.parseFloat(model.getOpen());
            float close = Float.parseFloat(model.getClose());

            candleDataList.add(new CandlestickData(new Time.Utc(time), open, Float.parseFloat(model.getHigh()), Float.parseFloat(model.getLow()), close, null, null, null));

            IntColor volColor = (close >= open) ? new IntColor(Color.argb(128, 0, 255, 0)) : new IntColor(Color.argb(128, 255, 0, 0));
            volumeDataList.add(new HistogramData(new Time.Utc(time), Float.parseFloat(model.getVolume()), volColor));
        }
        candlestickSeries.setData(candleDataList);
        if (volumeSeries != null) volumeSeries.setData(volumeDataList);

        for (Map.Entry<String, SeriesApi> entry : activeIndicatorSeries.entrySet()) {
            String name = entry.getKey();
            SeriesApi series = entry.getValue();
            if (name.contains("EMA")) {
                int p = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                series.setData(FinancialUtils.calculateEMA(ohlcList, p));
            } else if (name.contains("SMA")) {
                int p = Integer.parseInt(name.replaceAll("[^0-9]", ""));
                series.setData(FinancialUtils.calculateSMA(ohlcList, p));
            } else if (name.equals("VWAP")) {
                series.setData(FinancialUtils.calculateVWAP(ohlcList));
            } else if (name.equals("Bollinger Bands")) {
                series.setData(FinancialUtils.calculateBollingerBands(ohlcList, 20, 2.0).get(0));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(updateRunnable);
    }
}
