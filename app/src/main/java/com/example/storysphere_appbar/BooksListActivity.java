package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

public class BooksListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchView searchView;            // @+id/searchViewBook
    private Spinner spinnerBook;              // map ไปที่ @+id/spinnerUser ใน XML
    private final List<String> categoryOpts = new ArrayList<>();

    private BooksAdapter adapter;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_book);

        db = new DBHelper(this);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // โหลดหนังสือทั้งหมด
        List<WritingItem> all = db.getAllWritingItems();
        adapter = new BooksAdapter(all, item -> {
            Intent it = new Intent(BooksListActivity.this, BookDetailActivity.class);
            it.putExtra("book_id", item.getId());
            startActivity(it);
        });
        recyclerView.setAdapter(adapter);

        // ===== Spinner (spinnerBook): กรองตามหมวดหมู่ =====
        // หมายเหตุ: XML ใช้ id "spinnerUser" อยู่แล้ว จับมาเป็นตัวแปร spinnerBook
        spinnerBook = findViewById(R.id.spinnerUser);

        categoryOpts.clear();
        categoryOpts.add("All books");
        categoryOpts.addAll(db.getAllCategoriesNonEmpty());   // ใช้เมธอดใหม่จาก DBHelper

        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryOpts
        );
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBook.setAdapter(catAdapter);

        spinnerBook.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    // "All books" → โชว์ทั้งหมด
                    // ถ้า BooksAdapter มีเมธอด reset ให้ใช้; ถ้าไม่มี ใช้ filter("") แทน
                    tryResetOrClearFilter();
                } else {
                    String selectedCat = categoryOpts.get(position);
                    // กรองด้วยข้อความ (ให้ BooksAdapter.filter กรอง title/tagline/tag/category)
                    adapter.filter(selectedCat);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { /* no-op */ }
        });

        // ===== SearchView: กรองด้วยคำค้น =====
        searchView = findViewById(R.id.searchViewBook);
        if (searchView != null) {
            searchView.setIconifiedByDefault(false);
            searchView.setQueryHint("Search...");
            searchView.clearFocus();
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

        // ===== ปุ่ม Home =====
        ImageView homeBtn = findViewById(R.id.imageView12);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(BooksListActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }

    /** ถ้า BooksAdapter มีเมธอด resetFilter() ก็เรียกใช้, ถ้าไม่มีก็ fallback เป็น filter("") */
    private void tryResetOrClearFilter() {
        try {
            // สะดวกถ้าในอะแดปเตอร์คุณมี resetFilter()
            BooksAdapter.class.getMethod("resetFilter").invoke(adapter);
        } catch (Exception ignore) {
            // ไม่มี resetFilter → ใช้ filter("") ให้โชว์ทั้งหมด
            adapter.filter("");
        }
    }
}
