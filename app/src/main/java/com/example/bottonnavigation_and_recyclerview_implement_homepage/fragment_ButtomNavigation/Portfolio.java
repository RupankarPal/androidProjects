package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.PortfolioAdapter;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.portfolio_stocks_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class Portfolio extends Fragment {


    public Portfolio() {
        // Required empty public constructor
    }

    private TextView invest_amount_txt, current_amount_txt, total_pl_txt, todays_change_txt,backToWatchlist_txt;
    private RecyclerView recyclerView;
    private Button goToWatchlist_btn;
    private ImageView shuffelrd_img_btn;
    private PortfolioAdapter adapter;
    private ArrayList<portfolio_stocks_model> arrayList;
    private Context context;

    private  Runnable runnable;
    private Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_portfolio, container, false);

        context = getActivity();

        //initilization
        recyclerView = view.findViewById(R.id.recyclerView);
        shuffelrd_img_btn = view.findViewById(R.id.shorted_img_btn);
        invest_amount_txt = view.findViewById(R.id.invested_amount_txt);
        current_amount_txt = view.findViewById(R.id.current_amount_txt);
        total_pl_txt = view.findViewById(R.id.total_pl_txt);
        todays_change_txt = view.findViewById(R.id.todays_change_txt);
        backToWatchlist_txt = view.findViewById(R.id.backToWatchlist_txt);
        goToWatchlist_btn = view.findViewById(R.id.goToWatchlist_btn);

        arrayList = new ArrayList<>();

        runnable = new Runnable() {
            @Override
            public void run() {
                updateUI();
                handler.postDelayed(this,5000);
            }
        };
        handler.postDelayed(runnable, 5000);


        return view;
    }

    private void updateUI(){
        if (arrayList.isEmpty()){
            recyclerView.setVisibility(View.GONE);
            goToWatchlist_btn.setVisibility(View.VISIBLE);
            backToWatchlist_txt.setVisibility(View.VISIBLE);
            loderec();
        }else {
            recyclerView.setVisibility(View.VISIBLE);
            goToWatchlist_btn.setVisibility(View.GONE);
            backToWatchlist_txt.setVisibility(View.GONE);
            loderec();
        }

        goToWatchlist_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomNavigationView bnView = getActivity().findViewById(R.id.bottom_navigation);
                bnView.setSelectedItemId(R.id.watchlist);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.main_activity,new watchlist());
                ft.commit();
            }
        });
    }

    private void loderec(){

        adapter = new PortfolioAdapter(context,arrayList);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);
    }



}