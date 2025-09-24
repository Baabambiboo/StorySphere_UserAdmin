package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

public class BooksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;   // ใช้ฟิลด์ ไม่ประกาศซ้ำในเมธอด
    private BooksAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_book);

        db = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<WritingItem> all = db.getAllWritingItems();
        adapter = new BooksAdapter(all, item -> {
            Intent it = new Intent(BooksListActivity.this, BookDetailActivity.class);
            it.putExtra("book_id", item.getId());
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // ใช้ AppCompat SearchView (id ต้องตรงกับ XML: @+id/searchViewBook)
        searchView = findViewById(R.id.searchViewBook);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false); // ให้เป็นช่องค้นหาพร้อมพิมพ์
            searchView.setQueryHint("Search...");
            searchView.clearFocus();                 // ไม่เด้งคีย์บอร์ดทันที

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String q) {
                    adapter.filter(q);
                    return true;
                }
                @Override public boolean onQueryTextChange(String q) {
                    adapter.filter(q == null ? "" : q);
                    return true;
                }
            });
        }

        android.widget.ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(BooksListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }
}
