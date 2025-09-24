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
import java.util.Locale;

public class BooksAdapter extends RecyclerView.Adapter<BooksAdapter.VH> {

    // คลิกหลัก + onDelete เป็น optional (default ว่าง)
    public interface OnItemClick {
        void onClick(WritingItem item);
        default void onDelete(WritingItem item, int position) {}
    }

    private final List<WritingItem> full;
    private final List<WritingItem> data;
    private final OnItemClick click;

    public BooksAdapter(List<WritingItem> items, OnItemClick click) {
        List<WritingItem> src = (items != null) ? items : new ArrayList<>();
        this.full  = new ArrayList<>(src);
        this.data  = new ArrayList<>(src);
        this.click = click;
        setHasStableIds(true);
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        View view = LayoutInflater.from(p.getContext()).inflate(R.layout.book_item, p, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        WritingItem w = data.get(i);

        h.txtBookId.setText(w.getId() > 0 ? "ID:" + w.getId() : "-");

        // ใช้ getters ทั้งหมด
        h.txtTitleBook.setText(safe(w.getTitle()));
        h.txtPenName.setText(safe(w.getTagline())); // ปรับเป็น getter อื่นถ้า pen name อยู่ที่อื่น
        h.txtTag.setText(safe(w.getTag()));
        // โชว์สถานะ/หมวด หมายเหตุ: เดิม fix "Ongoing"
        h.txtStatusBook.setText(safe(w.getCategory()));

        h.itemView.setOnClickListener(v -> { if (click != null) click.onClick(w); });
        h.imgDelete.setOnClickListener(v -> {
            if (click != null) click.onDelete(w, h.getAdapterPosition());
        });
    }

    @Override public int getItemCount() { return data.size(); }

    @Override public long getItemId(int pos) { return data.get(pos).getId(); }

    public void filter(String q) {
        data.clear();
        if (q == null || q.trim().isEmpty()) {
            data.addAll(full);
        } else {
            String k = q.trim().toLowerCase(Locale.ROOT);
            for (WritingItem w : full) {
                if (safe(w.getTitle()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getTagline()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getTag()).toLowerCase(Locale.ROOT).contains(k)
                        || safe(w.getCategory()).toLowerCase(Locale.ROOT).contains(k)) {
                    data.add(w);
                }
            }
        }
        notifyDataSetChanged();
    }

    private static String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imageView11, imgDelete;
        TextView txtBookId, txtTitleBook, txtPenName, txtTag, txtStatusBook;
        VH(View v) {
            super(v);
            imageView11   = v.findViewById(R.id.imageView11);
            imgDelete     = v.findViewById(R.id.imgDelete);
            txtBookId     = v.findViewById(R.id.txtBookId);
            txtTitleBook  = v.findViewById(R.id.txtTitleBook);
            txtPenName    = v.findViewById(R.id.txtPenName);
            txtTag        = v.findViewById(R.id.txtTag);
            txtStatusBook = v.findViewById(R.id.txtStatusBook);
        }
    }
}
