package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    // Views
    private RecyclerView recyclerView;
    private SearchView searchView;
    private Spinner spinnerUser;

    // Data / helpers
    private final List<User> fullList = new ArrayList<>();
    private final List<String> nameOptions = new ArrayList<>();
    private UserAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);

        db = new DBHelper(this);

        // Toolbar (ปุ่ม back)
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SearchView
        searchView = findViewById(R.id.searchView);

        // โหลด Users สำหรับลิสต์หลัก
        fullList.clear();
        fullList.addAll(db.getAllUsers());

        adapter = new UserAdapter(fullList, user -> {
            Intent it = new Intent(UserListActivity.this, UserDetailActivity.class);
            it.putExtra("user_id", user.id);
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // ========= Spinner (ทางเลือก A): ดึงเฉพาะ username ที่ไม่ว่าง =========
        spinnerUser = findViewById(R.id.spinnerUser);

        nameOptions.clear();
        nameOptions.add("All users");                       // ตัวเลือกแสดงทั้งหมด
        nameOptions.addAll(db.getAllUsernamesNonEmpty());   // <-- เมธอดใหม่ใน DBHelper

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, nameOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUser.setAdapter(spinnerAdapter);

        spinnerUser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    adapter.resetFilter(); // แสดงทั้งหมด
                } else {
                    String selected = nameOptions.get(position);
                    adapter.filterBySpinnerSelection(selected); // exact -> ไม่เจอค่อย contains
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });

        // ========= SearchView: กรองแบบพิมพ์คำค้น =========
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String query) { return false; }
                @Override public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);  // ทำงานร่วมกับตัวกรอง spinner ได้
                    return true;
                }
            });
        }

        // ========= ปุ่ม Home =========
        ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(UserListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }
}
