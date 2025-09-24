package com.example.storysphere_appbar;

public class User {
    public Integer id;
    public String code;        // ถ้ามี (เช่น ST01) — ไม่มีก็ปล่อยว่าง
    public String username;
    public String displayName; // ใช้ username แทนถ้าไม่มี
    public String email;
    public boolean active;     // ถ้ายังไม่มีคอลัมน์สถานะ, set true ไปก่อน

    public User(Integer id, String code, String username, String displayName, String email, boolean active) {
        this.id = id;
        this.code = code;
        this.username = username;
        this.displayName = displayName;
        this.email = email;
        this.active = active;
    }
}
