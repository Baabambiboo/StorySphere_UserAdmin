package com.example.storysphere_appbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TopViewsFragment extends Fragment {

    private RecyclerView recyclerView;
    private TopChartRowAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_top_views, container, false);

        recyclerView = v.findViewById(R.id.rvTopViews);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // โหลดข้อมูลตอนแรก
        DBHelper db = new DBHelper(getContext());
        List<WritingItem> data = db.getTopWritingsByViews(100);

        adapter = new TopChartRowAdapter(
                data,
                item -> {
                    if (getContext() != null) {
                        startActivity(ReadingMainActivityLauncher.launch(getContext(), item.getId()));
                    }
                },
                /* lockBookmarkTint = */ false
        );
        recyclerView.setAdapter(adapter);

        return v;
    }

    /** ✅ method สำหรับ refresh เรียกจาก TopChartPagerAdapter */
    public void reloadData() {
        if (getContext() == null || adapter == null) return;
        DBHelper db = new DBHelper(getContext());
        List<WritingItem> newData = db.getTopWritingsByViews(100);
        adapter.replaceData(newData);
    }
}
