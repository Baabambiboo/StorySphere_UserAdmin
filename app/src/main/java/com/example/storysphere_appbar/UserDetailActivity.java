package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class UserDetailActivity extends AppCompatActivity {

    private DBHelper db;
    private TextView txtUserId, txtUserName, txtDTDisplayName, txtEmailUser, txtStatusBook;
    private ImageView imgHome;

    // เก็บค่าไว้ส่งต่อ
    private int userId = -1;
    private String userEmail = null;
    private String displayName = null;

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

        // home → AdminPanel
        if (imgHome != null) {
            imgHome.setOnClickListener(v -> {
                Intent i = new Intent(UserDetailActivity.this, AdminPanelActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
            });
        }

        // รับ user_id
        userId = getIntent().getIntExtra("user_id", -1);
        if (userId != -1) bindUser(userId);
        else finish();

        // ==== คลิกการ์ดประวัติ (ใช้ id จาก layout ตรงๆ) ====
        findViewById(R.id.cardView).setOnClickListener(v -> { // ใช้ id = cardView จาก XML
            Intent it = new Intent(UserDetailActivity.this, LikeHistoryActivity.class);
            it.putExtra("user_email", userEmail);
            it.putExtra("display_name", displayName);
            startActivity(it);
        });

        findViewById(R.id.cardviewComment).setOnClickListener(v -> { // ใช้ id = cardviewComment จาก XML
            Intent it = new Intent(UserDetailActivity.this, CommentHistoryActivity.class);
            it.putExtra("user_email", userEmail);
            it.putExtra("display_name", displayName);
            startActivity(it);
        });
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
        if (role != null && role.equalsIgnoreCase("admin")) {
            txtStatusBook.setText("Admin");
        } else {
            txtStatusBook.setText(u.active ? "Active" : "Inactive");
        }
    }

    private String nonEmpty(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
