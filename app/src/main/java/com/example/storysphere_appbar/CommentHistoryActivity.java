package com.example.storysphere_appbar;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;
import android.database.Cursor;

public class CommentHistoryActivity extends AppCompatActivity {

    private DBHelper db;
    private String userEmail;
    private String displayName;
    private LinearLayout listContainer;
    private final SimpleDateFormat fmt = new SimpleDateFormat("d MMM yyyy", new Locale("th","TH"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_history);

        db = new DBHelper(this);
        listContainer = findViewById(R.id.listContainer);

        userEmail   = getIntent().getStringExtra("user_email");
        displayName = getIntent().getStringExtra("display_name");

        TextView title = findViewById(R.id.title);
        title.setText("ประวัติคอมเมนต์ 💬  •  " + (displayName==null? "" : displayName));

        loadComments();
    }

    private void loadComments() {
        listContainer.removeAllViews();

        String sql =
                "SELECT w.title, c.content, c.created_at " +
                        "FROM " + DBHelper.TABLE_COMMENTS + " c " +
                        "JOIN " + DBHelper.TABLE_WRITINGS + " w ON w.id=c.writing_id " +
                        "WHERE c.user_email=? " +
                        "ORDER BY c.created_at DESC";

        try (Cursor c = db.getReadableDatabase().rawQuery(sql, new String[]{ userEmail })) {
            if (!c.moveToFirst()) {
                addEmpty("ยังไม่มีประวัติการคอมเมนต์");
                return;
            }
            do {
                String wTitle = c.getString(0);
                String text   = c.getString(1);
                long   when   = c.getLong(2);
                addCommentRow(wTitle, text, when);
            } while (c.moveToNext());
        }
    }

    private void addCommentRow(String bookTitle, String comment, long ts) {
        TextView tv = new TextView(this);
        tv.setText("เรื่อง: " + (bookTitle==null? "-" : bookTitle) +
                "\nวันที่คอมเมนต์: " + fmt.format(new java.util.Date(ts)) +
                "\nข้อความ: " + (comment==null? "-" : comment));
        tv.setPadding(24,20,24,20);
        tv.setBackgroundResource(R.drawable.rounded_purple_bg_admin);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16,12,16,0);
        listContainer.addView(tv, lp);
    }

    private void addEmpty(String msg) {
        TextView tv = new TextView(this);
        tv.setText(msg);
        tv.setPadding(24,20,24,20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(16,24,16,0);
        listContainer.addView(tv, lp);
    }
}
