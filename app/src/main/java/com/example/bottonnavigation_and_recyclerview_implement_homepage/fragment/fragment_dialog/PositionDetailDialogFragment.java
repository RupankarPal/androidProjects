package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment.fragment_dialog;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi;
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions;
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId;
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions;
import com.tradingview.lightweightcharts.api.options.models.PriceScaleMargins;
import com.tradingview.lightweightcharts.api.series.models.PriceFormat;
import com.tradingview.lightweightcharts.api.series.models.CandlestickData;
import com.tradingview.lightweightcharts.api.series.models.HistogramData;
import com.tradingview.lightweightcharts.api.series.models.Time;
import com.tradingview.lightweightcharts.view.ChartsView;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.FinancialUtils;
import com.tradingview.lightweightcharts.api.chart.models.color.IntColor;
import com.tradingview.lightweightcharts.api.options.models.LineSeriesOptions;
import com.tradingview.lightweightcharts.api.series.enums.LineWidth;

public class PositionDetailDialogFragment extends BottomSheetDialogFragment {

    private portfolio_stocks_model stockModel;
    private ChartsView chartsView;
    private SeriesApi candlestickSeries;
    private SeriesApi volumeSeries;
    private SeriesApi ema20Series, ema50Series, ema100Series, ema200Series;
    private TextView symbolTxt, priceTxt, changeTxt, qtyTxt, avgTxt, plTxt;
    private final ArrayList<OHLC_Model> chartDataCache = new ArrayList<>();

    public static PositionDetailDialogFragment newInstance(portfolio_stocks_model model) {
        PositionDetailDialogFragment fragment = new PositionDetailDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("model", model);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockModel = (portfolio_stocks_model) getArguments().getSerializable("model");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_position_detail, container, false);
        initViews(view);
        setupChart();
        fetchChartData();
        return view;
    }

    private void initViews(View view) {
        symbolTxt = view.findViewById(R.id.detail_symbol_txt);
        priceTxt = view.findViewById(R.id.detail_price_txt);
        changeTxt = view.findViewById(R.id.detail_change_txt);
        qtyTxt = view.findViewById(R.id.detail_qty_txt);
        avgTxt = view.findViewById(R.id.detail_avg_txt);
        plTxt = view.findViewById(R.id.detail_pl_txt);
        chartsView = view.findViewById(R.id.detail_chart_webview);

        if (stockModel != null) {
            symbolTxt.setText(stockModel.getStocks_name());
            priceTxt.setText(stockModel.getStocks_price());
            changeTxt.setText(stockModel.getDaily_change());
            qtyTxt.setText(stockModel.getStocks_quantity());
            avgTxt.setText(stockModel.getPercheasing_price());
            plTxt.setText(String.format("%s (%s)", stockModel.getPl_amount(), stockModel.getPercentage_change()));

            double pl = parseDoubleSafe(stockModel.getPl_amount());
            plTxt.setTextColor(pl >= 0 ? Color.parseColor("#0F9715") : Color.RED);

            view.findViewById(R.id.detail_add_more_btn).setOnClickListener(v -> {
                BuySellDialogFragment dialog = BuySellDialogFragment.newInstance(stockModel.getStocks_name(), stockModel.getStocks_price(), stockModel.getDaily_change(), "BUY", 0);
                dialog.show(getParentFragmentManager(), "BuySellDialog");
                dismiss();
            });

            view.findViewById(R.id.detail_sell_btn).setOnClickListener(v -> {
                BuySellDialogFragment dialog = BuySellDialogFragment.newInstance(stockModel.getStocks_name(), stockModel.getStocks_price(), stockModel.getDaily_change(), "SELL", parseDoubleSafe(stockModel.getStocks_quantity()));
                dialog.show(getParentFragmentManager(), "BuySellDialog");
                dismiss();
            });
        }
    }

    private void setupChart() {
        if (chartsView == null) return;
        // Main Price Series
        CandlestickSeriesOptions candleOptions = new CandlestickSeriesOptions();
        candleOptions.setScaleMargins(new PriceScaleMargins(0.1f, 0.3f));
        chartsView.getApi().addCandlestickSeries(
            candleOptions,
            series -> {
                candlestickSeries = series;
                return Unit.INSTANCE;
            }
        );

        // Volume Series for popup
        HistogramSeriesOptions volumeOptions = new HistogramSeriesOptions();
        volumeOptions.setPriceScaleId(new PriceScaleId("")); // Empty for overlay
        volumeOptions.setScaleMargins(new PriceScaleMargins(0.7f, 0.0f));
        volumeOptions.setPriceFormat(PriceFormat.Companion.priceFormatBuiltIn(PriceFormat.Type.VOLUME, 1, 1f));
        chartsView.getApi().addHistogramSeries(volumeOptions, series -> {
            volumeSeries = series;
            return Unit.INSTANCE;
        });

        // Add EMAs for popup as well
        chartsView.getApi().addLineSeries(createEMAOption(Color.BLUE), series -> {
            ema20Series = series;
            return Unit.INSTANCE;
        });
        chartsView.getApi().addLineSeries(createEMAOption(Color.rgb(255, 165, 0)), series -> {
            ema50Series = series;
            return Unit.INSTANCE;
        });
        chartsView.getApi().addLineSeries(createEMAOption(Color.GREEN), series -> {
            ema100Series = series;
            return Unit.INSTANCE;
        });
        chartsView.getApi().addLineSeries(createEMAOption(Color.RED), series -> {
            ema200Series = series;
            return Unit.INSTANCE;
        });
    }

    private LineSeriesOptions createEMAOption(int color) {
        LineSeriesOptions opt = new LineSeriesOptions();
        opt.setColor(new IntColor(color));
        opt.setLineWidth(LineWidth.ONE); // Thinner for popup
        return opt;
    }

    private void fetchChartData() {
        if (getContext() == null || stockModel == null) return;
        // Step 1: Fetch 12mo for background history and EMAs
        YahooStockDataFeatch.fetchStockData(getContext(), stockModel.getStocks_name(), "1d", "12mo", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    chartDataCache.clear();
                    chartDataCache.addAll(ohlcList);
                    updateChartNative(chartDataCache);
                    
                    // Step 2: Immediately chain a 2d fetch for accurate UI labels
                    fetchAccurateLabels();
                });
            }

            @Override
            public void onFailure(String symbol, String error) {}
        });
    }

    private void fetchAccurateLabels() {
        if (getContext() == null || stockModel == null) return;
        YahooStockDataFeatch.fetchStockData(getContext(), stockModel.getStocks_name(), "1d", "2d", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() == null || !isAdded()) return;
                getActivity().runOnUiThread(() -> {
                    if (!isAdded()) return;
                    // Update header with accurate real-time stats from 2d call
                    if (priceTxt != null && !ohlcList.isEmpty()) {
                        priceTxt.setText(ohlcList.get(ohlcList.size()-1).getClose());
                        changeTxt.setText(String.format("%s (%s%%)", change, percent));
                    }
                    
                    // Sync the latest price into history cache
                    if (!ohlcList.isEmpty() && !chartDataCache.isEmpty()) {
                        OHLC_Model latest = ohlcList.get(ohlcList.size() - 1);
                        OHLC_Model cachedLast = chartDataCache.get(chartDataCache.size() - 1);
                        
                        if (latest.getTimestamp() >= cachedLast.getTimestamp()) {
                             if (latest.getTimestamp() == cachedLast.getTimestamp()) {
                                 chartDataCache.set(chartDataCache.size() - 1, latest);
                             } else {
                                 chartDataCache.add(latest);
                             }
                             updateChartNative(chartDataCache);
                        }
                    }
                });
            }
            @Override public void onFailure(String symbol, String error) {}
        });
    }

    private void updateChartNative(ArrayList<OHLC_Model> ohlcList) {
        if (ohlcList == null || ohlcList.isEmpty() || candlestickSeries == null || !isAdded()) return;
        
        List<CandlestickData> candleDataList = new ArrayList<>();
        List<HistogramData> volumeDataList = new ArrayList<>();

        for (OHLC_Model model : ohlcList) {
            long time = model.getTimestamp();
            if (time == 0) continue;
            
            float open = Float.parseFloat(model.getOpen());
            float high = Float.parseFloat(model.getHigh());
            float low = Float.parseFloat(model.getLow());
            float close = Float.parseFloat(model.getClose());
            float volume = Float.parseFloat(model.getVolume());

            candleDataList.add(new CandlestickData(
                new Time.Utc(time),
                open, high, low, close,
                null, null, null
            ));

            // Volume color based on candle direction
            IntColor volColor = (close >= open) ? new IntColor(Color.argb(128, 0, 255, 0)) : new IntColor(Color.argb(128, 255, 0, 0));
            volumeDataList.add(new HistogramData(
                new Time.Utc(time),
                volume,
                volColor
            ));
        }
        candlestickSeries.setData(candleDataList);
        if (volumeSeries != null) volumeSeries.setData(volumeDataList);

        // Update EMAs
        if (ema20Series != null) ema20Series.setData(FinancialUtils.calculateEMA(ohlcList, 20));
        if (ema50Series != null) ema50Series.setData(FinancialUtils.calculateEMA(ohlcList, 50));
        if (ema100Series != null) ema100Series.setData(FinancialUtils.calculateEMA(ohlcList, 100));
        if (ema200Series != null) ema200Series.setData(FinancialUtils.calculateEMA(ohlcList, 200));
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.isEmpty() || str.equals("--")) return 0.0;
        try {
            String clean = str.replace("₹", "").replace("%", "").replace(",", "").replace("(", "").replace(")", "").trim();
            return Double.parseDouble(clean);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
