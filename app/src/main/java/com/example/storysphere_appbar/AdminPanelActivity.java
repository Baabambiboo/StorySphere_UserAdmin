package com.example.storysphere_appbar;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AdminPanelActivity extends AppCompatActivity {

    private TextView txtBookCount, txtUserCount;
    private ImageView imgHome, imgBell;

    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin); // ใช้ XML ที่คุณส่งมา

        db = new DBHelper(this);

        // --- ผูกตัวแปรกับ XML ---
        txtBookCount = findViewById(R.id.txtBookCount);
        txtUserCount = findViewById(R.id.txtUserCount);
        imgHome      = findViewById(R.id.imgHome);
        imgBell      = findViewById(R.id.imgBell);

        // ✅ การ์ด (กดเพื่อไปหน้า list)
        CardView cardBook  = findViewById(R.id.cardView);   // การ์ด Book
        CardView cardUsers = findViewById(R.id.cardView2);  // การ์ด Users

        // --- แสดงจำนวนจาก DB ---
        txtBookCount.setText(String.valueOf(db.countAllWritings()));
        txtUserCount.setText(String.valueOf(db.countAllUsers()));

        // --- ปุ่ม Home: (ถ้าคุณต้องการให้กลับมาหน้านี้อยู่แล้วกดไม่ต้องทำอะไรก็ได้)
        imgHome.setOnClickListener(v -> {
            // ถ้าคลิกแล้วต้องไป "หน้าแรกระบบ" ให้เปลี่ยนเป็น HomeActivity.class
            // startActivity(new Intent(this, HomeActivity.class));
            // ตอนนี้อยู่หน้า admin แล้ว เลยไม่ต้องทำอะไร
        });

        // --- ปุ่ม Bell: ตัวอย่าง Toast ไว้ก่อน ---
        imgBell.setOnClickListener(v ->
                android.widget.Toast.makeText(this, "ยังไม่มีการแจ้งเตือน", android.widget.Toast.LENGTH_SHORT).show()
        );

        // ✅ ไปหน้า Books List
        cardBook.setOnClickListener(v ->
                startActivity(new Intent(this, BooksListActivity.class))
        );

        // ✅ ไปหน้า Users List (ถ้าชื่อคลาสจริงของคุณต่างออกไป ให้แก้ชื่อคลาสตรงนี้)
        cardUsers.setOnClickListener(v ->
                        startActivity(new Intent(this, UserListActivity.class))
                // หรือ UsersListActivity.class แล้วแต่โปรเจกต์คุณใช้ชื่อไหน
        );
        CardView cardBanner = findViewById(R.id.cardBanner);
        cardBanner.setOnClickListener(v ->
                startActivity(new Intent(AdminPanelActivity.this, AdminBannerActivity.class))
        );

    }

}
