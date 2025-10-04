package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class activity_profile extends AppCompatActivity {

    private TextView tvUsername, tvEmail;
    private ImageView imgProfile;
    private DBHelper dbHelper;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        dbHelper   = new DBHelper(this);
        tvUsername = findViewById(R.id.tv_username);
        tvEmail    = findViewById(R.id.tv_email);
        imgProfile = findViewById(R.id.imageView);

        // [CHANGED] รับอีเมล: intent → extra_email → DB session (ตัด SharedPreferences ออก)
        userEmail = getIntent().getStringExtra("email");
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = getIntent().getStringExtra("extra_email");
        }
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = dbHelper.getLoggedInUserEmail();        // [ADDED] ใช้ DB session เป็น fallback
        }

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(activity_profile.this, HomeActivity.class);
            if (userEmail != null && !userEmail.isEmpty()) {
                intent.putExtra("email", userEmail);
            }
            startActivity(intent);
            finish();
        });

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        if (userEmail == null || userEmail.isEmpty()) {
            tvUsername.setText("Guest User");
            tvEmail.setText("guest@example.com");
            Toast.makeText(this, "No email provided - test mode", Toast.LENGTH_SHORT).show();
            return;
        }

        tvEmail.setText(userEmail);

        // ดึงชื่อจาก DB
        String username = dbHelper.getUsernameByEmail(userEmail);
        if (username == null || username.trim().isEmpty()) {
            String role = dbHelper.getUserRole(userEmail);
            if (role != null && role.equalsIgnoreCase("admin")) {
                username = "Admin";
            } else {
                int at = userEmail.indexOf('@');
                username = (at > 0) ? userEmail.substring(0, at) : userEmail;
            }
        }
        tvUsername.setText(username);
        // โหลดรูปโปรไฟล์จาก DB (คอลัมน์ image_uri)
        String uriStr = dbHelper.getUserImageUri(userEmail);
        if (uriStr != null && !uriStr.isEmpty()) {
            try { imgProfile.setImageURI(android.net.Uri.parse(uriStr)); }
            catch (Exception e) {
                imgProfile.setImageResource(R.drawable.ic_launcher_foreground); // fallback
            }
        } else {
            imgProfile.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }

    public void edit_profile(View view) {
        Intent intent = new Intent(this, activity_edit_profile.class);
        intent.putExtra("email", userEmail);
        startActivity(intent);
    }

    public void logOut(View view) {
        // [CHANGED] ล้าง session ใน DB (ตัด SharedPreferences ออก)
        dbHelper.clearLoginSession();                           // [ADDED]
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void delete_account(View view) {
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "No account to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean deleted = dbHelper.deleteUser(userEmail);
        if (deleted) {
            // [CHANGED] ล้าง session DB ด้วย
            dbHelper.clearLoginSession();                       // [ADDED]
            Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, activity_sign_up.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show();
        }
    }
}
