package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class IndicatorDialogFragment extends BottomSheetDialogFragment {

    public interface IndicatorListener {
        void onIndicatorSelected(String indicatorName);
    }

    private IndicatorListener listener;
    private static final List<String> INDICATORS = Arrays.asList(
        "EMA 20", "EMA 50", "EMA 100", "EMA 200",
        "SMA 20", "SMA 50", "SMA 200",
        "Bollinger Bands", "VWAP", "MACD", "Volume"
    );

    public static IndicatorDialogFragment newInstance(IndicatorListener listener) {
        IndicatorDialogFragment fragment = new IndicatorDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_indicator_selector, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.indicator_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new IndicatorAdapter());
        return view;
    }

    private class IndicatorAdapter extends RecyclerView.Adapter<IndicatorAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = INDICATORS.get(position);
            holder.text.setText(name);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onIndicatorSelected(name);
                dismiss();
            });
        }

        @Override
        public int getItemCount() { return INDICATORS.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View v) { super(v); text = v.findViewById(android.R.id.text1); }
        }
    }
}
