package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.FundDatabase;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.DatabaseClasses.OrdersDatabaseHelper;
import com.example.bottonnavigation_and_recyclerview_implement_homepage.Model.Order_model;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Calendar;

public class OrderOptionsDialogFragment extends BottomSheetDialogFragment {

    private Order_model order;

    public static OrderOptionsDialogFragment newInstance(Order_model order) {
        OrderOptionsDialogFragment fragment = new OrderOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("order", order);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            order = (Order_model) getArguments().getSerializable("order");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_order_options, container, false);

        TextView title = view.findViewById(R.id.order_options_title);
        title.setText(order.getStock_name() + " - " + order.getOrder_type());

        // Constraint check: Order must be OPEN
        if (order.getExicuted_quantity() >= order.getStock_quantity()) {
            Toast.makeText(getContext(), "Order already executed", Toast.LENGTH_SHORT).show();
            dismiss();
            return view;
        }

        view.findViewById(R.id.btn_cancel_order).setOnClickListener(v -> {
            OrdersDatabaseHelper odb = new OrdersDatabaseHelper(getContext());
            FundDatabase fdb = new FundDatabase(getContext());
            
            // Refund funds if it was a BUY order
            if (order.getOrder_type().equalsIgnoreCase("BUY")) {
                double totalValue = order.getOrder_prise() * order.getStock_quantity();
                double marginUsed = order.getProduct_type().equalsIgnoreCase("INTRADAY") ? totalValue / 5.0 : totalValue;
                
                Calendar cal = Calendar.getInstance();
                String date = cal.get(Calendar.DAY_OF_MONTH) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR);
                fdb.addFund("USD", marginUsed, date); // Refund by adding fund back
            }

            odb.cancelOrder(order.getId());
            odb.close();
            fdb.close();
            
            Toast.makeText(getContext(), "Order Cancelled & Funds Refunded", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        view.findViewById(R.id.btn_modify_order).setOnClickListener(v -> {
            BuySellDialogFragment modifyDialog = BuySellDialogFragment.newInstanceForEdit(order);
            modifyDialog.show(getParentFragmentManager(), "ModifyOrderDialog");
            dismiss();
        });

        return view;
    }
}
