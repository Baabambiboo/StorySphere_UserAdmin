package com.example.storysphere_appbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EpisodeDrawerAdapter extends RecyclerView.Adapter<EpisodeDrawerAdapter.VH> {

    public interface OnPick {
        void onPick(Episode ep); // ✅ ใช้ Episode (ไฟล์นอก DBHelper)
    }

    private final List<Episode> data; // ✅
    private final OnPick pick;

    public EpisodeDrawerAdapter(List<Episode> data, OnPick pick) {
        this.data = data;
        this.pick = pick;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_episode_simple, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Episode ep = data.get(position); // ✅
        h.tvEpisodeNumber.setText("#" + ep.episodeNo);
        h.tvEpisodeItem.setText(ep.title);
        h.itemView.setOnClickListener(v -> pick.onPick(ep));
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvEpisodeNumber, tvEpisodeItem;
        VH(@NonNull View itemView) {
            super(itemView);
            tvEpisodeNumber = itemView.findViewById(R.id.tvEpisodeNumber);
            tvEpisodeItem   = itemView.findViewById(R.id.tvEpisodeItem);
        }
    }
}
