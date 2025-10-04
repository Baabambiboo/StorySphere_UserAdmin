package com.example.storysphere_appbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserDetailActivity extends AppCompatActivity {

    private DBHelper db;

    private TextView txtUserId, txtUserName, txtDTDisplayName, txtEmailUser, txtStatusBook;
    private ImageView imgHome, imgUserDetails;

    // inline sections
    private LinearLayout likesSection, commentsSection;
    private boolean likesLoaded = false, commentsLoaded = false;

    // keep for queries
    private int userId = -1;
    private String userEmail = null;
    private String displayName = null;

    private final SimpleDateFormat dateFmt = new SimpleDateFormat("d MMM yyyy", new Locale("th","TH"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        db = new DBHelper(this);

        // toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }

        // bind views
        txtUserId        = findViewById(R.id.txtUserId);
        txtUserName      = findViewById(R.id.txtUserName);
        txtDTDisplayName = findViewById(R.id.txtDTDisplayName);
        txtEmailUser     = findViewById(R.id.txtEmailUser);
        txtStatusBook    = findViewById(R.id.txtStatusBook);
        imgHome          = findViewById(R.id.imgHome);
        imgUserDetails   = findViewById(R.id.img_User_Details);

        likesSection     = findViewById(R.id.likesSection);
        commentsSection  = findViewById(R.id.commentsSection);

        // home → AdminPanel
        if (imgHome != null) {
            imgHome.setOnClickListener(v -> {
                Intent i = new Intent(UserDetailActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // รับ user_id & bind
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) bindUser(userId);
        else finish();

        // ----- Inline “Liked History” -----
        View likeCard = findViewById(R.id.cardView);
        if (likeCard != null) {
            likeCard.setOnClickListener(v -> {
                if (likesSection.getVisibility() == View.VISIBLE) {
                    likesSection.setVisibility(View.GONE);
                } else {
                    if (!likesLoaded) {
                        populateLikes();
                        likesLoaded = true;
                    }
                    likesSection.setVisibility(View.VISIBLE);
                }
            });
        }

        // ----- Inline “Comment History” -----
        View commentCard = findViewById(R.id.cardviewComment);
        if (commentCard != null) {
            commentCard.setOnClickListener(v -> {
                if (commentsSection.getVisibility() == View.VISIBLE) {
                    commentsSection.setVisibility(View.GONE);
                } else {
                    if (!commentsLoaded) {
                        populateComments();
                        commentsLoaded = true;
                    }
                    commentsSection.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void bindUser(int uid) {
        User u = db.getUserByIdInt(uid);
        if (u == null) { finish(); return; }

        userEmail   = u.email;
        displayName = (u.displayName != null && !u.displayName.trim().isEmpty())
                ? u.displayName : u.username;

        txtUserId.setText(u.id != null ? "ID: " + u.id : "ID: -");
        txtUserName.setText(nonEmpty(u.username));
        txtDTDisplayName.setText(nonEmpty(displayName));
        txtEmailUser.setText(nonEmpty(userEmail));

        String role = db.getUserRole(u.email);
        txtStatusBook.setText(role != null && role.equalsIgnoreCase("admin")
                ? "Admin" : (u.active ? "Active" : "Inactive"));

        // โปรไฟล์รูปจาก users.image_uri
        String imgUri = db.getUserImageUri(userEmail);
        if (imgUserDetails != null) {
            if (imgUri != null && !imgUri.trim().isEmpty()) {
                try {
                    imgUserDetails.setImageURI(Uri.parse(imgUri));
                } catch (Exception e) {
                    imgUserDetails.setImageResource(R.drawable.ic_human_background);
                }
            } else {
                imgUserDetails.setImageResource(R.drawable.ic_human_background);
            }
        }
    }

    // ================== INLINE LIKES ==================
    private void populateLikes() {
        likesSection.removeAllViews();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            addEmptyRow(likesSection, "ยังไม่มีประวัติกดใจ");
            return;
        }

        List<DBHelper.LikeRecord> records = db.getUserLikeHistory(userEmail);
        if (records == null || records.isEmpty()) {
            addEmptyRow(likesSection, "ยังไม่มีประวัติกดใจ");
            return;
        }
        for (DBHelper.LikeRecord r : records) {
            String when = dateFmt.format(new Date(r.createdAt));
            addLikeRow(likesSection, r.writingTitle, when);
        }
    }

    private void addLikeRow(ViewGroup parent, String title, String dateText) {
        // หัวข้อเรื่อง
        TextView t1 = buildChipText("เรื่อง: " + safe(title));
        parent.addView(t1, lpMatchWrap(16, 10, 16, 0));
        // วันที่
        TextView t2 = buildSubText("วันที่กดใจ: " + safe(dateText));
        parent.addView(t2, lpMatchWrap(28, 2, 16, 12));
        // เส้นคั่นบาง ๆ
        parent.addView(buildDivider(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    // ================== INLINE COMMENTS ==================
    private void populateComments() {
        commentsSection.removeAllViews();
        if (userEmail == null || userEmail.trim().isEmpty()) {
            addEmptyRow(commentsSection, "ยังไม่มีประวัติการคอมเมนต์");
            return;
        }

        List<DBHelper.CommentRecord> records = db.getUserCommentHistory(userEmail);
        if (records == null || records.isEmpty()) {
            addEmptyRow(commentsSection, "ยังไม่มีประวัติการคอมเมนต์");
            return;
        }
        for (DBHelper.CommentRecord r : records) {
            String when = dateFmt.format(new Date(r.createdAt));
            String ep = (r.episodeTitle == null || r.episodeTitle.trim().isEmpty())
                    ? "" : " • ตอน: " + r.episodeTitle;
            addCommentRow(commentsSection,
                    r.writingTitle + ep,
                    r.content,
                    when);
        }
    }

    private void addCommentRow(ViewGroup parent, String where, String content, String dateText) {
        TextView t1 = buildChipText("เรื่อง: " + safe(where));
        parent.addView(t1, lpMatchWrap(16, 10, 16, 0));

        TextView t2 = buildSubText("ข้อความ: " + safe(content));
        parent.addView(t2, lpMatchWrap(28, 2, 16, 0));

        TextView t3 = buildSubText("วันที่คอมเมนต์: " + safe(dateText));
        parent.addView(t3, lpMatchWrap(28, 2, 16, 12));

        parent.addView(buildDivider(), new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    // ================== UI small helpers ==================
    private void addEmptyRow(ViewGroup parent, String msg) {
        TextView tv = buildSubText(msg);
        parent.addView(tv, lpMatchWrap(16, 12, 16, 0));
    }

    private TextView buildChipText(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextColor(getColor(R.color.purpleAdmin));
        tv.setTextSize(16f);
        return tv;
    }

    private TextView buildSubText(String s) {
        TextView tv = new TextView(this);
        tv.setText(s);
        tv.setTextSize(14f);
        return tv;
    }

    private View buildDivider() {
        View v = new View(this);
        v.setBackgroundColor(getColor(R.color.light_gray)); // มีสีอ่อนๆ ไว้คั่น
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        lp.setMargins(dp(16), dp(12), dp(16), dp(12));
        v.setLayoutParams(lp);
        return v;
    }

    private LinearLayout.LayoutParams lpMatchWrap(int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        return lp;
    }

    private int dp(int v) {
        return Math.round(getResources().getDisplayMetrics().density * v);
    }

    private String nonEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    private String safe(String s) {
        return s == null ? "-" : s;
    }
}
