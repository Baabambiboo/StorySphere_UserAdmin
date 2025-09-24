package com.example.storysphere_appbar;

public class Episode {
    public int episodeId;
    public int writingId;
    public int episodeNo;
    public String title;
    public String contentHtml;
    public boolean isPrivate;
    public long createdAt;
    public long updatedAt;

    public Episode() { }

    public Episode(int episodeId, int writingId, int episodeNo,
                   String title, String contentHtml,
                   boolean isPrivate, long createdAt, long updatedAt) {
        this.episodeId = episodeId;
        this.writingId = writingId;
        this.episodeNo = episodeNo;
        this.title = title;
        this.contentHtml = contentHtml;
        this.isPrivate = isPrivate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
