package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class BookDetailActivity extends AppCompatActivity {

    private TextView txtUserId, txtStatusBook, txtTitleBook, txtPenName;
    private TextView DateWritten, txtAllEpisodes, textView27;
    private TextView txtReaderCount, txtCommentCount, txtFavCount;
    private ImageView img_book_cover, imgHome;

    private DBHelper db;
    private int writingId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        db = new DBHelper(this);

        // ---- Toolbar back ----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                // กลับไป BooksListActivity เสมอ
                Intent i = new Intent(BookDetailActivity.this, BooksListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }

        // ---- Views ----
        txtUserId       = findViewById(R.id.txtUserId);
        txtStatusBook   = findViewById(R.id.txtStatusBook);
        txtTitleBook    = findViewById(R.id.txtTitleBook);
        txtPenName      = findViewById(R.id.txtPenName);
        DateWritten     = findViewById(R.id.DateWritten);
        txtAllEpisodes  = findViewById(R.id.txtAllEpisodes);
        textView27      = findViewById(R.id.textView27);
        txtReaderCount  = findViewById(R.id.txtReaderCount);
        txtCommentCount = findViewById(R.id.txtCommentCount);
        txtFavCount     = findViewById(R.id.txtFavCount);
        img_book_cover  = findViewById(R.id.img_book_cover);
        imgHome         = findViewById(R.id.imgHome);

        // ---- รูปบ้าน: ไปหน้าแอดมิน ----
        if (imgHome != null) {
            imgHome.setOnClickListener(v -> {
                Intent i = new Intent(BookDetailActivity.this, AdminPanelActivity.class);
                // ถ้าอยากล้างสแต็กให้กลับไป admin อย่างเดียว
                // i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // ---- รับ id ----
        writingId = getIntent().getIntExtra("book_id", -1);

        if (writingId > 0) {
            bindWritingBasics(writingId);
            bindStats(writingId);
            bindPenName(writingId);
        } else {
            txtTitleBook.setText("-");
            txtPenName.setText("-");
            txtReaderCount.setText("0");
            txtCommentCount.setText("0");
            txtFavCount.setText("0");
        }

        android.widget.ImageView homeBtn = findViewById(R.id.imgHome);
        if (homeBtn != null) {
            homeBtn.setOnClickListener(v -> {
                Intent i = new Intent(BookDetailActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }
    }

    private void bindWritingBasics(int id) {
        Cursor c = db.getWritingById(id);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String title    = safe(c, "title");
                    String tag      = safe(c, "tag");
                    String imageUri = safe(c, "image_path");

                    String code = String.format(java.util.Locale.ROOT, "BST%02d", Math.max(0, id));
                    txtUserId.setText(code);
                    txtTitleBook.setText(title != null && !title.trim().isEmpty() ? title : "-");
                    txtStatusBook.setText("Ongoing");
                    if (textView27 != null) textView27.setText(tag == null || tag.trim().isEmpty() ? "-" : tag);

                    // TODO: โหลดภาพด้วย Glide/Picasso ถ้ามี
                    // Glide.with(this).load(imageUri).into(img_book_cover);
                }
            } finally {
                c.close();
            }
        }
    }

    private void bindStats(int id) {
        int views     = db.getViews(id);         // ผู้อ่าน
        int bookmarks = db.countBookmarks(id);   // ใช้แทนคอมเมนต์ชั่วคราว
        int likes     = db.getLikes(id);         // กดใจ

        txtReaderCount.setText(formatCount(views));
        txtCommentCount.setText(formatCount(bookmarks));
        txtFavCount.setText(formatCount(likes));
    }

    private void bindPenName(int id) {
        String pen = db.getAuthorNameForWritingId(id);
        txtPenName.setText(pen == null || pen.trim().isEmpty() ? "-" : pen);
    }

    private static String safe(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        return i >= 0 ? c.getString(i) : null;
    }

    private static String formatCount(int n) {
        if (n < 1000) return String.valueOf(n);
        if (n < 10000) return (n / 1000) + "." + ((n % 1000) / 100) + "K";
        if (n < 1000000) return (n / 1000) + "K";
        return (n / 1000000) + "M";
    }
}
