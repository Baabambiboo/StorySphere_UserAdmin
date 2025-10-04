package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ReadingMainActivity extends AppCompatActivity {

    public static final String EXTRA_WRITING_ID = "writing_id";
    public static final String EXTRA_EPISODE_ID = "episode_id";
    public static final String ACTION_WRITING_CHANGED = "storysphere.ACTION_WRITING_CHANGED";

    private DBHelper db;
    private int writingId;
    private RecyclerView rvEpisodes;
    private boolean statsTouched = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_main);

        db = new DBHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) getSupportActionBar().setDisplayShowTitleEnabled(false);
            toolbar.setNavigationIcon(R.drawable.round_arrow_back_ios_24);
            toolbar.setNavigationOnClickListener(v -> goBack());
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() { goBack(); }
        });

        int fromExtra1 = getIntent().getIntExtra("writing_id", -1);
        int fromExtra2 = getIntent().getIntExtra(EXTRA_WRITING_ID, -1);
        writingId = (fromExtra2 > 0) ? fromExtra2 : fromExtra1;
        if (writingId <= 0) writingId = pickLatestWritingId();

        bindStoryHeader(writingId);
        setupSocialBar(writingId);

        rvEpisodes = findViewById(R.id.recyclerViewEpisodes);
        rvEpisodes.setLayoutManager(new LinearLayoutManager(this));
        rvEpisodes.setHasFixedSize(false);
        List<Episode> eps = db.getEpisodesByWritingId(writingId);
        EpisodeReadingAdapter adp = new EpisodeReadingAdapter(eps, ep -> {
            Intent it = new Intent(this, activity_episode.class);
            it.putExtra(EXTRA_WRITING_ID, ep.writingId);
            it.putExtra(EXTRA_EPISODE_ID, ep.episodeId);
            startActivity(it);
        });
        rvEpisodes.setAdapter(adp);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (id == R.id.nav_library) {
                startActivity(new Intent(this, LibraryHistoryActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (id == R.id.nav_writing) {
                startActivity(new Intent(this, activity_writing.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            } else if (id == R.id.nav_activity) {
                startActivity(new Intent(this, UserActivity.class));
                overridePendingTransition(0, 0);
                finish(); return true;
            }
            return false;
        });

        // เพิ่มวิวครั้งแรกเมื่อเข้าหน้านี้ แล้วแจ้งรีลไทม์ให้ฝั่งซ้าย
        int newViews = db.addViewOnce(writingId);
        TextView tvEye = findViewById(R.id.tvEye);
        if (tvEye != null) tvEye.setText(String.valueOf(Math.max(0, newViews)));
        statsTouched = true;
        broadcastChange(); // <<< แจ้งไปฝั่งซ้าย
    }

    private int pickLatestWritingId() {
        try (Cursor c = db.getReadableDatabase()
                .rawQuery("SELECT id FROM " + DBHelper.TABLE_WRITINGS + " ORDER BY id DESC LIMIT 1", null)) {
            if (c.moveToFirst()) return c.getInt(0);
        }
        return -1;
    }

    private void bindStoryHeader(int writingId) {
        String title = null, tagline = null, tagsCsv = null, authorEmail = null, imagePath = null;

        Cursor cur = null;
        try {
            cur = db.getWritingById(writingId);
            if (cur != null && cur.moveToFirst()) {
                int idxTitle   = cur.getColumnIndex("title");
                int idxTagline = cur.getColumnIndex("tagline");
                int idxTags    = cur.getColumnIndex("tag");
                int idxAuthor  = cur.getColumnIndex("author_email");
                int idxImage   = cur.getColumnIndex("image_path");

                if (idxTitle   >= 0) title       = cur.getString(idxTitle);
                if (idxTagline >= 0) tagline     = cur.getString(idxTagline);
                if (idxTags    >= 0) tagsCsv     = cur.getString(idxTags);
                if (idxAuthor  >= 0) authorEmail = cur.getString(idxAuthor);
                if (idxImage   >= 0) imagePath   = cur.getString(idxImage);
            }
        } finally { if (cur != null) cur.close(); }

        // ใช้การ์ดเป็น scope หา view
        View card = findViewById(R.id.storyCard);

        // Title / Tagline
        if (card != null) {
            TextView tvTitle   = card.findViewById(R.id.textTitle);
            TextView tvTagline = card.findViewById(R.id.textBlurb);
            if (tvTitle   != null && title   != null) tvTitle.setText(title);
            if (tvTagline != null && tagline != null) tvTagline.setText(tagline);
        }

        // รูปปก: ImageView ภายใน item_story_reading (id = imageView)
        if (card != null) {
            ImageView cover = card.findViewById(R.id.imageView);
            if (cover != null) {
                try {
                    if (imagePath != null && !imagePath.trim().isEmpty()) {
                        if (imagePath.startsWith("content://")) {
                            cover.setImageURI(Uri.parse(imagePath));
                        } else {
                            Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                            if (bmp != null) {
                                cover.setImageBitmap(bmp);
                            } else {
                                cover.setImageResource(R.drawable.ic_launcher_foreground);
                            }
                        }
                    } else {
                        cover.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } catch (Exception e) {
                    cover.setImageResource(R.drawable.ic_launcher_foreground);
                }
            }
        }

        // ผู้เขียน
        if (card != null) {
            TextView tvAuthor = card.findViewById(R.id.tvAuthorRight);
            if (tvAuthor != null) {
                String authorName = null;

                if (authorEmail != null && !authorEmail.trim().isEmpty()) {
                    authorName = db.getUsernameByEmail(authorEmail);
                    if (authorName == null || authorName.trim().isEmpty()) {
                        int at = authorEmail.indexOf('@');
                        authorName = (at > 0) ? authorEmail.substring(0, at) : authorEmail;
                    }

                    final String passEmail = authorEmail;
                    tvAuthor.setOnClickListener(v -> {
                        Intent it = new Intent(this, activity_follow_writer.class);
                        it.putExtra(activity_follow_writer.EXTRA_EMAIL, passEmail);
                        startActivity(it);
                    });
                }

                if (authorName == null || authorName.trim().isEmpty()) authorName = "Author";
                tvAuthor.setText(authorName);
            }
        }

        // แท็ก: ใช้ container ที่อยู่นอกการ์ด (activity_reading_main.xml)
        LinearLayout tagContainer = findViewById(R.id.tagContainer);
        if (tagContainer != null) {
            tagContainer.removeAllViews();
            if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
                for (String t : tagsCsv.split(",")) {
                    TextView chip = buildPurpleTag(t.trim());
                    LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    clp.setMarginEnd(dp(8));
                    chip.setLayoutParams(clp);
                    tagContainer.addView(chip);
                }
            }
        }
    }

    private void setupSocialBar(int writingId) {
        // <<< ประกาศ view ให้ครบ >>>
        ImageView ivBookmark = findViewById(R.id.ivBookmark);
        TextView  tvBookmark = findViewById(R.id.tvBookmark);
        ImageView ivHeart    = findViewById(R.id.ivHeart);
        TextView  tvHeart    = findViewById(R.id.tvHeart);
        TextView  tvEye      = findViewById(R.id.tvEye);

        final String logged = db.getLoggedInUserEmail();
        final String userEmail = (logged == null || logged.trim().isEmpty()) ? "guest" : logged.trim();

        // ----- BOOKMARK -----
        int bCount = Math.max(0, db.countBookmarks(writingId));
        if (tvBookmark != null) tvBookmark.setText(String.valueOf(bCount));
        boolean isBookmarked = db.isBookmarked(userEmail, writingId);
        if (ivBookmark != null) {
            ivBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_outline);
            ivBookmark.setOnClickListener(v -> {
                boolean now = db.isBookmarked(userEmail, writingId);
                boolean ok  = db.setBookmark(userEmail, writingId, !now);
                if (ok) {
                    int c = Math.max(0, db.countBookmarks(writingId));
                    if (tvBookmark != null) tvBookmark.setText(String.valueOf(c));
                    ivBookmark.setImageResource(!now ? R.drawable.ic_bookmark_filled
                            : R.drawable.ic_bookmark_outline);
                    statsTouched = true;
                    broadcastChange();
                }
            });
        }

        // ----- LIKE (บันทึกต่อผู้ใช้) -----
        if (ivHeart != null && tvHeart != null) {
            int likes = Math.max(0, db.getLikes(writingId));
            tvHeart.setText(String.valueOf(likes));

            boolean liked = db.isUserLiked(userEmail, writingId);
            ivHeart.setImageResource(liked ? R.drawable.ic_heart_filled
                    : R.drawable.heart_svgrepo_com);

            ivHeart.setOnClickListener(v -> {
                boolean nowLiked = db.isUserLiked(userEmail, writingId);
                // toggle แล้วอัปเดตทั้ง LOG และ COUNTER
                boolean ok = db.setUserLike(userEmail, writingId, !nowLiked);
                if (ok) {
                    int newLikes = Math.max(0, db.getLikes(writingId));
                    tvHeart.setText(String.valueOf(newLikes));
                    ivHeart.setImageResource(!nowLiked ? R.drawable.ic_heart_filled
                            : R.drawable.heart_svgrepo_com);
                    statsTouched = true;
                    broadcastChange();
                }
            });
        }


        if (tvEye != null) {
            tvEye.setText(String.valueOf(Math.max(0, db.getViews(writingId))));
        }
    }

    private void goBack() {
        if (statsTouched) {
            EventCenter.notifyChanged(this, writingId, "return");
            broadcastChange(); // <<< กันเหนียวเวลาออกจากหน้านี้
        }
        finish();
    }

    @SuppressLint("MissingSuperCall")
    @Override public void onBackPressed() { goBack(); }

    private TextView buildPurpleTag(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setIncludeFontPadding(false);
        tv.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        android.graphics.drawable.GradientDrawable bg =
                new android.graphics.drawable.GradientDrawable();
        bg.setColor(ContextCompat.getColor(this, R.color.purple1));
        bg.setCornerRadius(dp(20));
        tv.setBackground(bg);
        tv.setPadding(dp(12), dp(6), dp(12), dp(6));
        return tv;
    }

    private int dp(int v) { return Math.round(getResources().getDisplayMetrics().density * v); }

    // ---------- เพิ่มเมธอดบรอดแคสต์ให้หน้าอื่นอัปเดตเรียลไทม์ ----------
    private void broadcastChange() {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(
                        new Intent(ACTION_WRITING_CHANGED)
                                .putExtra(EXTRA_WRITING_ID, writingId)
                );
    }
}
