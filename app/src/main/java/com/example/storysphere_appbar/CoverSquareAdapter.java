package com.example.storysphere_appbar;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;


public class CoverSquareAdapter extends RecyclerView.Adapter<CoverSquareAdapter.VH> {

    public interface OnItemClick {
        void onClick(WritingItem item);
    }

    private final List<WritingItem> items;
    private final OnItemClick listener;

    public CoverSquareAdapter(List<WritingItem> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cover_square, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        WritingItem it = items.get(position);
        h.tvTitle.setText(it.getTitle() == null ? "" : it.getTitle());

        // โหลดรูปอย่างเบา ๆ : ถ้า imagePath เป็น Uri string ก็ setImageURI ได้
        Context ctx = h.itemView.getContext();
        if (it.getImagePath() != null && !it.getImagePath().isEmpty()) {
            try {
                h.imgCover.setImageURI(Uri.parse(it.getImagePath()));
            } catch (Exception e) {
                h.imgCover.setImageResource(R.drawable.ic_library); // placeholder
            }
        } else {
            h.imgCover.setImageResource(R.drawable.ic_library); // placeholder
        }

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(it);
        });
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgCover;
        TextView tvTitle;
        VH(@NonNull View itemView) {
            super(itemView);
            imgCover = itemView.findViewById(R.id.imgCover);
            tvTitle  = itemView.findViewById(R.id.tvTitle);
        }
    }
}
