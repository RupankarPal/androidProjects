package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment.fragment_ButtomNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.ProfitLossActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.MainActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.utils.CurrencyConverter;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.validation.loginPage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.Calendar;
import java.util.Locale;

public class My_acount extends Fragment {

    private Activity context;
    private RewardedAd rewardedAd;

    private TextView inrFundTxt, usdFundTxt;
    private double liveUsdToInrRate = CurrencyConverter.DEFAULT_USD_TO_INR_RATE;
    private boolean isSwappedDirection = false; // false = INR -> USD, true = USD -> INR

    private void loadRewardAd(Activity context) {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = (MainActivity) getActivity();
        loadRewardAd(context);

        View view = inflater.inflate(R.layout.fragment_my_acount, container, false);

        MobileAds.initialize(context, initializationStatus -> {});

        inrFundTxt = view.findViewById(R.id.inr_fund_txt);
        usdFundTxt = view.findViewById(R.id.fund_txt);

        refreshWalletBalances(view);

        // Date String for Transactions
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        String dateStr = date + "-" + month + "-" + year;

        // Add INR Fund Code (+₹25,000 to INR Wallet)
        Button addFundBtn = view.findViewById(R.id.add_fund_btn);
        if (addFundBtn != null) {
            addFundBtn.setOnClickListener(v -> {
                FundDatabase fdb = new FundDatabase(context);
                try {
                    if (rewardedAd != null) {
                        rewardedAd.show(context, rewardItem -> {
                            fdb.addFund("INR", 25000, dateStr);
                            refreshWalletBalances(getView());
                            Toast.makeText(context, "+₹25,000 INR Added to Wallet!", Toast.LENGTH_SHORT).show();
                            loadRewardAd(context);
                        });
                    } else {
                        Toast.makeText(context, "Ad Loading... Adding +₹25,000 INR in 5s...", Toast.LENGTH_SHORT).show();
                        new android.os.Handler().postDelayed(() -> {
                            fdb.addFund("INR", 25000, dateStr);
                            refreshWalletBalances(getView());
                            Toast.makeText(context, "+₹25,000 INR Added to Wallet (fallback)!", Toast.LENGTH_SHORT).show();
                        }, 5000);
                        loadRewardAd(context);
                    }
                } catch (Exception e) {
                    fdb.addFund("INR", 25000, dateStr);
                    refreshWalletBalances(getView());
                    Toast.makeText(context, "+₹25,000 INR Added to Wallet!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Currency Converter Dialog Trigger
        Button convertFundBtn = view.findViewById(R.id.convert_fund_btn);
        if (convertFundBtn != null) {
            convertFundBtn.setOnClickListener(v -> openCurrencyConverterDialog(dateStr));
        }

        // History and Reports Button
        Button historyReportBtn = view.findViewById(R.id.history_report_btn);
        if (historyReportBtn != null) {
            historyReportBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProfitLossActivity.class);
                startActivity(intent);
            });
        }

        // Logout Button
        Button logOutBtn = view.findViewById(R.id.LogOut_btn);
        if (logOutBtn != null) {
            logOutBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, loginPage.class);
                startActivity(intent);
            });
        }

        // Reset Data Button
        Button resetBtn = view.findViewById(R.id.reset_data_btn);
        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                builder.setTitle("Reset All Data?");
                builder.setMessage("This will permanently clear all your portfolio, orders, and wallet balances. This action cannot be undone.");
                builder.setPositiveButton("Reset", (dialog, which) -> {
                    new Thread(() -> {
                        com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper odb = new com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper(context);
                        odb.clearAllOrders();
                        odb.close();

                        FundDatabase fdb_internal = new FundDatabase(context);
                        fdb_internal.getWritableDatabase().delete("Funds", null, null);
                        fdb_internal.getWritableDatabase().delete("StockTransactions", null, null);
                        fdb_internal.getWritableDatabase().delete("TradeHistory", null, null);
                        fdb_internal.close();

                        com.example.bottonnavigation_and_recyclerview_implement_homepage.SubscriptionManager.setPremium(context, false);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                refreshWalletBalances(getView());
                                refreshPremiumStatus();
                                Toast.makeText(context, "All data reset successfully", Toast.LENGTH_LONG).show();
                            });
                        }
                    }).start();
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            });
        }

        // Premium Button
        Button premiumBtn = view.findViewById(R.id.premium_btn);
        if (premiumBtn != null) {
            premiumBtn.setOnClickListener(v -> {
                if (com.example.bottonnavigation_and_recyclerview_implement_homepage.SubscriptionManager.isPremium(context)) {
                    Toast.makeText(context, "You are already a Premium user!", Toast.LENGTH_SHORT).show();
                } else {
                    com.example.bottonnavigation_and_recyclerview_implement_homepage.SubscriptionManager.startSubscriptionPayment(getActivity());
                }
            });
        }

        refreshPremiumStatus(view);

        return view;
    }

    private void refreshWalletBalances(View view) {
        if (view == null || getContext() == null) return;
        FundDatabase fdb = new FundDatabase(getContext());
        double inrBal = fdb.getInrBalance();
        double usdBal = fdb.getUsdBalance();

        if (inrFundTxt == null) inrFundTxt = view.findViewById(R.id.inr_fund_txt);
        if (usdFundTxt == null) usdFundTxt = view.findViewById(R.id.fund_txt);

        if (inrFundTxt != null) inrFundTxt.setText(String.format(Locale.getDefault(), "₹%.2f", inrBal));
        if (usdFundTxt != null) usdFundTxt.setText(String.format(Locale.getDefault(), "$%.2f", usdBal));
    }

    private void openCurrencyConverterDialog(String dateStr) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_currency_converter, null);
        dialog.setContentView(dialogView);

        TextView rateBadge = dialogView.findViewById(R.id.converter_rate_badge);
        TextView sourceLabel = dialogView.findViewById(R.id.source_label_txt);
        TextView sourceAvailTxt = dialogView.findViewById(R.id.source_avail_balance_txt);
        TextView destLabel = dialogView.findViewById(R.id.dest_label_txt);
        TextView destAvailTxt = dialogView.findViewById(R.id.dest_avail_balance_txt);

        EditText edtAmount = dialogView.findViewById(R.id.edt_convert_amount);
        TextView txtPreview = dialogView.findViewById(R.id.txt_convert_preview);
        ImageButton btnSwap = dialogView.findViewById(R.id.btn_swap_direction);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm_transfer);

        Button btn25 = dialogView.findViewById(R.id.btn_preset_25);
        Button btn50 = dialogView.findViewById(R.id.btn_preset_50);
        Button btn75 = dialogView.findViewById(R.id.btn_preset_75);
        Button btnMax = dialogView.findViewById(R.id.btn_preset_max);

        // Fetch Live Exchange Rate
        CurrencyConverter.fetchExchangeRate(getContext(), rate -> {
            liveUsdToInrRate = rate;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (rateBadge != null) {
                        rateBadge.setText(String.format(Locale.getDefault(), "1 USD = ₹%.2f", liveUsdToInrRate));
                    }
                    updateConverterDialogUI(sourceLabel, sourceAvailTxt, destLabel, destAvailTxt, edtAmount, txtPreview);
                });
            }
        });

        // Direction Swap Listener with smooth 180-degree rotation animation
        btnSwap.setOnClickListener(v -> {
            isSwappedDirection = !isSwappedDirection;
            btnSwap.animate().rotationBy(180f).setDuration(300).start();
            updateConverterDialogUI(sourceLabel, sourceAvailTxt, destLabel, destAvailTxt, edtAmount, txtPreview);
        });

        // Real-time Conversion Calculation Listener
        edtAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateConversionPreview(edtAmount.getText().toString(), txtPreview);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Preset Percentage Listeners
        View.OnClickListener presetListener = v -> {
            FundDatabase fdb = new FundDatabase(getContext());
            double Avl = !isSwappedDirection ? fdb.getInrBalance() : fdb.getUsdBalance();
            double factor = 1.0;

            int id = v.getId();
            if (id == R.id.btn_preset_25) factor = 0.25;
            else if (id == R.id.btn_preset_50) factor = 0.50;
            else if (id == R.id.btn_preset_75) factor = 0.75;
            else if (id == R.id.btn_preset_max) factor = 1.0;

            double selectedAmount = Avl * factor;
            edtAmount.setText(String.format(Locale.getDefault(), "%.2f", selectedAmount));
        };

        btn25.setOnClickListener(presetListener);
        btn50.setOnClickListener(presetListener);
        btn75.setOnClickListener(presetListener);
        btnMax.setOnClickListener(presetListener);

        // Confirm Transfer Listener
        btnConfirm.setOnClickListener(v -> {
            String inputStr = edtAmount.getText().toString().trim();
            double amountToConvert = parseDoubleSafe(inputStr);

            if (amountToConvert <= 0) {
                Toast.makeText(getContext(), "Please enter a valid transfer amount", Toast.LENGTH_SHORT).show();
                return;
            }

            FundDatabase fdb = new FundDatabase(getContext());
            double Avl = !isSwappedDirection ? fdb.getInrBalance() : fdb.getUsdBalance();

            if (amountToConvert > Avl) {
                Toast.makeText(getContext(), "Insufficient balance! Available: " + (isSwappedDirection ? "$" : "₹") + String.format(Locale.getDefault(), "%.2f", Avl), Toast.LENGTH_LONG).show();
                return;
            }

            double convertedReceived;
            String fromCurrency, toCurrency;

            if (!isSwappedDirection) { // INR -> USD
                fromCurrency = "INR";
                toCurrency = "USD";
                convertedReceived = CurrencyConverter.convertInrToUsd(amountToConvert, liveUsdToInrRate);
            } else { // USD -> INR
                fromCurrency = "USD";
                toCurrency = "INR";
                convertedReceived = CurrencyConverter.convertUsdToInr(amountToConvert, liveUsdToInrRate);
            }

            fdb.convertCurrency(fromCurrency, toCurrency, amountToConvert, convertedReceived, dateStr);
            refreshWalletBalances(getView());

            Toast.makeText(getContext(), String.format(Locale.getDefault(), "Transferred %s%.2f ➔ %s%.2f", !isSwappedDirection ? "₹" : "$", amountToConvert, !isSwappedDirection ? "$" : "₹", convertedReceived), Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        updateConverterDialogUI(sourceLabel, sourceAvailTxt, destLabel, destAvailTxt, edtAmount, txtPreview);
        dialog.show();
    }

    private void updateConverterDialogUI(TextView sourceLabel, TextView sourceAvailTxt, TextView destLabel, TextView destAvailTxt, EditText edtAmount, TextView txtPreview) {
        if (getContext() == null) return;
        FundDatabase fdb = new FundDatabase(getContext());
        double inrBal = fdb.getInrBalance();
        double usdBal = fdb.getUsdBalance();

        if (!isSwappedDirection) { // INR -> USD
            sourceLabel.setText("FROM (INR - ₹)");
            sourceAvailTxt.setText(String.format(Locale.getDefault(), "Avl: ₹%.2f", inrBal));

            destLabel.setText("TO (USD - $)");
            destAvailTxt.setText(String.format(Locale.getDefault(), "Avl: $%.2f", usdBal));
        } else { // USD -> INR
            sourceLabel.setText("FROM (USD - $)");
            sourceAvailTxt.setText(String.format(Locale.getDefault(), "Avl: $%.2f", usdBal));

            destLabel.setText("TO (INR - ₹)");
            destAvailTxt.setText(String.format(Locale.getDefault(), "Avl: ₹%.2f", inrBal));
        }

        updateConversionPreview(edtAmount.getText().toString(), txtPreview);
    }

    private void updateConversionPreview(String inputStr, TextView txtPreview) {
        if (txtPreview == null) return;
        double inputVal = parseDoubleSafe(inputStr);

        if (inputVal <= 0) {
            txtPreview.setText(String.format("You will receive: %s0.00", !isSwappedDirection ? "$" : "₹"));
            return;
        }

        double result;
        if (!isSwappedDirection) { // INR -> USD
            result = CurrencyConverter.convertInrToUsd(inputVal, liveUsdToInrRate);
            txtPreview.setText(String.format(Locale.getDefault(), "You will receive: $%.2f", result));
        } else { // USD -> INR
            result = CurrencyConverter.convertUsdToInr(inputVal, liveUsdToInrRate);
            txtPreview.setText(String.format(Locale.getDefault(), "You will receive: ₹%.2f", result));
        }
    }

    private double parseDoubleSafe(String str) {
        if (str == null || str.trim().isEmpty()) return 0.0;
        try {
            return Double.parseDouble(str.replace("₹", "").replace("$", "").replace(",", "").trim());
        } catch (Exception e) {
            return 0.0;
        }
    }

    public void refreshPremiumStatus() {
        if (getView() != null) {
            refreshPremiumStatus(getView());
        }
    }

    private void refreshPremiumStatus(View view) {
        TextView statusTxt = view.findViewById(R.id.premium_status_txt);
        if (statusTxt != null && getContext() != null) {
            boolean isPremium = com.example.bottonnavigation_and_recyclerview_implement_homepage.SubscriptionManager.isPremium(getContext());
            statusTxt.setText(isPremium ? "Status: PREMIUM" : "Status: Free");
            statusTxt.setTextColor(isPremium ? getResources().getColor(R.color.violet, null) : android.graphics.Color.BLACK);

            Button premiumBtn = view.findViewById(R.id.premium_btn);
            if (premiumBtn != null) {
                premiumBtn.setText(isPremium ? "Active" : "Get Premium");
            }
        }
    }
}
