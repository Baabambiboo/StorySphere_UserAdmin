package com.example.storysphere_appbar;

import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class activity_forgot_pass extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button bttLogin;
    TextView ClickLogin, txtShowPass;
    boolean isPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickLogin = findViewById(R.id.ClickLogin);
        txtShowPass = findViewById(R.id.txtShowPass);

        bttLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString().trim();
            String newPassword = edtPassword.getText().toString().trim();

            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "กรุณากรอกอีเมลและรหัสผ่านใหม่", Toast.LENGTH_SHORT).show();
                return;
            }
            if (newPassword.length() < 6) {
                Toast.makeText(this, "รหัสผ่านควรมีอย่างน้อย 6 ตัวอักษร", Toast.LENGTH_SHORT).show();
                return;
            }

            DBHelper dbHelper = new DBHelper(this);
            Cursor c = dbHelper.getUserByEmail(email);
            boolean emailExists = false;
            if (c != null) {
                try {
                    emailExists = c.moveToFirst();
                } finally {
                    c.close();
                }
            }

            if (!emailExists) {
                Toast.makeText(this, "ไม่พบอีเมลนี้ในระบบ", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean updated = dbHelper.updateUserPassword(email, newPassword);
            if (updated) {
                Toast.makeText(this, "เปลี่ยนรหัสผ่านเรียบร้อย", Toast.LENGTH_SHORT).show();
                finish(); // กลับไปหน้า Login
            } else {
                Toast.makeText(this, "ไม่สามารถเปลี่ยนรหัสผ่านได้", Toast.LENGTH_SHORT).show();
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
            edtPassword.setSelection(edtPassword.getText().length());
            isPasswordVisible = !isPasswordVisible;
        });
    }
}
