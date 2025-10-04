package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class activity_writing extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WritingAdapter adapter;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing);

        // Bottom nav
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_writing);
        bottomNav.setOnItemSelectedListener(item -> {
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
                // อยู่หน้าเดิมแล้ว ไม่ต้องรีสตาร์ท
                return true;

            } else if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // คัดลอกฐานจาก assets มาครั้งแรก (ถ้ายังไม่มี)
        DatabaseCopier.copyIfNeeded(this, DBHelper.DATABASE_NAME);

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerViewWriting);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dbHelper = new DBHelper(this);
        loadData();
    }

    private void loadData() {
        String email = dbHelper.getLoggedInUserEmail();   // ← อีเมลผู้ใช้ที่ล็อกอิน
        List<WritingItem> list;

        if (email == null || email.trim().isEmpty()) {
            // ยังไม่ล็อกอิน: ให้แสดงว่าง (หรือจะพาไปหน้า Login ก็ได้)
            list = java.util.Collections.emptyList();
        } else {
            // โชว์เฉพาะงานของฉัน
            list = dbHelper.getWritingItemsByAuthorEmail(email);
        }

        if (adapter == null) {
            adapter = new WritingAdapter(this, list);
            recyclerView.setAdapter(adapter);
        } else {
            // ถ้าคุณยังไม่ได้เพิ่ม ให้ใส่เมธอด replace(..) ใน WritingAdapter ตามนี้:
            // public void replace(List<WritingItem> newItems){ this.items.clear(); if(newItems!=null) this.items.addAll(newItems); notifyDataSetChanged(); }
            adapter.replace(list);
        }

        // (ทางเลือก) แสดง empty state ถ้าไม่มีงานเขียน
        View empty = findViewById(R.id.emptyView); // ใส่ TextView ใน layout ถ้ายังไม่มี
        if (empty != null) {
            empty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    public void AddNewWritng(View view) {
        Intent intent = new Intent(this, Writing_Add_Episode1.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, MainActivity.class); // ถ้าอยากกลับไปหน้า Login/หลัก
        startActivity(intent);
        finish(); // ปิดหน้านี้ไม่ให้ย้อนกลับมาได้
        return true;
    }
}
