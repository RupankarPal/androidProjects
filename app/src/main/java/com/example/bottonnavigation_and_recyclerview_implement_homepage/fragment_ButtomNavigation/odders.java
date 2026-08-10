package com.example.bottonnavigation_and_recyclerview_implement_homepage.fragment_ButtomNavigation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.Adapter.Recycler_stock_row_Adapter_order;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.processing_packages.OrderWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class odders extends Fragment {

    private BottomNavigationView bnView;
    private Button gotowatchlist;
    private RecyclerView recyclerView_odders;
    private Recycler_stock_row_Adapter_order adapterOrder;

    private ArrayList<Order_model> orderInfo = new ArrayList<>();
    
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAdded()) {
                refreshOrders();
                handler.postDelayed(this, 2000); // Fast 2s refresh
            }
        }
    };

    public static void setAfterProcessOrderArr(ArrayList<Order_model> list) {
        // Kept for backward compatibility but no longer used as we read directly from DB
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_odders, container, false);

        // RecyclerView setup
        recyclerView_odders = view.findViewById(R.id.orders_rec_view);
        recyclerView_odders.setLayoutManager(new LinearLayoutManager(getContext()));

        adapterOrder = new Recycler_stock_row_Adapter_order(getContext(), orderInfo);
        recyclerView_odders.setAdapter(adapterOrder);

        // Button: go to watchlist
        gotowatchlist = view.findViewById(R.id.go_to_Witchlist_btn);
        gotowatchlist.setOnClickListener(v -> {
            if (getActivity() != null) {
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

    private void refreshOrders() {
        if (getContext() == null) return;
        new Thread(() -> {
            OrdersDatabaseHelper odb = new OrdersDatabaseHelper(getContext());
            ArrayList<Order_model> list = odb.getAllOrders();
            odb.close();
            new Handler(Looper.getMainLooper()).post(() -> {
                if (isAdded()) {
                    orderInfo.clear();
                    orderInfo.addAll(list);
                    adapterOrder.notifyDataSetChanged();
                }
            });
        }).start();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshOrders();
        handler.post(refreshRunnable);
        startBackgroundCheck();
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(refreshRunnable);
    }

    private void startBackgroundCheck() {
        Context context = getContext();
        if (context == null) return;

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(OrderWorker.class)
                .setInitialDelay(2, TimeUnit.SECONDS)
                .build();

        WorkManager.getInstance(context).enqueue(workRequest);

        WorkManager.getInstance(context).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(getViewLifecycleOwner(), workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        refreshOrders(); // Refresh UI once worker completes
                    }
                });
    }
}
