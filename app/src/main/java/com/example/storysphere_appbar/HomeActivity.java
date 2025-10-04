package com.example.storysphere_appbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.chip.Chip;

import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageView ivProfile;
    private BottomNavigationView bottomNav;

    private RecyclerView rvTopChart;
    private TopChartRowAdapter topChartAdapter;

    // ------- เพิ่มตัวแปร field สำหรับ Banner + DB -------
    private ViewPager2 bannerPager;
    private ImageView bannerPlaceholder;
    private LinearLayout dots;
    private DBHelper db;

    /** ฟังเหตุการณ์สถิติเปลี่ยน (like, view, bookmark) แล้วรีโหลด Top Chart บนหน้า Home */
    private final android.content.BroadcastReceiver writingChangedReceiver =
            new android.content.BroadcastReceiver() {
                @Override
                public void onReceive(android.content.Context context, Intent intent) {
                    refreshTop3();
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // ---------- init DB + Banner views (ประกาศครั้งเดียว) ----------
        db = new DBHelper(this);
        bannerPager       = findViewById(R.id.bannerPager);
        bannerPlaceholder = findViewById(R.id.bannerPlaceholder);
        dots              = findViewById(R.id.dots);

        // ===== Search pill =====
        EditText etSearch = findViewById(R.id.etSearch);
        if (etSearch != null) {
            etSearch.setFocusable(false);
            etSearch.setFocusableInTouchMode(false);
            etSearch.setCursorVisible(false);
            etSearch.setLongClickable(false);
            etSearch.setOnClickListener(v ->
                    startActivity(new Intent(this, SearchActivity.class)));
        }

        // ===== กัน Toolbar โดน status bar =====
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final View appBar = findViewById(R.id.appbar);
        ViewCompat.setOnApplyWindowInsetsListener(appBar, (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            v.setPadding(v.getPaddingLeft(), sb.top, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        // ===== Toolbar =====
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);

        // ===== Profile icon =====
        ivProfile = findViewById(R.id.ivProfile);
        ivProfile.setOnClickListener(v -> {
            String email = getSharedPreferences("auth", MODE_PRIVATE).getString("email", null);
            Intent i = new Intent(this, activity_profile.class);
            if (email != null) i.putExtra("email", email);
            startActivity(i);
        });

        // ===== BottomNavigationView =====
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            if (id == R.id.nav_library) {
                startActivity(new Intent(this, LibraryHistoryActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0, 0); finish(); return true;
            }
            return false;
        });

        // ===== ตั้งชื่อหัวข้อ =====
        setSectionText(R.id.rowYouMayLike, "You may also like");
        setSectionText(R.id.rowTopChart,  "Top Chart");
        setSectionText(R.id.rowRomance,   "Romance");
        setSectionText(R.id.rowDrama,     "Drama");
        setSectionText(R.id.rowComedy,    "Comedy");
        setSectionText(R.id.rowFantasy,   "Fantasy");
        setSectionText(R.id.rowScifi,     "Sci-fi");
        setSectionText(R.id.rowMystery,   "Mystery");

        // ===== ตั้ง LayoutManager =====
        setHorizontalList(R.id.rvYouMayLike);
        setVerticalList(R.id.rvTopChart);
        setHorizontalList(R.id.rvRomance);
        setHorizontalList(R.id.rvDrama);
        setHorizontalList(R.id.rvComedy);
        setHorizontalList(R.id.rvFantasy);
        setHorizontalList(R.id.rvScifi);
        setHorizontalList(R.id.rvMystery);

        // ปุ่ม '>' ของ Top Chart
        View rowTop = findViewById(R.id.rowTopChart);
        if (rowTop != null) {
            View arrow = rowTop.findViewById(R.id.btnMore);
            if (arrow != null) {
                arrow.setOnClickListener(v ->
                        startActivity(new Intent(this, TopChartActivity.class)));
            }
        }

        // === Chip หมวด ===
        Chip chipRomance = findViewById(R.id.chipRomance);
        Chip chipDrama   = findViewById(R.id.chipDrama);
        Chip chipComedy  = findViewById(R.id.chipComedy);
        Chip chipFantasy = findViewById(R.id.chipFantasy);
        Chip chipMystery = findViewById(R.id.chipMystery);
        Chip chipScifi   = findViewById(R.id.chipScifi);

        if (chipRomance != null) chipRomance.setOnClickListener(v -> openCategory("Romance"));
        if (chipDrama   != null) chipDrama.setOnClickListener(v   -> openCategory("Drama"));
        if (chipComedy  != null) chipComedy.setOnClickListener(v  -> openCategory("Comedy"));
        if (chipFantasy != null) chipFantasy.setOnClickListener(v -> openCategory("Fantasy"));
        if (chipMystery != null) chipMystery.setOnClickListener(v -> openCategory("Mystery"));
        if (chipScifi   != null) chipScifi.setOnClickListener(v   -> openCategory("Sci-fi"));

        // ===== โหลดข้อมูลรายการต่าง ๆ =====
        RecyclerView rvYouMayLike = findViewById(R.id.rvYouMayLike);
        rvYouMayLike.setAdapter(new CoverSquareAdapter(
                db.getRecentWritings(12), this::openWritingDetail));

        rvTopChart = findViewById(R.id.rvTopChart);
        topChartAdapter = new TopChartRowAdapter(
                db.getTopWritingsByLikes(3), this::openWritingDetail, true);
        rvTopChart.setAdapter(topChartAdapter);

        RecyclerView rvRomance = findViewById(R.id.rvRomance);
        rvRomance.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("romance", 12), this::openWritingDetail));
        RecyclerView rvDrama = findViewById(R.id.rvDrama);
        rvDrama.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("drama", 12), this::openWritingDetail));
        RecyclerView rvComedy = findViewById(R.id.rvComedy);
        rvComedy.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("comedy", 12), this::openWritingDetail));
        RecyclerView rvFantasy = findViewById(R.id.rvFantasy);
        rvFantasy.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("fantasy", 12), this::openWritingDetail));
        RecyclerView rvScifi = findViewById(R.id.rvScifi);
        rvScifi.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("sci-fi", 12), this::openWritingDetail));
        RecyclerView rvMystery = findViewById(R.id.rvMystery);
        rvMystery.setAdapter(new CoverSquareAdapter(
                db.getWritingItemsByTag("mystery", 12), this::openWritingDetail));

        // ===== โหลด Banner ครั้งแรก =====
        loadBanners();
    }

    /** โหลด/รีเฟรชแบนเนอร์ */
    private void loadBanners() {
        if (bannerPager == null) return;
        List<DBHelper.Banner> banners = db.getActiveBanners(); // ใช้เมธอดที่คุณมีจริง
        if (banners != null && !banners.isEmpty()) {
            bannerPager.setAdapter(new BannerPagerAdapter(this, banners));
            if (bannerPlaceholder != null) bannerPlaceholder.setVisibility(View.GONE);
        } else {
            if (bannerPlaceholder != null) bannerPlaceholder.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                writingChangedReceiver,
                new IntentFilter(EventCenter.ACTION_WRITING_CHANGED)
        );
        refreshTop3();
        loadProfileAvatar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // เผื่อเพิ่ม/ลบ banner จากหน้าแอดมิน กลับมาหน้านี้จะรีเฟรชให้
        loadBanners();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(writingChangedReceiver);
    }

    // ---------- helpers ----------
    private void refreshTop3() {
        if (topChartAdapter == null) return;
        topChartAdapter.replaceData(new DBHelper(this).getTopWritingsByLikes(3));
    }

    private void setSectionText(int includeId, String title) {
        View row = findViewById(includeId);
        if (row == null) return;
        TextView tv = row.findViewById(R.id.tvSectionTitle);
        if (tv != null) tv.setText(title);
    }

    private void setHorizontalList(int recyclerId) {
        RecyclerView rv = findViewById(recyclerId);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            rv.setHasFixedSize(true);
        }
    }

    private void setVerticalList(int recyclerId) {
        RecyclerView rv = findViewById(recyclerId);
        if (rv != null) {
            rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            rv.setHasFixedSize(false);
            rv.setNestedScrollingEnabled(false);
        }
    }

    private void openWritingDetail(WritingItem item) {
        if (item == null) return;
        Intent i = new Intent(this, ReadingMainActivity.class);
        i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, item.getId());
        startActivity(i);
    }

    private void openCategory(String category) {
        if (category == null || category.trim().isEmpty()) return;
        Intent i = new Intent(this, CategoryListActivity.class);
        i.putExtra(CategoryListActivity.EXTRA_CATEGORY, category);
        startActivity(i);
    }

    private void loadProfileAvatar() {
        ImageView avatar = ivProfile;
        if (avatar == null) return;

        String email = db.getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) {
            email = getSharedPreferences("auth", MODE_PRIVATE).getString("email", null);
        }
        String uriStr = (email != null) ? db.getUserImageUri(email) : null;

        if (uriStr != null && !uriStr.trim().isEmpty()) {
            try { avatar.setImageURI(Uri.parse(uriStr)); }
            catch (Exception e) { avatar.setImageResource(R.drawable.user_circle_svgrepo_com); }
        } else {
            avatar.setImageResource(R.drawable.user_circle_svgrepo_com);
        }
    }
}
