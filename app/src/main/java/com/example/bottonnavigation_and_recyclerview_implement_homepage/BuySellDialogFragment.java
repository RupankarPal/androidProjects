package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.MarketTimeManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.OrderWorker;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BuySellDialogFragment extends BottomSheetDialogFragment {

    private TextView stockName, stockPrice, stockChangeInPercent, todayChange_txt, total_cost_txt, balance_txt, price_label;
    private ImageView back_btn, advanced_arrow;
    private Spinner order_type_spinner;
    private Button review_order_btn, delivery_btn, intraday_btn, buy_side_btn, sell_side_btn;
    private EditText Qedt, Pedt, Tedt, sledt, trailing_sl_edt;
    private View advanced_layout;
    
    private int orderId = -1; // -1 means NEW order
    private double oldMargin = 0; // Margin blocked by original order
    private int orderType = 1; // 1=market, 2=limit
    private String tradeSide = "BUY"; // BUY or SELL
    private String productType = "DELIVERY"; // DELIVERY or INTRADAY
    private double balance;
    private String symbol, currentPriceStr, percentile;
    private double availableQty = 0;
    
    private final Handler updateHandler = new Handler(Looper.getMainLooper());
    private Runnable updateRunnable;
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    public static BuySellDialogFragment newInstance(String symbol, String price, String percentile, String type, double availableQty) {
        BuySellDialogFragment fragment = new BuySellDialogFragment();
        Bundle args = new Bundle();
        args.putString("symbol", symbol);
        args.putString("price", price);
        args.putString("percentile", percentile);
        args.putString("type", type);
        args.putDouble("availableQty", availableQty);
        fragment.setArguments(args);
        return fragment;
    }

    public static BuySellDialogFragment newInstance(String symbol, String price, String percentile) {
        return newInstance(symbol, price, percentile, "BUY", 0);
    }

    public static BuySellDialogFragment newInstanceForEdit(Order_model order) {
        BuySellDialogFragment fragment = new BuySellDialogFragment();
        Bundle args = new Bundle();
        args.putInt("orderId", order.getId());
        args.putString("symbol", order.getStock_name());
        args.putString("price", String.valueOf(order.getOrder_prise()));
        args.putDouble("orderPriceForMargin", order.getOrder_prise());
        args.putString("percentile", "0");
        args.putString("type", order.getOrder_type());
        args.putString("productType", order.getProduct_type());
        args.putDouble("quantity", order.getStock_quantity());
        args.putDouble("target", order.getTargate_price());
        args.putDouble("sl", order.getSl_price());
        args.putDouble("trailingSl", order.getTrailingSl());
        args.putInt("orderTypeMode", order.getOrder_prise() == 0 ? 1 : 2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getInt("orderId", -1);
            symbol = getArguments().getString("symbol");
            currentPriceStr = getArguments().getString("price");
            percentile = getArguments().getString("percentile");
            availableQty = getArguments().getDouble("availableQty", 0);
            
            // Initial trade side and product type
            tradeSide = getArguments().getString("type", "BUY");
            productType = getArguments().getString("productType", "DELIVERY");

            if (orderId != -1) {
                double q = getArguments().getDouble("quantity", 0);
                double p = getArguments().getDouble("orderPriceForMargin", 0);
                double total = q * p;
                oldMargin = productType.equalsIgnoreCase("INTRADAY") ? total / 5.0 : total;
                if (!tradeSide.equalsIgnoreCase("BUY")) oldMargin = 0;
            }
        }
    }

    @NonNull
    @Override
    public android.app.Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        com.google.android.material.bottomsheet.BottomSheetDialog dialog = (com.google.android.material.bottomsheet.BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(dialogInterface -> {
            com.google.android.material.bottomsheet.BottomSheetDialog d = (com.google.android.material.bottomsheet.BottomSheetDialog) dialogInterface;
            View bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet);
                behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null) {
            View parent = (View) view.getParent();
            ViewGroup.LayoutParams layoutParams = parent.getLayoutParams();
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            parent.setLayoutParams(layoutParams);
            
            com.google.android.material.bottomsheet.BottomSheetBehavior<View> behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(parent);
            behavior.setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            behavior.setSkipCollapsed(true);
            
            // Set peek height to screen height for safety
            android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
            if (getActivity() != null) {
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                behavior.setPeekHeight(displayMetrics.heightPixels);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_buysell_order_window, container, false);
        initViews(view);
        setupListeners();
        loadData();
        return view;
    }

    private void initViews(View view) {
        stockName = view.findViewById(R.id.stocks_Name_txt);
        stockPrice = view.findViewById(R.id.limit_price_txt);
        stockChangeInPercent = view.findViewById(R.id.percentile_txt);
        todayChange_txt = view.findViewById(R.id.todayChange_txt);
        price_label = view.findViewById(R.id.price_label);
        order_type_spinner = view.findViewById(R.id.order_type_spinner);
        
        balance_txt = view.findViewById(R.id.balence_txt);
        back_btn = view.findViewById(R.id.back_btn_img);
        
        review_order_btn = view.findViewById(R.id.review_order_btn);
        total_cost_txt = view.findViewById(R.id.total_cost_txt);
        
        delivery_btn = view.findViewById(R.id.delivery_btn);
        intraday_btn = view.findViewById(R.id.intraday_btn);
        buy_side_btn = view.findViewById(R.id.buy_side_btn);
        sell_side_btn = view.findViewById(R.id.sell_side_btn);

        Qedt = view.findViewById(R.id.Qedt);
        Pedt = view.findViewById(R.id.pedt);
        Tedt = view.findViewById(R.id.targate_edt);
        sledt = view.findViewById(R.id.sl_edt);
        trailing_sl_edt = view.findViewById(R.id.trailing_sl_edt);
        
        advanced_layout = view.findViewById(R.id.advanced_layout);
        advanced_arrow = view.findViewById(R.id.advanced_arrow);
        View advanced_header = view.findViewById(R.id.advanced_header);
        if (advanced_header != null) {
            advanced_header.setOnClickListener(v -> {
                if (advanced_layout.getVisibility() == View.VISIBLE) {
                    advanced_layout.setVisibility(View.GONE);
                    advanced_arrow.setRotation(0);
                } else {
                    advanced_layout.setVisibility(View.VISIBLE);
                    advanced_arrow.setRotation(180);
                }
            });
        }
    }

    private void loadData() {
        if (stockName != null) stockName.setText(symbol);
        updateUIWithPrice(currentPriceStr, percentile, "0.00");

        MarketTimeManager.MarketType marketType = MarketTimeManager.getMarketType(symbol);
        if (Qedt != null) {
            if (marketType == MarketTimeManager.MarketType.CRYPTO || marketType == MarketTimeManager.MarketType.US) {
                Qedt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
            } else {
                Qedt.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
            }
        }

        dbExecutor.execute(() -> {
            Context ctx = getContext();
            if (ctx == null) return;
            FundDatabase fdb = new FundDatabase(ctx);
            balance = fdb.getBalance();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (balance_txt != null) balance_txt.setText(String.format(Locale.getDefault(), "%.2f", balance));
            });
        });
        
        setMarketOrderMode();
        
            // Setup initial UI based on tradeSide and productType
            if (tradeSide.equalsIgnoreCase("SELL")) {
                sell_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.Red, null)));
                sell_side_btn.setTextColor(getResources().getColor(R.color.white, null));
                buy_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
                buy_side_btn.setTextColor(getResources().getColor(R.color.black, null));
            } else {
                buy_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.Green, null)));
                buy_side_btn.setTextColor(getResources().getColor(R.color.white, null));
                sell_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
                sell_side_btn.setTextColor(getResources().getColor(R.color.black, null));
            }

            if (productType.equalsIgnoreCase("INTRADAY")) {
                intraday_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.violet, null)));
                intraday_btn.setTextColor(getResources().getColor(R.color.white, null));
                delivery_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
                delivery_btn.setTextColor(getResources().getColor(R.color.black, null));
            } else {
                delivery_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.violet, null)));
                delivery_btn.setTextColor(getResources().getColor(R.color.white, null));
                intraday_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
                intraday_btn.setTextColor(getResources().getColor(R.color.black, null));
            }

        if (orderId != -1 && getArguments() != null) {
            Qedt.setText(String.valueOf(getArguments().getDouble("quantity")));
            Tedt.setText(String.valueOf(getArguments().getDouble("target")));
            sledt.setText(String.valueOf(getArguments().getDouble("sl")));
            trailing_sl_edt.setText(String.valueOf(getArguments().getDouble("trailingSl")));
            
            if (getArguments().getInt("orderTypeMode") == 2) {
                setLimitOrderMode();
                Pedt.setText(String.valueOf(getArguments().getString("price")));
                order_type_spinner.setSelection(1);
            }

            if (tradeSide.equalsIgnoreCase("SELL")) {
                sell_side_btn.performClick();
            }
            if (productType.equalsIgnoreCase("INTRADAY")) {
                intraday_btn.performClick();
            }
            review_order_btn.setText("Modify Order");
        } else if (availableQty > 0 && tradeSide.equalsIgnoreCase("SELL")) {
            // Pre-fill quantity for square-off from Portfolio
            Qedt.setText(String.valueOf(availableQty));
        }

        updateReviewButton();
        startPriceUpdates();

        if (availableQty > 0 && !tradeSide.equalsIgnoreCase("SELL")) {
             new Handler(Looper.getMainLooper()).postDelayed(() -> {
                 if (getContext() != null) Toast.makeText(getContext(), "You already hold: " + availableQty, Toast.LENGTH_SHORT).show();
             }, 1000);
        }
    }

    private void startPriceUpdates() {
        updateRunnable = new Runnable() {
            @Override
            public void run() {
                if (getContext() == null) return;
                com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch.fetchStockData(getContext(), symbol, new com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.YahooStockDataFeatch.StockDataCallback() {
                    @Override
                    public void onSuccess(String symbol, java.util.ArrayList<com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model> ohlcList, String percent, String change) {
                        if (ohlcList != null && !ohlcList.isEmpty()) {
                            com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.OHLC_Model data = ohlcList.get(ohlcList.size() - 1);
                            String price = data.getClose();
                            new Handler(Looper.getMainLooper()).post(() -> {
                                updateUIWithPrice(price, percent, change); 
                            });
                        }
                    }
                    @Override public void onFailure(String symbol, String error) {}
                });
                updateHandler.postDelayed(this, 1500); // Fast price updates
            }
        };
        updateHandler.post(updateRunnable);
    }

    private void updateUIWithPrice(String price, String percent, String change) {
        if (stockPrice != null) stockPrice.setText(price);
        if (stockChangeInPercent != null) stockChangeInPercent.setText(String.format("(%s%%)", percent));
        if (todayChange_txt != null) todayChange_txt.setText(change);
        
        currentPriceStr = price;
        percentile = percent;
        
        int color = colorCoding(percent);
        if (stockPrice != null) stockPrice.setTextColor(color);
        if (stockChangeInPercent != null) stockChangeInPercent.setTextColor(color);
        if (todayChange_txt != null) todayChange_txt.setTextColor(color);
    }

    private int colorCoding(String percent) {
        double p = parseDoubleSafe(percent);
        if (p > 0) return getResources().getColor(R.color.Green, null);
        if (p < 0) return getResources().getColor(R.color.Red, null);
        return getResources().getColor(R.color.black, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (updateHandler != null && updateRunnable != null) {
            updateHandler.removeCallbacks(updateRunnable);
        }
    }

    private void setupListeners() {
        if (back_btn != null) back_btn.setOnClickListener(v -> dismiss());
        
        if (Pedt != null) {
            Pedt.setOnClickListener(v -> {
                if (orderType == 1) setLimitOrderMode();
            });
        }

        if (Qedt != null) {
            Qedt.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { updateReviewButton(); }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (order_type_spinner != null) {
            String[] options = {"Market", "Limit"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, options);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            order_type_spinner.setAdapter(adapter);
            order_type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) setMarketOrderMode();
                    else setLimitOrderMode();
                }
                @Override public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (delivery_btn != null) delivery_btn.setOnClickListener(v -> {
            productType = "DELIVERY";
            delivery_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.violet, null)));
            delivery_btn.setTextColor(getResources().getColor(R.color.white, null));
            intraday_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
            intraday_btn.setTextColor(getResources().getColor(R.color.black, null));
            updateReviewButton();
        });

        if (intraday_btn != null) intraday_btn.setOnClickListener(v -> {
            productType = "INTRADAY";
            intraday_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.violet, null)));
            intraday_btn.setTextColor(getResources().getColor(R.color.white, null));
            delivery_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
            delivery_btn.setTextColor(getResources().getColor(R.color.black, null));
            updateReviewButton();
        });

        if (buy_side_btn != null) buy_side_btn.setOnClickListener(v -> {
            tradeSide = "BUY";
            buy_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.Green, null)));
            buy_side_btn.setTextColor(getResources().getColor(R.color.white, null));
            sell_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
            sell_side_btn.setTextColor(getResources().getColor(R.color.black, null));
            updateReviewButton();
        });

        if (sell_side_btn != null) sell_side_btn.setOnClickListener(v -> {
            tradeSide = "SELL";
            sell_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.Red, null)));
            sell_side_btn.setTextColor(getResources().getColor(R.color.white, null));
            buy_side_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFF5F5F5));
            buy_side_btn.setTextColor(getResources().getColor(R.color.black, null));
            updateReviewButton();
        });

        if (review_order_btn != null) review_order_btn.setOnClickListener(v -> {
            review_order_btn.setEnabled(false); // Prevent double click
            handleTrade(tradeSide);
        });
    }

    private void updateReviewButton() {
        double q = parseDoubleSafe(Qedt != null ? Qedt.getText().toString() : "0");
        double p = orderType == 1 ? parseDoubleSafe(currentPriceStr) : parseDoubleSafe(Pedt != null ? Pedt.getText().toString() : "0");
        double total = q * p;
        
        // 5x Leverage for Intraday
        double requiredMargin = productType.equals("INTRADAY") ? total / 5.0 : total;
        
        TextView label = getView() != null ? getView().findViewById(R.id.textView9) : null;
        if (label != null) {
            label.setText(productType.equals("INTRADAY") ? "Required margin (5x): " : "Estimated charges: ");
        }

        if (total_cost_txt != null) total_cost_txt.setText(String.format(Locale.getDefault(), "%.2f", requiredMargin));

        if (review_order_btn != null) {
            String text = (tradeSide.equals("BUY") ? "Review buy order" : "Review sell order");
            review_order_btn.setText(text);
            review_order_btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(tradeSide.equals("BUY") ? R.color.Green : R.color.Red, null)));
        }
        
        if (balance_txt != null) {
            balance_txt.setTextColor(tradeSide.equals("BUY") && requiredMargin > balance ? getResources().getColor(R.color.Red, null) : getResources().getColor(R.color.black, null));
        }
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replace("₹", "").replace("%", "").replace(",", "").replace("(", "").replace(")", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private void handleTrade(String type) {
        MarketTimeManager.MarketType marketType = MarketTimeManager.getMarketType(symbol);
        if (!MarketTimeManager.isMarketOpen(marketType)) {
            Toast.makeText(getContext(), "Market is currently closed for " + symbol, Toast.LENGTH_LONG).show();
            return;
        }

        double q = parseDoubleSafe(Qedt != null ? Qedt.getText().toString() : "0");
        if (marketType == MarketTimeManager.MarketType.INDIAN && q != Math.floor(q)) {
            Toast.makeText(getContext(), "Indian shares only allow whole numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (q <= 0) {
            Toast.makeText(getContext(), "Enter valid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        if (type.equals("SELL")) {
            if (productType.equals("DELIVERY") && q > availableQty) {
                Toast.makeText(getContext(), "Cannot sell in Delivery if not owned. Available: " + availableQty, Toast.LENGTH_LONG).show();
                return;
            }
        }

        double p = orderType == 1 ? 0 : parseDoubleSafe(Pedt != null ? Pedt.getText().toString() : "0");
        double total = q * (p == 0 ? parseDoubleSafe(currentPriceStr) : p);
        double requiredMargin = productType.equals("INTRADAY") ? total / 5.0 : total;

        // Balance check considering the money already blocked by the old order
        if (type.equals("BUY") && requiredMargin > (balance + oldMargin)) {
            Toast.makeText(getContext(), "Insufficient balance. Required: " + String.format(Locale.getDefault(), "%.2f", requiredMargin), Toast.LENGTH_SHORT).show();
            return;
        }

        dbExecutor.execute(() -> {
            Context context = getContext();
            if (context == null) return;
            
            OrdersDatabaseHelper odb = null;
            FundDatabase fdb = null;
            try {
                odb = new OrdersDatabaseHelper(context);
                fdb = new FundDatabase(context);
                
                Calendar cal = Calendar.getInstance();
                String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
                String timeStr = String.format(Locale.getDefault(), "%d:%02d%s", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm");

                double target = parseDoubleSafe(Tedt != null ? Tedt.getText().toString() : "0");
                double sl = parseDoubleSafe(sledt != null ? sledt.getText().toString() : "0");
                double trailingSl = parseDoubleSafe(trailing_sl_edt != null ? trailing_sl_edt.getText().toString() : "0");
                
                // CAPTURE ENTRY PRICE FOR P&L REPORTING
                double recordedBasisPrice = p; 
                double currentPortfolioQty = fdb.getAvailableQuantity(symbol);
                
                if (type.equals("SELL") && currentPortfolioQty > 0.0001) {
                    // Closing LONG
                    recordedBasisPrice = fdb.getAveragePrice(symbol);
                } else if (type.equals("BUY") && currentPortfolioQty < -0.0001) {
                    // Closing SHORT (Covering)
                    recordedBasisPrice = fdb.getAveragePrice(symbol);
                }
                
                // If it's a MARKET order (p=0), use the market price for basis if not closing position
                if (recordedBasisPrice == 0) {
                    recordedBasisPrice = parseDoubleSafe(currentPriceStr);
                }

                Order_model updatedOrder = new Order_model(p, recordedBasisPrice, q, 0, target, sl, symbol, type, timeStr, trailingSl, productType);
                
                if (orderId == -1) {
                    odb.insertOrder(updatedOrder);
                    if (type.equals("BUY")) fdb.useFund(requiredMargin, date);
                } else {
                    updatedOrder.setId(orderId);
                    odb.modifyOrder(orderId, updatedOrder);
                    
                    // Refund old margin and deduct new margin
                    if (type.equals("BUY")) {
                        fdb.addFund("USD", oldMargin, date); // Refund
                        fdb.useFund(requiredMargin, date); // Re-deduct
                    }
                }

                // Trigger Background Processing immediately
                OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(OrderWorker.class)
                        .setInitialDelay(100, TimeUnit.MILLISECONDS)
                        .build();
                WorkManager.getInstance(context).enqueue(workRequest);

                // EXTRA: For Market orders, trigger matching engine logic immediately in this background thread
                if (orderType == 1) { // 1 = MARKET
                    com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.order_process process = 
                        new com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.order_process(context);
                    process.matchPendingOrders();
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null) {
                        Toast.makeText(getActivity(), "Order Placed", Toast.LENGTH_SHORT).show();
                        dismissAllowingStateLoss();
                    }
                });
                
            } catch (Exception e) {
                Log.e("TRADING_ERROR", "Trade failed: " + e.getMessage());
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded() && getActivity() != null) {
                        review_order_btn.setEnabled(true);
                        Toast.makeText(getActivity(), "Trade execution failed", Toast.LENGTH_SHORT).show();
                        dismissAllowingStateLoss();
                    }
                });
            } finally {
                if (odb != null) odb.close();
                if (fdb != null) fdb.close();
            }
        });
    }

    private void setMarketOrderMode() {
        orderType = 1;
        if (Pedt != null) {
            Pedt.setFocusable(false);
            Pedt.setClickable(true);
            Pedt.setHint("Market");
            Pedt.setText("");
        }
        if (price_label != null) price_label.setText("Market");
    }

    private void setLimitOrderMode() {
        orderType = 2;
        if (Pedt != null) {
            Pedt.setFocusableInTouchMode(true);
            Pedt.setEnabled(true);
            Pedt.setHint("00.00");
        }
        if (price_label != null) price_label.setText("Limit");
    }
}
