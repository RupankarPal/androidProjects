package com.example.bottonnavigation_and_recyclerview_implement_homepage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class IntervalDialogFragment extends BottomSheetDialogFragment {

    public interface IntervalListener {
        void onIntervalSelected(String intervalLabel, String yahooInterval, String yahooRange);
    }

    private IntervalListener listener;
    private static final List<String[]> INTERVALS = Arrays.asList(
        new String[]{"5m", "5m", "1d"},
        new String[]{"15m", "15m", "1d"},
        new String[]{"30m", "30m", "1d"},
        new String[]{"1h", "1h", "5d"},
        new String[]{"1d", "1d", "1mo"},
        new String[]{"1w", "1wk", "3mo"},
        new String[]{"1mo", "1mo", "1y"}
    );

    public static IntervalDialogFragment newInstance(IntervalListener listener) {
        IntervalDialogFragment fragment = new IntervalDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_interval_selector, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.interval_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new IntervalAdapter());
        return view;
    }

    private class IntervalAdapter extends RecyclerView.Adapter<IntervalAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String[] data = INTERVALS.get(position);
            holder.text.setText(data[0]);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onIntervalSelected(data[0], data[1], data[2]);
                dismiss();
            });
        }

        @Override
        public int getItemCount() { return INTERVALS.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View v) { super(v); text = v.findViewById(android.R.id.text1); }
        }
    }
}
