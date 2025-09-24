package com.example.storysphere_appbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class SearchResultsActivity extends AppCompatActivity {

    private DBHelper db;
    private RecyclerView rv;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        db = new DBHelper(this);

        String q = getIntent().getStringExtra("q");
        if (q == null) q = "";

        ArrayList<String> categories = getIntent().getStringArrayListExtra("categories");
        if (categories == null) categories = new ArrayList<>();

        tvHeader = findViewById(R.id.tvHeader);
        tvHeader.setText(buildHeader(q, categories));

        rv = findViewById(R.id.rvResults);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // 1) ถ้ามีคำค้น -> ค้นจาก title/tagline/tag
        //    ถ้าไม่มีคำค้น -> ดึงงานล่าสุด
        List<WritingItem> base = TextUtils.isEmpty(q)
                ? db.getRecentWritings(200)
                : db.searchWritings(q, 200);

        // 2) ถ้ามีเลือกหมวดหมู่ -> กรองด้วย category (เคสอินซิตีฟ)
        if (!categories.isEmpty()) {
            filterByCategories(base, categories);
        }

        // 3) แสดงผลด้วยการ์ดเดิม StoryAdapter
        rv.setAdapter(new StoryAdapter(this, base));
    }

    private String buildHeader(String q, ArrayList<String> categories) {
        StringBuilder sb = new StringBuilder("Results");
        if (!TextUtils.isEmpty(q)) {
            sb.append(" for: ").append(q);
        }
        if (!categories.isEmpty()) {
            sb.append("  •  Category: ").append(TextUtils.join(", ", categories));
        }
        return sb.toString();
    }

    /** กรองรายการให้เหลือเฉพาะที่ category ตรงกับตัวที่เลือก (เลือกหลายหมวดได้) */
    private void filterByCategories(List<WritingItem> list, ArrayList<String> cats) {
        // เตรียมเปรียบเทียบแบบไม่สนเคส
        ArrayList<String> normCats = new ArrayList<>();
        for (String c : cats) {
            if (!TextUtils.isEmpty(c)) normCats.add(c.trim().toLowerCase(Locale.ROOT));
        }
        if (normCats.isEmpty()) return;

        Iterator<WritingItem> it = list.iterator();
        while (it.hasNext()) {
            WritingItem w = it.next();
            String cat = w.getCategory();
            String tagCsv = w.getTag();

            boolean match = false;

            // 1) ตรง category ตรง ๆ
            if (!TextUtils.isEmpty(cat)) {
                String norm = cat.trim().toLowerCase(Locale.ROOT);
                for (String want : normCats) {
                    // เทียบชื่อแบบตรง เช่น "Romance", "Drama", "Sci-fi", ...
                    if (norm.equals(want)) { match = true; break; }
                    // เผื่อเคสสะกดมีขีด/ไม่มีขีด
                    if (norm.replace("-", "").equals(want.replace("-", ""))) { match = true; break; }
                }
            }

            // 2) เผื่อบางงานไม่ได้ใส่ category แต่มี tag -> ลองเช็ค tag ด้วย
            if (!match && !TextUtils.isEmpty(tagCsv)) {
                String csv = tagCsv.toLowerCase(Locale.ROOT).replace(" ", "");
                for (String want : normCats) {
                    String needle = want.replace("-", "");
                    if (("," + csv + ",").contains("," + needle + ",")) { match = true; break; }
                }
            }

            if (!match) it.remove();
        }
    }
}
