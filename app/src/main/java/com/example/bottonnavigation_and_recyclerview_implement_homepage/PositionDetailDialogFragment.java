package com.example.bottonnavigation_and_recyclerview_implement_homepage;

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
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi;
import com.tradingview.lightweightcharts.api.options.models.CandlestickSeriesOptions;
import com.tradingview.lightweightcharts.api.series.models.CandlestickData;
import com.tradingview.lightweightcharts.api.series.models.Time;
import com.tradingview.lightweightcharts.view.ChartsView;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class PositionDetailDialogFragment extends BottomSheetDialogFragment {

    private portfolio_stocks_model stockModel;
    private ChartsView chartsView;
    private SeriesApi candlestickSeries;
    private TextView symbolTxt, priceTxt, changeTxt, qtyTxt, avgTxt, plTxt;

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

        symbolTxt.setText(stockModel.getStocks_name());
        priceTxt.setText(stockModel.getStocks_price());
        changeTxt.setText(stockModel.getDaily_change());
        qtyTxt.setText(stockModel.getStocks_quantity());
        avgTxt.setText(stockModel.getPercheasing_price());
        plTxt.setText(stockModel.getPl_amount() + " (" + stockModel.getPercentage_change() + ")");

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

    private void setupChart() {
        chartsView.getApi().addCandlestickSeries(
            new CandlestickSeriesOptions(),
            series -> {
                candlestickSeries = series;
                return Unit.INSTANCE;
            }
        );
    }

    private void fetchChartData() {
        YahooStockDataFeatch.fetchStockData(getContext(), stockModel.getStocks_name(), "1h", "5d", new YahooStockDataFeatch.StockDataCallback() {
            @Override
            public void onSuccess(String symbol, ArrayList<OHLC_Model> ohlcList, String percent, String change) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> updateChartNative(ohlcList));
            }

            @Override
            public void onFailure(String symbol, String error) {}
        });
    }

    private void updateChartNative(ArrayList<OHLC_Model> ohlcList) {
        if (ohlcList == null || ohlcList.isEmpty() || candlestickSeries == null) return;
        
        List<CandlestickData> dataList = new ArrayList<>();
        for (OHLC_Model model : ohlcList) {
            long time;
            try {
                time = Long.parseLong(model.getVolume());
            } catch (Exception e) {
                continue;
            }
            
            dataList.add(new CandlestickData(
                new Time.Utc(time),
                Float.parseFloat(model.getOpen()),
                Float.parseFloat(model.getHigh()),
                Float.parseFloat(model.getLow()),
                Float.parseFloat(model.getClose()),
                null, null, null
            ));
        }
        candlestickSeries.setData(dataList);
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
