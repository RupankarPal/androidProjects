package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bottonnavigation_and_recyclerview_implement_homepage.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Arrays;
import java.util.List;

public class DrawingDialogFragment extends BottomSheetDialogFragment {

    public interface DrawingToolListener {
        void onToolSelected(String toolName);
    }

    private DrawingToolListener listener;
    private static final List<String> TOOLS = Arrays.asList(
        "Cursor", "Trend Line", "Horizontal Line", "Brush", "Price Range", "Clear All"
    );

    public static DrawingDialogFragment newInstance(DrawingToolListener listener) {
        DrawingDialogFragment fragment = new DrawingDialogFragment();
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_drawing_selector, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.drawing_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(new DrawingAdapter());
        return view;
    }

    private class DrawingAdapter extends RecyclerView.Adapter<DrawingAdapter.ViewHolder> {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String name = TOOLS.get(position);
            holder.text.setText(name);
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onToolSelected(name);
                dismiss();
            });
        }

        @Override
        public int getItemCount() { return TOOLS.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView text;
            ViewHolder(View v) { super(v); text = v.findViewById(android.R.id.text1); }
        }
    }
}
