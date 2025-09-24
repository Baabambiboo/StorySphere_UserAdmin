package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.SearchView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;

public class UserListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private UserAdapter adapter;
    private DBHelper db;

    private List<User> fullList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list); // ใช้ XML ที่คุณส่งมา

        db = new DBHelper(this);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // RecyclerView setup
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // SearchView setup (ต้องมี android:id="@+id/searchView" ใน XML)
        searchView = findViewById(R.id.searchView);

        // โหลดข้อมูล user จาก DB
        fullList = db.getAllUsers();
        adapter = new UserAdapter(fullList, user -> {
            Intent it = new Intent(UserListActivity.this, UserDetailActivity.class);
            it.putExtra("user_id", user.id);  // ส่ง id ไปหน้า UserDetailActivity
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // กรองข้อมูลเวลาพิมพ์ค้นหา
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);  // ใช้ method filter ของ UserAdapter
                    return true;
                }
            });
        }

        android.widget.ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(UserListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }
}
