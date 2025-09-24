package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.res.ColorStateList;
import androidx.core.view.ViewCompat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class activity_follow_writer extends AppCompatActivity {

    public static final String EXTRA_USERNAME = "extra_username";
    public static final String EXTRA_EMAIL    = "extra_email";

    private DBHelper db;
    private String email;        // อีเมลของนักเขียน (เจ้าของหน้า)
    private String username;     // username ของนักเขียน
    private String viewerEmail;  // อีเมลผู้ใช้งานปัจจุบัน (สำหรับบันทึก follow)

    private ImageView ivAvatar;
    private TextView tvNameAuthor;
    private RecyclerView rvWritings;
    private Button btnFollow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_writer);

        // ----- Bottom Nav -----
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
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
            });
        }

        db = new DBHelper(this);

        // ----- Toolbar -----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (toolbar != null) toolbar.setNavigationOnClickListener(v -> finish());

        // ให้ไอคอนจุดสามจุดชัด ๆ (ถ้ามีไฟล์)
        Drawable more = AppCompatResources.getDrawable(this, R.drawable.overflow_menu_vertical_svgrepo_com);
        if (more != null && toolbar != null) {
            Drawable w = DrawableCompat.wrap(more.mutate());
            DrawableCompat.setTint(w, ContextCompat.getColor(this, R.color.black));
            toolbar.setOverflowIcon(w);
        }

        // ----- Views -----
        ivAvatar     = findViewById(R.id.ivAvatar);
        tvNameAuthor = findViewById(R.id.tvNameAuthor);
        btnFollow    = findViewById(R.id.btnFollow);
        rvWritings   = findViewById(R.id.rvWritings);

        rvWritings.setLayoutManager(new LinearLayoutManager(this));
        rvWritings.setHasFixedSize(true);
        rvWritings.setNestedScrollingEnabled(false);

        // กัน warning: ตั้ง adapter ว่างไว้ก่อน
        rvWritings.setAdapter(new BookmarksAdapter(this, java.util.Collections.emptyList()));

        // ผู้ชมปัจจุบัน (อาจเป็น null ได้ ถ้ายังไม่ล็อกอิน)
        viewerEmail = db.getLoggedInUserEmail();

        // รับค่า “นักเขียน” จาก Intent
        email    = getIntent().getStringExtra(EXTRA_EMAIL);
        username = getIntent().getStringExtra(EXTRA_USERNAME);
        if (email == null || email.trim().isEmpty()) {
            String sessionEmail = db.getLoggedInUserEmail();
            if (sessionEmail != null && !sessionEmail.trim().isEmpty()) email = sessionEmail.trim();
        }

        // Header + Follow + รายการงานเขียน
        bindAuthorHeader();
        setupFollowButton();
        loadAuthorWritings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupFollowButton(); // รีเฟรชสถานะ
        loadAuthorWritings();
    }

    // ====== เมนูสามจุด (overflow) ======
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reading_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_report) {
            PopupUtil.showConfirm(
                    this,
                    "คุณต้องการรายงานผู้ใช้นี้หรือไม่?",
                    () -> Toast.makeText(this, "รายงานผู้เขียนแล้ว", Toast.LENGTH_SHORT).show()
            );
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    // ====================================

    private void bindAuthorHeader() {
        String displayName = null;

        if (email != null && !email.trim().isEmpty()) {
            try (Cursor u = db.getUserByEmail(email)) {
                if (u != null && u.moveToFirst()) {
                    int unIdx  = u.getColumnIndex("username");
                    int imgIdx = u.getColumnIndex("image_uri");

                    if (unIdx >= 0) displayName = u.getString(unIdx);

                    if (imgIdx >= 0) {
                        String imageUri = u.getString(imgIdx);
                        if (imageUri != null && !imageUri.trim().isEmpty()) {
                            try { ivAvatar.setImageURI(Uri.parse(imageUri)); } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } else if (username != null && !username.trim().isEmpty()) {
            displayName = username.trim();
        }

        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = (email != null && email.contains("@"))
                    ? email.substring(0, email.indexOf('@'))
                    : getString(R.string.app_name);
        }
        tvNameAuthor.setText(displayName);
    }

    private void setupFollowButton() {
        if (btnFollow == null) return;

        btnFollow.setVisibility(Button.VISIBLE);
        btnFollow.setEnabled(true);

        final String curUser = (viewerEmail == null || viewerEmail.trim().isEmpty()) ? null : viewerEmail.trim();
        final String author  = (email == null) ? "" : email.trim();

        // ถ้าไม่ล็อกอินก็ยังให้เห็นปุ่มสีม่วง (แต่ตอนกดจะเตือนให้ล็อกอิน)
        boolean following = (curUser != null) && db.isFollowing(curUser, author);
        applyFollowUi(following);

        btnFollow.setOnClickListener(v -> {
            if (curUser == null) {
                Toast.makeText(this, "กรุณาเข้าสู่ระบบก่อน", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean now = db.isFollowing(curUser, author);
            boolean ok  = db.setFollow(curUser, author, !now);
            if (ok) {
                applyFollowUi(!now);  // เปลี่ยนสี/ข้อความทันที
                Toast.makeText(this, !now ? "ติดตามแล้ว" : "ยกเลิกการติดตาม", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "ไม่สำเร็จ กรุณาลองอีกครั้ง", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFollowUi(boolean followed) {
        if (btnFollow == null) return;

        btnFollow.setText(followed ? "Followed" : "Follow");
        btnFollow.setTextColor(ContextCompat.getColor(this, android.R.color.white));

        // ใช้ทรงเดิมของปุ่ม แล้ว Tint สีทับ (ไม่เปลี่ยนรูปร่าง)
        btnFollow.setBackgroundResource(R.drawable.bg_tag_chip);
        int color = ContextCompat.getColor(this,
                followed ? android.R.color.darker_gray : R.color.purple1);
        ViewCompat.setBackgroundTintList(btnFollow, ColorStateList.valueOf(color));

        btnFollow.setEnabled(true); // ให้สลับ follow/unfollow ได้
    }

    private void loadAuthorWritings() {
        List<WritingItem> items = null;

        if (email != null && !email.trim().isEmpty()) {
            items = db.getWritingItemsByAuthorEmail(email.trim());
        }
        if (items == null || items.isEmpty()) {
            String key = (username != null && !username.trim().isEmpty())
                    ? username.trim()
                    : (email != null ? emailLocalPart(email) : null);
            if (key != null && !key.trim().isEmpty()) {
                items = db.getWritingItemsByUsername(key);
            }
        }
        if (items == null) items = java.util.Collections.emptyList();

        BookmarksAdapter ad = (BookmarksAdapter) rvWritings.getAdapter();
        if (ad == null) {
            rvWritings.setAdapter(new BookmarksAdapter(this, items));
        } else {
            ad.replace(items);   // ใช้เมธอด replace ในอแดปเตอร์ของคุณ
        }
    }

    private void openWritingDetail(WritingItem item) {
        if (item == null) return;
        Intent i = new Intent(this, ReadingMainActivity.class);
        i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, item.getId());
        startActivity(i);
    }

    private String emailLocalPart(String mail) {
        if (mail == null) return "";
        int at = mail.indexOf('@');
        return at > 0 ? mail.substring(0, at) : mail;
    }
}
