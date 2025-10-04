package com.example.storysphere_appbar;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BannerPagerAdapter extends RecyclerView.Adapter<BannerPagerAdapter.VH> {
    private final Context ctx;
    private final List<DBHelper.Banner> data;

    public BannerPagerAdapter(Context ctx, List<DBHelper.Banner> data) {
        this.ctx = ctx; this.data = data;
        setHasStableIds(true);
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_banner, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        DBHelper.Banner b = data.get(pos);

        // simple: set ImageView from URI string (works with content://, file://, http(s) via Glide optional)
        try {
            // If you already use Glide/Coil, prefer it. Fallback basic:
            h.img.setImageURI(Uri.parse(b.imagePath));
        } catch (Exception ignore) { /* consider using Glide */ }

        if (b.title != null && !b.title.trim().isEmpty()){
            h.title.setText(b.title);
            h.title.setVisibility(View.VISIBLE);
        } else {
            h.title.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> {
            if (b.deeplink != null && !b.deeplink.trim().isEmpty()){
                try {
                    ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(b.deeplink)));
                } catch (Exception ignored){}
            }
        });
    }

    @Override public int getItemCount() { return data.size(); }
    @Override public long getItemId(int position) { return data.get(position).id; }

    static class VH extends RecyclerView.ViewHolder {
        ImageView img; TextView title;
        VH(View v){ super(v); img = v.findViewById(R.id.imgBanner); title = v.findViewById(R.id.tvTitle); }
    }
}
