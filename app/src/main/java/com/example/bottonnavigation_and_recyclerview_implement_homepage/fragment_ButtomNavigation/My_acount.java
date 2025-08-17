package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.BuySellOrderWindow;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.MainActivity;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.loginPage;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link My_acount#newInstance} factory method to
 * create an instance of this fragment.
 */
public class My_acount extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public My_acount() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment My_acount.
     */
    // TODO: Rename and change types and number of parameters
    public static My_acount newInstance(String param1, String param2) {
        My_acount fragment = new My_acount();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

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

        // Logout code
        Button LogOut_btn = view.findViewById(R.id.LogOut_btn);
        LogOut_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, loginPage.class);
                startActivity(intent);
            }
        });


        // fund code
        TextView fund_txt = view.findViewById(R.id.fund_txt);
        // add fund code
        Button addFundBtn = view.findViewById(R.id.add_fund_btn);
        addFundBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    try {
                        if (rewardedAd != null) {
                            rewardedAd.show(context, rewardItem -> {
                                // Reward the user after watching video
                                fund = fund + 25000;
                                fund_txt.setText(String.valueOf(fund));
                                Toast.makeText(context, "+25000 fund added", Toast.LENGTH_SHORT).show();
                                // Reload ad for next time
                               lodeRewardAdd(context);
                            });
                        } else {
                            // Ad not ready
                            Toast.makeText(context, "Ad not loaded yet. Please wait...", Toast.LENGTH_SHORT).show();
                            lodeRewardAdd(context);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Error showing ad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

            }
        });


        return view;
    }

    private  Runnable runnable;
    private Handler handler = new Handler();



}