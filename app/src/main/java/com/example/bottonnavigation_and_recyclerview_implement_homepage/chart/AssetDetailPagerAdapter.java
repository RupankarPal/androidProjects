package com.example.bottonnavigation_and_recyclerview_implement_homepage.chart;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AssetDetailPagerAdapter extends FragmentStateAdapter {

    private final String symbol;

    public AssetDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, String symbol) {
        super(fragmentActivity);
        this.symbol = symbol;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return ChartFragment.newInstance(symbol);
            case 1:
                return FundamentalFragment.newInstance(symbol);
            case 2:
                return AiAnalysisFragment.newInstance(symbol);
            default:
                return ChartFragment.newInstance(symbol);
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
