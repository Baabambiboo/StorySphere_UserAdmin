package com.example.storysphere_appbar;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_sign_up extends AppCompatActivity {

    EditText edtName, edtEmail, edtPassword;
    Button bttLogin;
    TextView ClickLogin, txtShowPass;
    DBHelper dbHelper;
    boolean isPasswordVisible = false; // ตัวแปรควบคุมสถานะ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        edtName = findViewById(R.id.edtName);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickLogin = findViewById(R.id.ClickLogin);
        dbHelper = new DBHelper(this);
        txtShowPass = findViewById(R.id.txtShowPass);

        bttLogin.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String email = edtEmail.getText().toString().trim();
            String password = edtPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "รูปแบบอีเมลไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.length() < 6) {
                Toast.makeText(this, "รหัสผ่านควรมีอย่างน้อย 6 ตัวอักษร", Toast.LENGTH_SHORT).show();
                return;
            }

            Cursor c = dbHelper.getUserByEmail(email);
            boolean exists = false;
            if (c != null) {
                try {
                    exists = c.moveToFirst();
                } finally {
                    c.close(); // ปิด Cursor
                }
            }
            if (exists) {
                Toast.makeText(this, "อีเมลนี้ถูกใช้ไปแล้ว", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean inserted = dbHelper.insertUser(name, email, password);
            if (inserted) {
                Toast.makeText(this, "สมัครสมาชิกเรียบร้อย", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(activity_sign_up.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "เกิดข้อผิดพลาดในการสมัครสมาชิก", Toast.LENGTH_SHORT).show();
            }
        });

        ClickLogin.setOnClickListener(v -> finish());

        txtShowPass.setOnClickListener(v -> {
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                txtShowPass.setText("Show");
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                txtShowPass.setText("Hide");
            }
            isPasswordVisible = !isPasswordVisible;
            edtPassword.setSelection(edtPassword.length());
        });
    }
}
