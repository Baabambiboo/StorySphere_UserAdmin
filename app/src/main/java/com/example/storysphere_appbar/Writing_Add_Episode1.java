package com.example.storysphere_appbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.Arrays;
import java.util.List;

public class Writing_Add_Episode1 extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 101;

    private EditText edtTitle, edtTagline, edtTag;
    private Spinner spinnerCategory;
    private Button bttCreate;
    private ImageView imageViewUpload;
    private TextView uploadImageText;

    private DBHelper dbHelper;
    // เก็บเป็น "URI string" ของภาพ (content://...) เพื่อความเข้ากันได้ทุกเวอร์ชัน
    private String imagePath = "";

    // SAF: เปิดไฟล์รูปแบบปลอดภัย (Android 10+ ดีสุด)
    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;

                // ขอสิทธิ์อ่านแบบถาวร (persist) เพื่อให้เปิดรูปได้แม้รีสตาร์ทแอป
                try {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    );
                } catch (SecurityException ignore) {}

                imageViewUpload.setImageURI(uri);
                imagePath = uri.toString();
                Toast.makeText(this, "เลือกรูปแล้ว", Toast.LENGTH_SHORT).show();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writing_add_episode1);

        // Toolbar back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            startActivity(new Intent(this, activity_writing.class));
            finish();
        });

        // Bind views
        edtTitle = findViewById(R.id.edtAddTitle);
        edtTagline = findViewById(R.id.edtAddTagline);
        edtTag = findViewById(R.id.edtAddTag);
        spinnerCategory = findViewById(R.id.spinner_custom);
        bttCreate = findViewById(R.id.bttCreate);
        imageViewUpload = findViewById(R.id.imageView4);
        uploadImageText = findViewById(R.id.UplodeImage); // มีใน layout ของคุณอยู่แล้ว

        dbHelper = new DBHelper(this);

        // Spinner
        List<String> categories = Arrays.asList("Fantasy", "Romance", "Sci-fi", "Drama","Comedy","Mystery");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        // เปิดตัวเลือกภาพ: รองรับทั้งคลิกรูปและคลิกข้อความ
        imageViewUpload.setOnClickListener(v -> openImagePickerSafely());
        if (uploadImageText != null) {
            uploadImageText.setOnClickListener(v -> openImagePickerSafely());
        }

        // Create
        bttCreate.setOnClickListener(v -> {
            String title = edtTitle.getText().toString().trim();
            String tagline = edtTagline.getText().toString().trim();
            String tag = edtTag.getText().toString().trim();
            String category = spinnerCategory.getSelectedItem().toString();
            String content = ""; // ยังไม่ใช้

            if (title.isEmpty()) {
                edtTitle.setError("กรุณากรอกหัวข้อ");
                edtTitle.requestFocus();
                return;
            }

            long id = dbHelper.insertWriting(title, tagline, tag, category, imagePath, content);
            if (id > 0) {
                Toast.makeText(this, "สร้างงานเขียนใหม่สำเร็จ", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, activity_writing.class));
                finish();
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการบันทึกข้อมูล", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePickerSafely() {
        if (Build.VERSION.SDK_INT >= 33) {
            // Android 13+ ใช้ SAF เลย ไม่ต้องขอ permission
            pickImage.launch(new String[]{"image/*"});
        } else {
            // Android 12- ต้องมี READ_EXTERNAL_STORAGE ก่อน
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch(new String[]{"image/*"});
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage.launch(new String[]{"image/*"});
            } else {
                Toast.makeText(this, "ต้องอนุญาตสิทธิ์เพื่อเลือกภาพ", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
