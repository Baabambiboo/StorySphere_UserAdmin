package com.example.storysphere_appbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

public class AdminBannerActivity extends AppCompatActivity {
    private ImageView preview;
    private EditText edtTitle, edtDeeplink;
    private Uri picked;
    private DBHelper db;

    // ใช้ OpenDocument เพื่อเก็บสิทธิ์ URI ได้ถาวร
    private final ActivityResultLauncher<String[]> pickImage =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    picked = uri;
                    preview.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_admin_banner);

        db = new DBHelper(this); // ✅ ใช้ตัวแปรเดียว ไม่ประกาศใหม่

        preview = findViewById(R.id.preview);
        edtDeeplink = findViewById(R.id.edtDeeplink);

        findViewById(R.id.btnPick).setOnClickListener(v ->
                pickImage.launch(new String[]{"image/*"}));

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            if (picked == null) {
                Toast.makeText(this, "Please pick an image", Toast.LENGTH_SHORT).show();
                return;
            }

            long id = db.insertBanner(
                    picked.toString(),
                    edtTitle.getText().toString(),
                    edtDeeplink.getText().toString(),
                    true
            );

            Toast.makeText(this, id != -1 ? "Saved!" : "Failed", Toast.LENGTH_SHORT).show();
            if (id != -1) finish();
        });
    }
}
