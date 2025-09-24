package com.example.storysphere_appbar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TopChartRowAdapter extends RecyclerView.Adapter<TopChartRowAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(WritingItem item);
    }

    private final List<WritingItem> list;
    private final OnItemClickListener listener;
    private final boolean lockBookmarkTint; // true = ไม่เปลี่ยนสีไอคอน bookmark

    public TopChartRowAdapter(List<WritingItem> data,
                              OnItemClickListener listener,
                              boolean lockBookmarkTint) {
        this.list = (data != null) ? new ArrayList<>(data) : new ArrayList<>();
        this.listener = listener;
        this.lockBookmarkTint = lockBookmarkTint;
        setHasStableIds(true);
    }

    /** เรียกตอนต้องการอัปเดตรายการแบบเรียลไทม์ */
    public void replaceData(List<WritingItem> newData) {
        list.clear();
        if (newData != null) list.addAll(newData);
        notifyDataSetChanged();
    }

    @Override public long getItemId(int position) {
        if (position < 0 || position >= list.size()) return RecyclerView.NO_ID;
        return list.get(position).getId();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_top_chart_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        final WritingItem item = list.get(pos);

        // rank
        if (h.tvRank != null) h.tvRank.setText(String.valueOf(pos + 1));

        // title & blurb
        if (h.tvTitle != null) h.tvTitle.setText(!TextUtils.isEmpty(item.getTitle()) ? item.getTitle() : "(untitled)");
        if (h.tvBlurb != null) h.tvBlurb.setText(!TextUtils.isEmpty(item.getTagline()) ? item.getTagline() : "");

        // cover
        bindCoverImage(h.imageView, item.getImagePath());

        // stats
        if (h.tvBookmark != null) h.tvBookmark.setText(String.valueOf(item.getBookmarks()));
        if (h.tvHeart    != null) h.tvHeart.setText(String.valueOf(item.getLikes()));
        if (h.tvEye      != null) h.tvEye.setText(String.valueOf(item.getViews()));

        // หมายเหตุเรื่อง “ไม่เปลี่ยนสี bookmark”:
        // เราไม่ไปยุ่งกับไอคอน/tint ตรงนี้เลย ปล่อย UI ตาม layout/ธีมเดิม
        // ถ้าหน้าบางหน้าต้องเปลี่ยนสี ให้ไปทำในหน้านั้นแทน (adapter นี้อ่านอย่างเดียว)

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override public int getItemCount() { return list.size(); }

    // ---------- helpers ----------

    /** โหลดภาพแบบเบสิก ป้องกัน NPE */
    private void bindCoverImage(ImageView iv, String path) {
        if (iv == null) return; // ป้องกัน NPE (กรณี layout ไม่มี imageView)
        final int fallback = android.R.drawable.ic_menu_report_image;

        if (TextUtils.isEmpty(path)) {
            iv.setImageResource(fallback);
            return;
        }

        try {
            if (path.startsWith("content://")) {
                Uri uri = Uri.parse(path);
                try (InputStream is = iv.getContext().getContentResolver().openInputStream(uri)) {
                    if (is != null) {
                        Bitmap bmp = BitmapFactory.decodeStream(is);
                        if (bmp != null) {
                            iv.setImageBitmap(bmp);
                            return;
                        }
                    }
                }
            } else {
                File f = new File(path);
                if (f.exists()) {
                    BitmapFactory.Options opts = new BitmapFactory.Options();
                    opts.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, opts);
                    int target = 300;
                    opts.inSampleSize = calcInSampleSize(opts, target, target);
                    opts.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeFile(path, opts);
                    if (bmp != null) {
                        iv.setImageBitmap(bmp);
                        return;
                    }
                }
            }
        } catch (Exception ignore) {
        }
        iv.setImageResource(fallback);
    }

    private int calcInSampleSize(BitmapFactory.Options options, int reqW, int reqH) {
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqH || width > reqW) {
            int halfH = height / 2;
            int halfW = width / 2;
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // ---------- ViewHolder ----------
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvRank, tvTitle, tvBlurb;
        final ImageView imageView;
        final TextView tvBookmark, tvHeart, tvEye;

        ViewHolder(View v) {
            super(v);
            tvRank     = v.findViewById(R.id.tvRank);
            tvTitle    = v.findViewById(R.id.textTitle);
            tvBlurb    = v.findViewById(R.id.textBlurb);
            imageView  = v.findViewById(R.id.imageView);
            tvBookmark = v.findViewById(R.id.textBookmarkView);
            tvHeart    = v.findViewById(R.id.textHeartView);
            tvEye      = v.findViewById(R.id.textViewNumber);
        }
    }
}
