package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;

import androidx.appcompat.app.AppCompatActivity;

public class activity_edit_profile extends AppCompatActivity {

    private EditText etUsername, etOldPassword, etNewPassword;
    private TextView tvEmail;
    private View btnSave;
    private DBHelper dbHelper;
    private String userEmail;

    private static final int REQ_PICK_IMAGE = 2001;
    private ImageView imgProfileEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        dbHelper      = new DBHelper(this);
        etUsername    = findViewById(R.id.et_username);
        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        tvEmail       = findViewById(R.id.tv_email);

        userEmail = getIntent().getStringExtra("email");

        // ⭐ ตรงกับ XML: @id/img_profile  (เดิมคุณใช้ img_profile_edit แล้วหาไม่เจอ)
        imgProfileEdit  = findViewById(R.id.img_profile);

        // ⭐ ตรงกับ XML: @id/tv_change_picture (มี onClick ใน XML ด้วย)
        TextView tvChangePicture = findViewById(R.id.tv_change_picture);
        View.OnClickListener pickListener = v -> openSystemPicker();
        imgProfileEdit.setOnClickListener(pickListener);
        if (tvChangePicture != null) tvChangePicture.setOnClickListener(pickListener);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            Intent intent = new Intent(activity_edit_profile.this, activity_profile.class);
            if (userEmail != null && !userEmail.isEmpty()) {
                intent.putExtra("email", userEmail);
            }
            startActivity(intent);
            finish();
        });

        // ----- TEST MODE: ไม่มี email -----
        if (userEmail == null || userEmail.isEmpty()) {
            tvEmail.setText("guest@example.com");
            etUsername.setText("Guest User");
            etOldPassword.setEnabled(false);
            etNewPassword.setEnabled(false);
            // ถ้ามีปุ่ม save ให้ disable ตรงนี้
            Toast.makeText(this, "Test mode (no email) — fields are mocked", Toast.LENGTH_SHORT).show();
            return;
        }

        // ----- NORMAL MODE: มี email -----
        tvEmail.setText(userEmail);
        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                etUsername.setText(cursor.getString(cursor.getColumnIndexOrThrow("username")));

                // ⭐ โหลดรูปโปรไฟล์จากคอลัมน์ image_uri (ยังอยู่ภายใน try ก่อน close)
                int idx = cursor.getColumnIndex("image_uri");
                if (idx != -1) {
                    String uriStr = cursor.getString(idx);
                    if (uriStr != null && !uriStr.isEmpty()) {
                        imgProfileEdit.setImageURI(Uri.parse(uriStr));
                    }
                }
            } else {
                Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // ⭐ เปิด Photo Picker (ไม่ต้องขอ permission เพิ่ม)
    private void openSystemPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    // ⭐ รับรูป → persist permission → แสดงรูป → บันทึกลง DB (image_uri)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                final int takeFlags = data.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                try { getContentResolver().takePersistableUriPermission(uri, takeFlags); } catch (Exception ignore) {}

                imgProfileEdit.setImageURI(uri);

                if (userEmail != null && !userEmail.isEmpty()) {
                    // ใช้เมธอด updateUser(email, username?, password?, imageUri) ที่คุณมีอยู่แล้ว
                    boolean ok = dbHelper.updateUser(userEmail, null, null, uri.toString());
                    Toast.makeText(this, ok ? "อัปเดตรูปโปรไฟล์แล้ว" : "บันทึกรูปล้มเหลว", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void save_change(View view) {
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Test mode: saving is disabled", Toast.LENGTH_SHORT).show();
            return;
        }

        String username   = etUsername.getText().toString().trim();
        String oldPassword= etOldPassword.getText().toString().trim();
        String newPassword= etNewPassword.getText().toString().trim();

        if (username.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = null;
        try {
            cursor = dbHelper.getUserByEmail(userEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String currentPassword = cursor.getString(cursor.getColumnIndexOrThrow("password"));
                if (!oldPassword.equals(currentPassword)) {
                    Toast.makeText(this, "Old password is incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(this, "User not found in database", Toast.LENGTH_SHORT).show();
                return;
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        boolean updated = dbHelper.updateUser(userEmail, username, newPassword); // image_uri ไม่เปลี่ยนในปุ่มนี้
        if (updated) {
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, activity_profile.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    // ⭐ XML ของคุณผูก onClick="onChangePictureClick" ไว้ → เรียก picker
    public void onChangePictureClick(View view) {
        openSystemPicker();
    }
}
