package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.Recycler_stock_row_Adapter_order;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.order_process;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class odders extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private BottomNavigationView bnView;
    private Button gotowatchlist;
    private Button allBtn,openBtn, closeBtn;
    private RecyclerView recyclerView_odders;
    private Context context;
    private Recycler_stock_row_Adapter_order Adapter_Order;
    private ArrayList<Order_model> orderInfo = new ArrayList<>();
    private ArrayList<Order_model> afterProcessOrder_arr = new ArrayList<>();

    // function order cheacking till 3:30 pm
    private ArrayList<Order_model> continiousCheck(ArrayList<Order_model> orderInfo){
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Calendar now = Calendar.getInstance();
                int hower = now.get(Calendar.HOUR_OF_DAY);
                int min = now.get(Calendar.MINUTE);
                if (hower == 15 && min == 30 || hower > 15 ) {
                    timer.cancel();
                    return;
                }
                order_process afterProcessOrder = new order_process(context,orderInfo);
                afterProcessOrder_arr = afterProcessOrder.getOrderCurrentReport();
            }
        },0,1000);
        return afterProcessOrder_arr;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getActivity(); // get the context

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_odders, container, false);

        // recycler view setup
        recyclerView_odders = view.findViewById(R.id.orders_rec_view);
        recyclerView_odders.setLayoutManager(new LinearLayoutManager(context));
        if (getArguments()!=null) {
            orderInfo = (ArrayList<Order_model>) getArguments().getSerializable("OrderInfo_From_Main_Activity");
        }
        ArrayList<Order_model> arrayList = continiousCheck(orderInfo);
        Adapter_Order = new Recycler_stock_row_Adapter_order(context,arrayList); // data add in the adupter
        Adapter_Order.notifyDataSetChanged();
        recyclerView_odders.setAdapter(Adapter_Order);

        //inflate odderfragment -> watchlist fregment by go_to_watchlist button
        gotowatchlist = view.findViewById(R.id.go_to_Witchlist_btn);
        gotowatchlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bnView = getActivity().findViewById(R.id.bottom_navigation);
                bnView.setSelectedItemId(R.id.watchlist);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.main_activity, new watchlist());
                ft.commit();
            }
        });

        return view;
    }
}