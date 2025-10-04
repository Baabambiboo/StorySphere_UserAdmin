package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BookDetailActivity extends AppCompatActivity {

    // ===== Views ตรงกับ layout ที่ให้มา =====
    private TextView txtUserId, txtStatusBook, txtTitleBook, txtPenName;
    private TextView DateWritten, txtAllEpisodes, textView27;      // tag = textView27
    private TextView txtReaderCount, txtCommentCount, txtFavCount;
    private ImageView img_book_cover, imgHome;

    private DBHelper db;
    private int writingId = -1;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("d/M/yy", new Locale("th","TH"));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        db = new DBHelper(this);

        // ---- Toolbar back ไปหน้า BooksList เสมอ ----
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                Intent i = new Intent(BookDetailActivity.this, BooksListActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }

        // ---- Bind Views ----
        txtUserId       = findViewById(R.id.txtUserId);
        txtStatusBook   = findViewById(R.id.txtStatusBook);
        txtTitleBook    = findViewById(R.id.txtTitleBook);
        txtPenName      = findViewById(R.id.txtPenName);
        DateWritten     = findViewById(R.id.DateWritten);
        txtAllEpisodes  = findViewById(R.id.txtAllEpisodes);
        textView27      = findViewById(R.id.textView27);     // Tag
        txtReaderCount  = findViewById(R.id.txtReaderCount);
        txtCommentCount = findViewById(R.id.txtCommentCount);
        txtFavCount     = findViewById(R.id.txtFavCount);
        img_book_cover  = findViewById(R.id.img_book_cover);
        imgHome         = findViewById(R.id.imgHome);

        if (imgHome != null) {
            imgHome.setOnClickListener(v -> startActivity(new Intent(this, AdminPanelActivity.class)));
        }

        // ---- รับ writingId ----
        writingId = getIntent().getIntExtra("book_id", -1);

        // ---- แสดงผล ----
        if (writingId > 0) {
            bindWritingBasics(writingId);  // Title / Pen / Dates / Episodes / Tag / Cover / Status
            bindStats(writingId);          // Readers / Comments / Favorites
        } else {
            // fallback
            txtUserId.setText("-");
            txtStatusBook.setText("-");
            txtTitleBook.setText("-");
            txtPenName.setText("-");
            DateWritten.setText("-");
            txtAllEpisodes.setText("-");
            textView27.setText("-");
            txtReaderCount.setText("0");
            txtCommentCount.setText("0");
            txtFavCount.setText("0");
        }
    }

    // ============= ดึงข้อมูลหลักของนิยาย =============
    private void bindWritingBasics(int id) {
        Cursor c = db.getWritingById(id);   // ต้องคืน cursor มีคอลัมน์: title, tag, image_path, author_email, created_at, completed_at(optional), status(optional)
        if (c == null) return;

        try {
            if (!c.moveToFirst()) return;

            String title       = getStr(c, "title");
            String tag         = getStr(c, "tag");
            String imagePath   = getStr(c, "image_path");
            String authorEmail = getStr(c, "author_email");
            Long   createdAt   = getLongOrNull(c, "created_at");    // epoch millis ถ้ามี
            Long   completedAt = getLongOrNull(c, "completed_at");  // epoch millis ถ้ามี
            String status      = getStr(c, "status");               // "Ongoing"/"Complete" ถ้ามี

            // Code BSTxx
            String code = String.format(Locale.ROOT, "BST%02d", Math.max(0, id));
            txtUserId.setText(code);

            // Title
            txtTitleBook.setText(empty(title) ? "-" : title);

            // Pen name (displayName ถ้ามี, ไม่ก็ username, fallback เป็น email local-part)
            String pen = db.getAuthorNameForWritingId(id);
            if (empty(pen) && !empty(authorEmail)) {
                pen = db.getUsernameByEmail(authorEmail);
                if (empty(pen)) {
                    int at = authorEmail.indexOf('@');
                    pen = at > 0 ? authorEmail.substring(0, at) : authorEmail;
                }
            }
            txtPenName.setText(empty(pen) ? "-" : pen);

            // Date written
            if (createdAt != null && createdAt > 0) {
                DateWritten.setText(dateFmt.format(new Date(createdAt)));
            } else {
                // ไม่มีคอลัมน์ created_at → แสดง "-"
                DateWritten.setText("-");
            }

            // Date completed (TextView id = textView23 ใน layout)
            TextView tvCompleted = findViewById(R.id.textView23);
            if (tvCompleted != null) {
                if (completedAt != null && completedAt > 0) {
                    tvCompleted.setText(dateFmt.format(new Date(completedAt)));
                } else {
                    tvCompleted.setText("-"); // ยังไม่จบ
                }
            }

            // All episode
            int epCount = db.countEpisodesByWritingId(id);   // ต้องมีเมธอดนี้ใน DBHelper
            txtAllEpisodes.setText(epCount + " Episode");

            // Tag
            textView27.setText(empty(tag) ? "-" : tag);

            // สถานะ
            if (!empty(status)) {
                txtStatusBook.setText(status);
            } else {
                // เดาว่าจบเมื่อมี completedAt
                txtStatusBook.setText(completedAt != null && completedAt > 0 ? "Complete" : "Ongoing");
            }

            // Cover
            if (!empty(imagePath)) {
                try {
                    if (imagePath.startsWith("content://")) {
                        img_book_cover.setImageURI(Uri.parse(imagePath));
                    } else {
                        Bitmap bmp = BitmapFactory.decodeFile(imagePath);
                        if (bmp != null) img_book_cover.setImageBitmap(bmp);
                        else img_book_cover.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } catch (Exception e) {
                    img_book_cover.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                img_book_cover.setImageResource(R.drawable.ic_launcher_foreground);
            }

        } finally {
            c.close();
        }
    }

    // ============= ตัวเลขสถิติ =============
    private void bindStats(int id) {
        // Readers = views
        int readers = Math.max(0, db.getViews(id));

        // Comments
        int comments;
        try {
            comments = Math.max(0, db.countCommentsForWriting(id));
        } catch (Throwable ignore) {
            comments = Math.max(0, db.countBookmarks(id));
        }

        // Favorites = likes
        int favorites = Math.max(0, db.getLikes(id));

        txtReaderCount.setText(formatCount(readers));
        txtCommentCount.setText(formatCount(comments));
        txtFavCount.setText(formatCount(favorites));
    }

    // ===== Utils =====
    private static String getStr(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        return i >= 0 ? c.getString(i) : null;
    }

    private static Long getLongOrNull(Cursor c, String col) {
        int i = c.getColumnIndex(col);
        if (i < 0 || c.isNull(i)) return null;
        try { return c.getLong(i); } catch (Exception e) { return null; }
    }

    private static boolean empty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String formatCount(int n) {
        if (n < 1000) return String.valueOf(n);
        if (n < 10000) return (n / 1000) + "." + ((n % 1000) / 100) + "K";
        if (n < 1000000) return (n / 1000) + "K";
        return (n / 1000000) + "M";
    }
}
