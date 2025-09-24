package com.example.storysphere_appbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class TopChartPagerAdapter extends FragmentStateAdapter {

    private TopFavoriteFragment favFragment;
    private TopViewsFragment viewsFragment;

    public TopChartPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            favFragment = new TopFavoriteFragment();
            return favFragment;
        } else {
            viewsFragment = new TopViewsFragment();
            return viewsFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    /** ✅ ให้ Activity เรียกเพื่อรีเฟรชใหม่ */
    public void refreshAll() {
        if (favFragment != null) favFragment.reloadData();
        if (viewsFragment != null) viewsFragment.reloadData();
    }
}
