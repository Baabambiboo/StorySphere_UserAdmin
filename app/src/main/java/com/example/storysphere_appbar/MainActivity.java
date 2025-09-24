package com.example.storysphere_appbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button bttLogin;
    private TextView ClickSignUp, Forgotpass, txtShowPassword;

    private DBHelper dbHelper;
    private boolean isPasswordVisible = false;

    // Hardcoded admin (ทดสอบเท่านั้น)
    private static final String HARDCODED_ADMIN_EMAIL = "storysphere63@gmail.com";
    private static final String HARDCODED_ADMIN_PASSWORD = "Storysphere987";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);
        // DEBUG ONLY: ใช้ช่วงทดสอบเท่านั้น แล้วค่อยลบทิ้ง
        dbHelper.clearLoginSession();


        String loggedInEmail = dbHelper.getLoggedInUserEmail();
        if (loggedInEmail != null && !loggedInEmail.isEmpty()) {

            // ถ้าอีเมลใน session ไม่มีในตาราง users -> เคลียร์ session แล้วค้างหน้า Login
            if (!dbHelper.checkEmailExists(loggedInEmail)) {
                dbHelper.clearLoginSession();
            } else {
                // ต้องมี role ชัดเจนถึงจะ redirect
                String userRole = dbHelper.getUserRole(loggedInEmail);
                if (userRole != null && !userRole.trim().isEmpty()) {
                    Log.d("MainActivity", "Auto redirect: " + loggedInEmail + " role=" + userRole);
                    navigateUserBasedOnRole(loggedInEmail, userRole);
                    return;
                } else {
                    // role ว่าง/เพี้ยน -> เคลียร์ session
                    dbHelper.clearLoginSession();
                }
            }
        }

        // ===== Bind UI =====
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        bttLogin = findViewById(R.id.bttLogin);
        ClickSignUp = findViewById(R.id.ClickSignUp);
        Forgotpass = findViewById(R.id.Forgotpass);
        txtShowPassword = findViewById(R.id.txtShowPass);

        bttLogin.setOnClickListener(v -> loginUser());
        ClickSignUp.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, activity_sign_up.class)));
        Forgotpass.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, activity_forgot_pass.class)));
        txtShowPassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void loginUser() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // ===== Validation =====
        if (email.isEmpty()) { edtEmail.setError("กรุณากรอกอีเมล"); edtEmail.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { edtEmail.setError("กรุณากรอกอีเมลที่ถูกต้อง"); edtEmail.requestFocus(); return; }
        if (password.isEmpty()) { edtPassword.setError("กรุณากรอกรหัสผ่าน"); edtPassword.requestFocus(); return; }

        boolean ok = false;
        String role = null;

        // ===== Hardcoded admin bypass =====
        if (email.equals(HARDCODED_ADMIN_EMAIL) && password.equals(HARDCODED_ADMIN_PASSWORD)) {
            ok = true;
            role = "admin";
            Log.d("MainActivity", "Bypass admin login");
        } else {
            // ===== เช็ค DB ด้วย Cursor =====
            Cursor c = null;
            try {
                c = dbHelper.getUserByEmail(email);
                if (c != null && c.moveToFirst()) {
                    String storedPassword = c.getString(c.getColumnIndexOrThrow("password"));
                    ok = password.equals(storedPassword); // TODO: เปลี่ยนเป็น hashing+salt ก่อนขึ้น production
                    int idxRole;
                    try {
                        idxRole = c.getColumnIndexOrThrow("role");
                        role = c.getString(idxRole);
                    } catch (Exception ignore) {
                        role = null; // ถ้าไม่มีคอลัมน์ role เดี๋ยวไปดึงด้วยเมธอด dbHelper ข้างล่าง
                    }
                } else {
                    Log.d("MainActivity", "User not found: " + email);
                }
            } catch (Exception e) {
                Log.e("MainActivity", "Cursor error", e);
                ok = false;
            } finally {
                if (c != null && !c.isClosed()) {
                    try { c.close(); } catch (Exception ignore) {}
                }
            }

            if (ok && (role == null || role.isEmpty())) {
                role = dbHelper.getUserRole(email);
            }
        }

        if (ok) {
            Toast.makeText(this, "เข้าสู่ระบบสำเร็จ!", Toast.LENGTH_SHORT).show();

            // ensure admin record ใน DB (ไม่เปลี่ยนชื่อตัวแปรของคุณ)
            if (email.equals(HARDCODED_ADMIN_EMAIL)) {
                try {
                    if (!dbHelper.checkEmailExists(email)) {
                        dbHelper.insertUser("Admin", email, HARDCODED_ADMIN_PASSWORD, "admin");
                    } else {
                        dbHelper.updateUser(email, "Admin", null, null);
                    }
                } catch (Exception e) {
                    Log.e("MainActivity", "Ensure admin record failed", e);
                }
            }

            // บันทึก session ลง DB เท่านั้น
            dbHelper.saveLoginSession(email);

            navigateUserBasedOnRole(email, role);
        } else {
            Toast.makeText(this, "อีเมลหรือรหัสผ่านไม่ถูกต้อง", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateUserBasedOnRole(String email, String role) {
        Intent intent;
        Log.d("MainActivity", "Navigate with role=" + role);
        if ("admin".equalsIgnoreCase(role)) {
            Toast.makeText(this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
            // ใช้หน้าเดียวกับของเพื่อน
            intent = new Intent(MainActivity.this, AdminPanelActivity.class);
        } else {
            intent = new Intent(MainActivity.this, HomeActivity.class);
        }
        intent.putExtra("email", email);
        intent.putExtra("role", role);
        startActivity(intent);
        finish();
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            txtShowPassword.setText("Show"); // หรือใช้ R.string.Show ถ้ามีใน strings.xml
        } else {
            edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            txtShowPassword.setText("Hide"); // หรือใช้ R.string.Hide
        }
        edtPassword.setSelection(edtPassword.getText().length());
        isPasswordVisible = !isPasswordVisible;
    }
}
