package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private EditText et;
    private ChipGroup chipGroupCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Toolbar + ปุ่ม Back
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");
        toolbar.setNavigationOnClickListener(v -> finish());

        // ช่องค้นหา (มาจาก include menu_action_search_pill2)
        et = findViewById(R.id.etSearch);
        if (et != null) {
            // โชว์เคอร์เซอร์/คีย์บอร์ดทันทีเมื่อเข้าหน้านี้
            et.setCursorVisible(true);
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.post(() -> {
                android.view.inputmethod.InputMethodManager imm =
                        (android.view.inputmethod.InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(et, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            });

            // กดปุ่ม Search/Enter -> ไปหน้า SearchResultsActivity
            et.setOnEditorActionListener((v, actionId, event) -> {
                boolean isEnter = event != null &&
                        event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                        event.getAction() == KeyEvent.ACTION_DOWN;

                if (actionId == EditorInfo.IME_ACTION_SEARCH || isEnter) {
                    openResultsWithFilters();
                    return true;
                }
                return false;
            });
        }

        // ChipGroup หมวดหมู่ (หลายตัวเลือก)
        chipGroupCategories = findViewById(R.id.chipGroupCategories);
        if (chipGroupCategories != null) {
            // เมื่อแตะ chip ใด ๆ ก็ให้ยิงผลลัพธ์ทันที (ใช้ตัวเดิมที่หน้า Home)
            chipGroupCategories.setOnCheckedStateChangeListener((group, checkedIds) -> openResultsWithFilters());
        }
    }

    /** ดึง q และรายชื่อหมวดหมู่ที่ติ๊กไว้ แล้วเปิดหน้า SearchResultsActivity */
    private void openResultsWithFilters() {
        String q = et != null ? et.getText().toString().trim() : "";

        ArrayList<String> categories = new ArrayList<>();
        if (chipGroupCategories != null) {
            for (int i = 0; i < chipGroupCategories.getChildCount(); i++) {
                Chip chip = (Chip) chipGroupCategories.getChildAt(i);
                if (chip.isChecked()) {
                    // เก็บชื่อหมวดตามที่แสดง เช่น "Romance", "Drama", ...
                    categories.add(chip.getText().toString());
                }
            }
        }

        Intent i = new Intent(this, SearchResultsActivity.class);
        i.putExtra("q", q);
        i.putStringArrayListExtra("categories", categories);
        startActivity(i);
    }
}
