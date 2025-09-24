package com.example.storysphere_appbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class TopChartActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private TopChartPagerAdapter pagerAdapter;

    /** ใช้คลาสปกติแทน lambda เพราะ BroadcastReceiver ไม่ใช่ functional interface */
    private final BroadcastReceiver writingChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshTabs();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topchart);

        // ----- Toolbar -----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // ----- Tabs + ViewPager -----
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        pagerAdapter = new TopChartPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Favorite" : "View");
        }).attach();

        // ----- BottomNav -----
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(this::onBottomNavItemSelected);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                writingChangedReceiver,
                new IntentFilter(EventCenter.ACTION_WRITING_CHANGED)
        );
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(writingChangedReceiver);
        super.onStop();
    }

    /** รีเฟรชข้อมูลใน 2 แท็บ (ให้แน่ใจว่า TopChartPagerAdapter มีเมธอดนี้) */
    private void refreshTabs() {
        if (pagerAdapter != null) {
            pagerAdapter.refreshAll();
        }
    }

    private boolean onBottomNavItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home) {
            startActivity(new Intent(this, HomeActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_library) {
            startActivity(new Intent(this, LibraryHistoryActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_writing) {
            startActivity(new Intent(this, activity_writing.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        } else if (id == R.id.nav_activity) {
            startActivity(new Intent(this, UserActivity.class));
            overridePendingTransition(0, 0);
            finish();
            return true;
        }
        return false;
    }
}
