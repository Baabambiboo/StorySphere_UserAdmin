package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import jp.wasabeef.richeditor.RichEditor;

public class AddNovelActivity extends AppCompatActivity {

    private EditText editTitle;
    private RichEditor editor;
    private TextView btnSave;
    private ImageButton btnBack;
    private DBHelper dbHelper;
    private androidx.appcompat.widget.Toolbar toolbar;

    // อย่าเปลี่ยนชื่อตัวแปรเหล่านี้
    private int writingId = -1;
    private int episodeId = -1;
    private String mode = "create";

    // ปุ่มจัดรูปแบบ
    private ImageButton btnBold, btnItalic, btnUnderline;
    private ImageButton btnAlignLeft, btnAlignCenter, btnAlignRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_novel);

        // ===== Toolbar =====
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // ซ่อน title
        }

        dbHelper = new DBHelper(this);
        // กันพังถ้ายังไม่มีตาราง episodes
        dbHelper.ensureEpisodesTable();

        // ผูก View
        editTitle = findViewById(R.id.editTitle);
        editor    = findViewById(R.id.editor);
        btnSave   = findViewById(R.id.saveButton);
        btnBack   = findViewById(R.id.btnBack);

        // รับ mode + id
        Intent it = getIntent();
        if (it != null) {
            String m = it.getStringExtra("mode");
            if (m != null) mode = m;

            if ("create".equals(mode)) {
                writingId = it.getIntExtra("writing_id", -1);
                Log.d("AddNovelActivity", "mode=create, writingId=" + writingId);
            } else if ("edit".equals(mode)) {
                episodeId = it.getIntExtra("episode_id", -1);
                Log.d("AddNovelActivity", "mode=edit, episodeId=" + episodeId);
                Episode ep = dbHelper.getEpisodeById(episodeId);
                if (ep != null) {
                    editTitle.setText(ep.title);
                    editor.setHtml(ep.contentHtml);
                }
            }
        }

        // ปุ่มจัดรูปแบบ
        btnBold        = findViewById(R.id.action_bold);
        btnItalic      = findViewById(R.id.action_italic);
        btnUnderline   = findViewById(R.id.action_underline);
        btnAlignLeft   = findViewById(R.id.action_align_left);
        btnAlignCenter = findViewById(R.id.action_align_center);
        btnAlignRight  = findViewById(R.id.action_align_right);

        editor.setEditorHeight(300);
        editor.setEditorFontSize(16);
        editor.setPlaceholder("เขียนเนื้อหานิยายที่นี่...");

        btnBold.setOnClickListener(v -> editor.setBold());
        btnItalic.setOnClickListener(v -> editor.setItalic());
        btnUnderline.setOnClickListener(v -> editor.setUnderline());
        btnAlignLeft.setOnClickListener(v -> editor.setAlignLeft());
        btnAlignCenter.setOnClickListener(v -> editor.setAlignCenter());
        btnAlignRight.setOnClickListener(v -> editor.setAlignRight());

        btnBack.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();

            // เอา HTML จาก RichEditor ตรง ๆ (นี่คือสิ่งที่เราจะบันทึก)
            String html = editor.getHtml();
            if (html == null) html = "";

            // ใช้ plain แค่ “ตรวจว่าง/ไม่ว่าง” เท่านั้น (ไม่บันทึก)
            String plain;
            if (android.os.Build.VERSION.SDK_INT >= 24) {
                plain = android.text.Html.fromHtml(html, android.text.Html.FROM_HTML_MODE_LEGACY).toString().trim();
            } else {
                plain = android.text.Html.fromHtml(html).toString().trim();
            }

            if (title.isEmpty() || plain.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกหัวเรื่องและเนื้อหา", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean ok;
            if ("edit".equals(mode)) {
                if (episodeId == -1) {
                    Toast.makeText(this, "ไม่พบรหัสตอน (episode_id)", Toast.LENGTH_SHORT).show();
                    return;
                }
                ok = dbHelper.updateEpisode(episodeId, title, html, /*isPrivate*/ false);
            } else {
                if (writingId == -1) {
                    Toast.makeText(this, "ไม่พบรหัสเรื่อง (writing_id)", Toast.LENGTH_SHORT).show();
                    return;
                }
                ok = dbHelper.insertEpisode(writingId, title, html, /*isPrivate*/ false);
            }

            if (ok) {
                setResult(RESULT_OK);
                Toast.makeText(this, "บันทึกเรียบร้อย", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "บันทึกไม่สำเร็จ (ตรวจสอบว่า writing_id/episode_id ถูกต้อง)", Toast.LENGTH_LONG).show();
            }
        });
    }

    // กู้ค่า writingId กรณีไม่ได้ส่งมา: เอา id ล่าสุดจาก TABLE_WRITINGS
    private int resolveWritingIdFallback() {
        Cursor c = null;
        try {
            c = dbHelper.getReadableDatabase().rawQuery(
                    "SELECT id FROM " + DBHelper.TABLE_WRITINGS + " ORDER BY id DESC LIMIT 1",
                    null
            );
            if (c.moveToFirst()) return c.getInt(0);
        } catch (Exception ignored) {
        } finally {
            if (c != null) c.close();
        }
        return -1;
    }
}
