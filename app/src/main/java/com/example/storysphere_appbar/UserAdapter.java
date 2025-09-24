package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {

    public interface OnItemClick {
        void onClick(User user);
    }

    private final List<User> full;   // ชุดเต็มไว้ใช้กรอง
    private final List<User> data;   // ชุดที่แสดงบนจอ
    private final OnItemClick click;

    public UserAdapter(List<User> users, OnItemClick click) {
        this.full  = new ArrayList<>(users != null ? users : new ArrayList<>());
        this.data  = new ArrayList<>(users != null ? users : new ArrayList<>());
        this.click = click;
        setHasStableIds(true); // ช่วยให้แอนิเมชัน/การเลื่อนลื่นขึ้น
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        User u = data.get(i);
        h.txtUserId.setText(u.code != null ? u.code : (u.id != null ? "ID:" + u.id : ""));
        h.txtUserName.setText(u.username != null ? u.username : "-");
        h.txtDisplayNameUser.setText(u.displayName != null ? u.displayName : "-");
        h.txtEmailUser.setText(u.email != null ? u.email : "-");
        h.txtUserStatus.setText(u.active ? "Active" : "Inactive");

        h.itemView.setOnClickListener(v -> { if (click != null) click.onClick(u); });
    }

    @Override public int getItemCount() { return data.size(); }

    @Override
    public long getItemId(int position) {
        // ใช้ id จริงถ้ามี ไม่งั้นใช้ hashCode ชั่วคราว
        User u = data.get(position);
        return u != null && u.id != null ? u.id : (u.email != null ? u.email.hashCode() : position);
    }

    /** กรองแบบ case-insensitive ด้วย username / display / email / code */
    public void filter(String q) {
        data.clear();
        if (q == null || q.trim().isEmpty()) {
            data.addAll(full);
        } else {
            String k = q.trim().toLowerCase();
            for (User u : full) {
                String username = u.username != null ? u.username.toLowerCase() : "";
                String display  = u.displayName != null ? u.displayName.toLowerCase() : "";
                String email    = u.email != null ? u.email.toLowerCase() : "";
                String code     = u.code != null ? u.code.toLowerCase() : "";
                if (username.contains(k) || display.contains(k) || email.contains(k) || code.contains(k)) {
                    data.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    /** อัปเดตข้อมูลทั้งชุด (เช่น รีโหลดจาก DB) แล้วรีเซ็ต filter */
    public void updateData(List<User> users) {
        full.clear();
        data.clear();
        if (users != null) {
            full.addAll(users);
            data.addAll(users);
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageView11, imgDelete;
        TextView txtUserId, txtUserName, txtDisplayNameUser, txtEmailUser, txtUserStatus;

        VH(View v) {
            super(v);
            imageView11        = v.findViewById(R.id.imageView11);
            imgDelete          = v.findViewById(R.id.imgDelete);
            txtUserId          = v.findViewById(R.id.txtUserId);
            txtUserName        = v.findViewById(R.id.txtUserName);
            txtDisplayNameUser = v.findViewById(R.id.txtDisplayNameUser);
            txtEmailUser       = v.findViewById(R.id.txtEmailUser);
            txtUserStatus      = v.findViewById(R.id.txtUserStatus);
        }
    }
}
