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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryVH> {

    private final Context context;
    private final List<WritingItem> stories;
    private final LayoutInflater inf;
    private final DBHelper db;

    // ถ้าหน้านี้จริง ๆ ใช้ layout อื่น เช่น item_cover_square ให้เปลี่ยนตรงนี้
    private static final int ITEM_LAYOUT = R.layout.item_story_reading;

    public StoryAdapter(Context context, List<WritingItem> stories) {
        this.context = context;
        this.stories = stories;
        this.inf = LayoutInflater.from(context);
        this.db  = new DBHelper(context);
    }

    @NonNull
    @Override
    public StoryVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = inf.inflate(ITEM_LAYOUT, parent, false);
        return new StoryVH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryVH h, int position) {
        WritingItem item = stories.get(position);
        final int wid = item.getId();

        // ชื่อ + คำโปรย
        if (h.title != null) h.title.setText(item.getTitle() == null ? "" : item.getTitle());
        if (h.blurb != null) {
            String sub = item.getTagline();
            if (sub == null || sub.trim().isEmpty()) sub = item.getCategory();
            if (sub == null) sub = "";
            h.blurb.setText(sub);
        }

        // โหลดรูปจาก imagePath
        if (h.cover != null) {
            String path = item.getImagePath(); // ต้องมี getter นี้ใน WritingItem
            try {
                if (path != null && !path.trim().isEmpty()) {
                    if (path.startsWith("content://")) {
                        h.cover.setImageURI(Uri.parse(path));
                    } else {
                        Bitmap bmp = BitmapFactory.decodeFile(path);
                        if (bmp != null) {
                            h.cover.setImageBitmap(bmp);
                        } else {
                            h.cover.setImageResource(R.drawable.ic_launcher_foreground);
                        }
                    }
                } else {
                    h.cover.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } catch (Exception e) {
                h.cover.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }

        // ตัวเลขสถิติจาก DB
        if (h.textBookmark != null) h.textBookmark.setText(String.valueOf(Math.max(0, db.countBookmarks(wid))));
        if (h.textHeart    != null) h.textHeart.setText(String.valueOf(Math.max(0, db.getLikes(wid))));
        if (h.textViewCount!= null) h.textViewCount.setText(String.valueOf(Math.max(0, db.getViews(wid))));

        // คลิกการ์ด → ไปหน้าอ่าน
        h.itemView.setOnClickListener(v -> {
            Intent i = new Intent(context, ReadingMainActivity.class);
            i.putExtra(ReadingMainActivity.EXTRA_WRITING_ID, wid);
            context.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return stories == null ? 0 : stories.size();
    }

    static class StoryVH extends RecyclerView.ViewHolder {
        ImageView cover;
        TextView title, blurb, textBookmark, textHeart, textViewCount;

        StoryVH(@NonNull View v) {
            super(v);
            // รูป
            cover = v.findViewById(R.id.imageView);

            // รองรับทั้ง id ชุดใหม่/ชุดเก่า
            title = v.findViewById(R.id.textTitle);
            if (title == null) title = v.findViewById(R.id.bookTitle);

            blurb = v.findViewById(R.id.textBlurb);
            if (blurb == null) blurb = v.findViewById(R.id.bookTagline);

            // แถวสถิติ (id ตาม item_story_reading)
            textBookmark = v.findViewById(R.id.tvBookmark);
            if (textBookmark == null) textBookmark = v.findViewById(R.id.textBookmarkView);

            textHeart = v.findViewById(R.id.tvHeart);
            if (textHeart == null) textHeart = v.findViewById(R.id.textHeartView);

            textViewCount = v.findViewById(R.id.tvEye);
            if (textViewCount == null) textViewCount = v.findViewById(R.id.textViewNumber);
        }
    }
}
