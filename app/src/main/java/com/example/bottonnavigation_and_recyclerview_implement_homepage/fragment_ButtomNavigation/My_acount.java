package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import  com.example.bottonnavigation_and_recyclerview_implement_homepage.ProfitLossActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.MainActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.validation.loginPage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.Calendar;

public class My_acount extends Fragment {

    String strCurrency;
    Double fund;
    //context
    Activity context;

    RewardedAd rewardedAd;
    // add lode function code
    private void lodeRewardAdd(Activity context){
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(context, "ca-app-pub-5307098553494261/5464605667", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                rewardedAd = null;
            }

            @Override
            public void onAdLoaded(@NonNull RewardedAd add) {
                rewardedAd = add;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = (MainActivity)getActivity();
        lodeRewardAdd(context);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_acount, container, false);

        MobileAds.initialize(context,initializationStatus -> {});   //initialise addMob

        //date and time featching
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int Month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        String dateStr = date + "-" + Month + "-" + year;

        // fund code
        TextView fund_txt = view.findViewById(R.id.fund_txt);
        FundDatabase fdb = new FundDatabase(context);
        fund = fdb.getBalance();
        fund_txt.setText(String.valueOf(fund));

        // currency code
        ImageView currency = view.findViewById(R.id.currency_img);
        currency.setOnClickListener(v -> {
            currency.setImageResource(R.drawable.doller_logo);
            strCurrency = "USD";
            Toast.makeText(context, "Your currency is change in to USD", Toast.LENGTH_SHORT).show();
        });

        // add fund code
        Button addFundBtn = view.findViewById(R.id.add_fund_btn);
        addFundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (rewardedAd != null) {
                        rewardedAd.show(context, rewardItem -> {
                            // Reward given after watching video
                            fund = fund + 25000;
                            fdb.addFund(strCurrency, fund, dateStr);
                            fund_txt.setText(String.valueOf(fund));
                            Toast.makeText(context, "+25000 fund added (via Ad)", Toast.LENGTH_SHORT).show();

                            // Reload ad
                            lodeRewardAdd(context);
                        });
                    } else {
                        // Ad not ready → add fund after 10 sec
                        Toast.makeText(context, "Ad not loaded. Adding fund in 10 sec...", Toast.LENGTH_SHORT).show();

                        new android.os.Handler().postDelayed(() -> {
                            fund = fund + 25000;
                            fdb.addFund(strCurrency, fund, dateStr);
                            fund_txt.setText(String.valueOf(fund));
                            Toast.makeText(context, "+25000 fund added (fallback)", Toast.LENGTH_SHORT).show();
                        }, 10000); // 10 seconds delay

                        lodeRewardAdd(context);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Error showing ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Also fallback after 10 sec in case of exception
                    new android.os.Handler().postDelayed(() -> {
                        fund = fund + 25000;
                        fdb.addFund(strCurrency, fund, dateStr);
                        fund_txt.setText(String.valueOf(fund));
                        Toast.makeText(context, "+25000 fund added (exception fallback)", Toast.LENGTH_SHORT).show();
                    }, 10000);
                }
            }
        });


        // history and report code
        Button history_report_btn = view.findViewById(R.id.history_report_btn);
        history_report_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ProfitLossActivity.class);
                startActivity(intent);
            }
        });

        // Logout button code
        Button LogOut_btn = view.findViewById(R.id.LogOut_btn);
        LogOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, loginPage.class);
                startActivity(intent);
            }
        });

        // Reset Data button code
        Button resetBtn = view.findViewById(R.id.reset_data_btn);
        if (resetBtn != null) {
            resetBtn.setOnClickListener(v -> {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                builder.setTitle("Reset All Data?");
                builder.setMessage("This will permanently clear all your portfolio, orders, and funds. This action cannot be undone.");
                builder.setPositiveButton("Reset", (dialog, which) -> {
                    // Clear all databases
                    new Thread(() -> {
                        com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper odb = new com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper(context);
                        odb.clearAllOrders();
                        odb.close();

                        FundDatabase fdb_internal = new FundDatabase(context);
                        fdb_internal.getWritableDatabase().delete("Funds", null, null);
                        fdb_internal.getWritableDatabase().delete("StockTransactions", null, null);
                        fdb_internal.getWritableDatabase().delete("TradeHistory", null, null);
                        fdb_internal.close();

                        // Reset Premium Status
                        com.example.bottonnavigation_and_recyclerview_implement_homepage.SubscriptionManager.setPremium(context, false);

                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                fund_txt.setText("0.0");
                                fund = 0.0;
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

        // Premium button code
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

    public void refreshPremiumStatus() {
        if (getView() != null) {
            refreshPremiumStatus(getView());
        }
    }

    private void refreshPremiumStatus(View view) {
        TextView statusTxt = view.findViewById(R.id.premium_status_txt);
        if (statusTxt != null) {
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