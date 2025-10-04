package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Writing_Add_Episode3 extends AppCompatActivity {

    private static final int REQ_ADD_EDIT_EPISODE = 1001;

    private int writingId = -1;
    private TextView bookTitle, bookTagline;
    private DBHelper dbHelper;

    private RecyclerView recyclerView;
    private EpisodeAdapter episodeAdapter;

    // ปก (ImageView อยู่ใน include item_story มี id = imageView)
    private ImageView cover;

    // === รับบรอดแคสต์จากหน้าขวา ให้รีเฟรชสถิติแบบเรียลไทม์ ===
    private final BroadcastReceiver writingChangeReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (intent == null) return;
            if (!ReadingMainActivity.ACTION_WRITING_CHANGED.equals(intent.getAction())) return;

            int changedId = intent.getIntExtra(ReadingMainActivity.EXTRA_WRITING_ID, -1);
            if (changedId == writingId) {
                // รีเฟรชตัวเลข bookmark/heart/view ให้ทันที
                setupSocialBar(writingId);
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_writing_add_episode3);

        new DBHelper(this).ensureEpisodesTable();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        bookTitle   = findViewById(R.id.bookTitle);
        bookTagline = findViewById(R.id.bookTagline);
        // ❗️ปกที่ถูกต้องในการ์ดคือ R.id.imageView (ไม่ใช่ imgHome)
        cover       = findViewById(R.id.imageView);

        dbHelper = new DBHelper(this);
        dbHelper.ensureEpisodesTable();

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("writing_id")) {
            writingId = intent.getIntExtra("writing_id", -1);
        }

        findViewById(R.id.btnAddEpisode).setOnClickListener(v -> {
            Intent add = new Intent(this, AddNovelActivity.class);
            add.putExtra("mode", "create");
            add.putExtra("writing_id", writingId);
            startActivityForResult(add, REQ_ADD_EDIT_EPISODE);
        });

        recyclerView = findViewById(R.id.recyclerViewEpisodes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        episodeAdapter = new EpisodeAdapter(new ArrayList<>(), this::onEditEpisodeClick);
        recyclerView.setAdapter(episodeAdapter);

        bookTitle.setOnClickListener(v -> {
            Intent editIntent = new Intent(Writing_Add_Episode3.this, activity_writnig_edit_writting.class);
            editIntent.putExtra("writing_id", writingId);
            startActivityForResult(editIntent, 1);
        });

        TextView deleteButton = findViewById(R.id.textView4);
        deleteButton.setOnClickListener(v -> {
            Intent it = new Intent(Writing_Add_Episode3.this, DeleteWritingActivity.class);
            it.putExtra("writing_id", writingId);
            startActivity(it);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ลงทะเบียนฟังการเปลี่ยนแปลงจากหน้าอ่าน
        LocalBroadcastManager.getInstance(this).registerReceiver(
                writingChangeReceiver,
                new IntentFilter(ReadingMainActivity.ACTION_WRITING_CHANGED)
        );

        loadWritingData();               // ชื่อ/คำโปรย/ปก/แท็ก
        setupSocialBar(writingId);       // สถิติ + คลิกได้
        dbHelper.backfillEpisodeNumbersIfNeeded();
        loadEpisodes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // ยกเลิกเพื่อกัน memory leak
        LocalBroadcastManager.getInstance(this).unregisterReceiver(writingChangeReceiver);
    }

    private void loadEpisodes() {
        List<Episode> list = dbHelper.getEpisodesByWritingId(writingId);
        if (episodeAdapter != null) episodeAdapter.submit(list);
    }

    private void onEditEpisodeClick(Episode ep) {
        Intent edit = new Intent(this, AddNovelActivity.class);
        edit.putExtra("mode", "edit");
        edit.putExtra("episode_id", ep.episodeId);
        startActivityForResult(edit, REQ_ADD_EDIT_EPISODE);
    }

    // โหลด title, tagline, image, tag จาก DB (อัปเดตการ์ด)
    private void loadWritingData() {
        Cursor cursor = dbHelper.getWritingById(writingId);
        if (cursor != null && cursor.moveToFirst()) {
            String title     = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String tagline   = cursor.getString(cursor.getColumnIndexOrThrow("tagline"));
            String tagString = cursor.getString(cursor.getColumnIndexOrThrow("tag"));
            int idxImg       = cursor.getColumnIndex("image_path");
            String imagePath = (idxImg >= 0 ? cursor.getString(idxImg) : null);

            // ปก
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

            // ข้อความ
            bookTitle.setText(title);
            bookTagline.setText(tagline);

            // แท็ก
            if (tagString != null && !tagString.trim().isEmpty()) {
                List<String> tagList = Arrays.asList(tagString.split(","));
                showTags(tagList);
            } else {
                showTags(new ArrayList<>());
            }

            cursor.close();
        }
    }

    // ทำให้การ์ดฝั่งซ้าย “ขึ้นเรียลไทม์” เหมือนฝั่งขวา
    private void setupSocialBar(int writingId) {
        ImageView ivBookmark = findViewById(R.id.ivBookmark);
        TextView  tvBookmark = findViewById(R.id.tvBookmark);
        ImageView ivHeart    = findViewById(R.id.ivHeart);
        TextView  tvHeart    = findViewById(R.id.tvHeart);
        TextView  tvEye      = findViewById(R.id.tvEye);

        final String logged = dbHelper.getLoggedInUserEmail();
        final String userEmail = (logged == null || logged.trim().isEmpty()) ? "guest" : logged.trim();

        // Bookmark
        int bCount = Math.max(0, dbHelper.countBookmarks(writingId));
        if (tvBookmark != null) tvBookmark.setText(String.valueOf(bCount));
        boolean isBookmarked = dbHelper.isBookmarked(userEmail, writingId);
        if (ivBookmark != null) {
            ivBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled
                    : R.drawable.ic_bookmark_outline);
            ivBookmark.setOnClickListener(v -> {
                boolean now = dbHelper.isBookmarked(userEmail, writingId);
                boolean ok  = dbHelper.setBookmark(userEmail, writingId, !now);
                if (ok) {
                    int c = Math.max(0, dbHelper.countBookmarks(writingId));
                    if (tvBookmark != null) tvBookmark.setText(String.valueOf(c));
                    ivBookmark.setImageResource(!now ? R.drawable.ic_bookmark_filled
                            : R.drawable.ic_bookmark_outline);
                }
            });
        }

        // Like
        if (ivHeart != null && tvHeart != null) {
            // ค่าเริ่มต้น
            int likes = Math.max(0, dbHelper.getLikes(writingId));
            tvHeart.setText(String.valueOf(likes));

            boolean liked = dbHelper.isUserLiked(userEmail, writingId);
            ivHeart.setImageResource(liked ? R.drawable.ic_heart_filled
                    : R.drawable.heart_svgrepo_com);

            ivHeart.setOnClickListener(v -> {
                boolean nowLiked = dbHelper.isUserLiked(userEmail, writingId);
                boolean ok = dbHelper.setUserLike(userEmail, writingId, !nowLiked);
                if (ok) {
                    int newLikes = Math.max(0, dbHelper.getLikes(writingId));
                    tvHeart.setText(String.valueOf(newLikes));
                    ivHeart.setImageResource(!nowLiked ? R.drawable.ic_heart_filled
                            : R.drawable.heart_svgrepo_com);

                    // แจ้งหน้าอื่นให้รีเฟรช (Home/Reading page ฯลฯ)
                    LocalBroadcastManager.getInstance(this).sendBroadcast(
                            new Intent(ReadingMainActivity.ACTION_WRITING_CHANGED)
                                    .putExtra(ReadingMainActivity.EXTRA_WRITING_ID, writingId)
                    );
                }
            });
        }

        // Views
        if (tvEye != null) {
            tvEye.setText(String.valueOf(Math.max(0, dbHelper.getViews(writingId))));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            loadWritingData();
            setupSocialBar(writingId);
        }
        if (requestCode == REQ_ADD_EDIT_EPISODE && resultCode == RESULT_OK) {
            loadEpisodes();
            setupSocialBar(writingId);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(this, activity_writing.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
        return true;
    }

    private void showTags(List<String> tags) {
        LinearLayout tagContainer = findViewById(R.id.tagContainer);
        tagContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        for (String tag : tags) {
            View tagView = inflater.inflate(R.layout.item_tag, tagContainer, false);
            TextView tvTagName = tagView.findViewById(R.id.tagName);
            tvTagName.setText(tag);
            tagContainer.addView(tagView);
        }
    }
}
