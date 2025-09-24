package com.example.storysphere_appbar;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.VH> {

    private final Context ctx;
    private final List<WritingItem> data = new ArrayList<>();
    private final LayoutInflater inf;
    private final DBHelper db;

    private static final int ITEM_LAYOUT = R.layout.item_story_reading;

    public BookmarksAdapter(Context ctx, List<WritingItem> items) {
        this.ctx = ctx;
        this.inf = LayoutInflater.from(ctx);
        if (items != null) this.data.addAll(items);
        this.db = new DBHelper(ctx);
    }

    public void replace(List<WritingItem> items) {
        this.data.clear();
        if (items != null) this.data.addAll(items);
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title;
        TextView subtitle;
        TextView tvAuthor;
        TextView tvBm, tvLike, tvView;
        View statRow;
        ImageView ivBookmark;

        VH(@NonNull View v) {
            super(v);
            cover    = v.findViewById(R.id.imageView);
            title    = v.findViewById(R.id.textTitle);
            subtitle = v.findViewById(R.id.textBlurb);
            tvAuthor = v.findViewById(R.id.tvAuthorRight);
            statRow  = v.findViewById(R.id.statRow);
            tvBm     = v.findViewById(R.id.tvBookmark);
            tvLike   = v.findViewById(R.id.tvHeart);
            tvView   = v.findViewById(R.id.tvEye);
            ivBookmark = v.findViewById(R.id.ivBookmark);
        }
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inf.inflate(ITEM_LAYOUT, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        WritingItem it = data.get(pos);
        final int wid = it.getId();

        // ตั้งรูปปกจาก image_path
        setCover(h.cover, it.getImagePath());

        // ชื่อเรื่อง / ซับไตเติล
        if (h.title != null) h.title.setText(it.getTitle() == null ? "(untitled)" : it.getTitle());
        if (h.subtitle != null) {
            String sub = it.getTagline();
            if (sub == null || sub.trim().isEmpty()) sub = it.getCategory();
            if (sub == null) sub = "";
            h.subtitle.setText(sub);
        }

        // ชื่อนักเขียน
        if (h.tvAuthor != null) {
            String author = db.getAuthorNameForWritingId(wid);
            if (author == null || author.trim().isEmpty()) author = "Author";
            h.tvAuthor.setText(author);

            h.tvAuthor.setOnClickListener(v -> {
                String email = db.getAuthorEmailForWriting(wid);
                Intent itFollow = new Intent(ctx, activity_follow_writer.class);
                itFollow.putExtra(activity_follow_writer.EXTRA_EMAIL, email);
                ctx.startActivity(itFollow);
            });
        }

        // ตัวเลข
        if (h.tvBm != null || h.tvLike != null || h.tvView != null) {
            int bm   = it.getBookmarks() > 0 ? it.getBookmarks() : db.countBookmarks(wid);
            int like = it.getLikes()     > 0 ? it.getLikes()     : db.getLikes(wid);
            int view = it.getViews()     > 0 ? it.getViews()     : db.getViews(wid);

            if (h.tvBm   != null) h.tvBm.setText(String.valueOf(bm));
            if (h.tvLike != null) h.tvLike.setText(String.valueOf(like));
            if (h.tvView != null) h.tvView.setText(String.valueOf(view));
        }

        // คลิกการ์ด → หน้าอ่าน
        h.itemView.setOnClickListener(v -> {
            Intent itRead = new Intent(ctx, ReadingMainActivity.class);
            itRead.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, wid);
            ctx.startActivity(itRead);
        });

        // ปุ่ม Bookmark toggle
        if (h.ivBookmark != null) {
            String email = safeEmail(db.getLoggedInUserEmail());
            applyBookmarkUi(h.ivBookmark, db.isBookmarked(email, wid));

            h.ivBookmark.setOnClickListener(v -> {
                String em = safeEmail(db.getLoggedInUserEmail());
                boolean current = db.isBookmarked(em, wid);
                boolean next = !current;
                boolean ok = db.setBookmark(em, wid, next);
                if (!ok) {
                    Toast.makeText(ctx, "ดำเนินการไม่สำเร็จ", Toast.LENGTH_SHORT).show();
                    return;
                }
                applyBookmarkUi(h.ivBookmark, next);
                if (h.tvBm != null) h.tvBm.setText(String.valueOf(db.countBookmarks(wid)));

                if (!next) {
                    int p = h.getAdapterPosition();
                    if (p != RecyclerView.NO_POSITION) {
                        data.remove(p);
                        notifyItemRemoved(p);
                        notifyItemRangeChanged(p, getItemCount() - p);
                    }
                    Toast.makeText(ctx, "เอาออกจาก Library แล้ว", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ctx, "บันทึกเข้าห้องสมุดแล้ว", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    // --- helpers ---
    private String safeEmail(String e){ return (e == null || e.trim().isEmpty()) ? "guest" : e; }

    private void applyBookmarkUi(ImageView iv, boolean bookmarked) {
        if (bookmarked) {
            int filled = ctx.getResources().getIdentifier("ic_bookmark_filled", "drawable", ctx.getPackageName());
            if (filled != 0) iv.setImageResource(filled);
            else iv.setImageResource(R.drawable.ic_bookmark_outline);
            iv.clearColorFilter();
        } else {
            iv.setImageResource(R.drawable.ic_bookmark_outline);
            iv.clearColorFilter();
        }
    }

    // ตั้งรูปจาก path/uri พร้อม fallback
    private void setCover(ImageView iv, String path) {
        if (iv == null) return;
        try {
            if (path != null && !path.trim().isEmpty()) {
                if (path.startsWith("content://")) {
                    iv.setImageURI(Uri.parse(path));
                } else {
                    Bitmap bmp = BitmapFactory.decodeFile(path);
                    if (bmp != null) {
                        iv.setImageBitmap(bmp);
                    } else {
                        iv.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                }
            } else {
                iv.setImageResource(R.drawable.ic_launcher_foreground);
            }
        } catch (Exception e) {
            iv.setImageResource(R.drawable.ic_launcher_foreground);
        }
    }
}
