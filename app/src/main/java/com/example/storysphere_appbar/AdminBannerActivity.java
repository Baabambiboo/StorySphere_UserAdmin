package com.example.storysphere_appbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class AdminBannerActivity extends AppCompatActivity {

    private ImageView preview;
    private EditText edtDeeplink;
    private Uri selectedImageUri;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_banner);

        db = new DBHelper(this);
        preview = findViewById(R.id.preview);
        edtDeeplink = findViewById(R.id.edtDeeplink);
        Button btnPick = findViewById(R.id.btnPick);
        Button btnSave = findViewById(R.id.btnSave);

        // เปิดเลือกภาพจากแกลเลอรี
        btnPick.setOnClickListener(v -> openGallery());

        // กดบันทึกภาพลงฐานข้อมูล
        btnSave.setOnClickListener(v -> saveBanner());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        imagePickerLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        // เก็บสิทธิ์การเข้าถึง URI แบบถาวร
                        final int takeFlags = result.getData().getFlags()
                                & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        getContentResolver().takePersistableUriPermission(selectedImageUri, takeFlags);

                        // แสดงภาพตัวอย่าง
                        preview.setImageURI(selectedImageUri);
                    }
                }
            });

    private void saveBanner() {
        if (selectedImageUri == null) {
            Toast.makeText(this, "กรุณาเลือกรูปก่อน", Toast.LENGTH_SHORT).show();
            return;
        }

        String deeplink = edtDeeplink.getText().toString().trim();
        long id = db.insertBanner(selectedImageUri.toString(), null, deeplink, true);

        if (id != -1) {
            Toast.makeText(this, "บันทึกสำเร็จ", Toast.LENGTH_SHORT).show();

            // ✅ แจ้งหน้า HomeActivity ให้โหลดแบนเนอร์ใหม่
            Intent intent = new Intent("ACTION_RELOAD_BANNERS");
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

            finish();
        } else {
            Toast.makeText(this, "บันทึกล้มเหลว", Toast.LENGTH_SHORT).show();
        }
    }
}
