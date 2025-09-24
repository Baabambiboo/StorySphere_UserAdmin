package com.example.storysphere_appbar;

import java.io.Serializable;

public class WritingItem implements Serializable {

    // ข้อมูลหลัก
    private int id;
    private String title;
    private String tagline;
    private String tag;
    private String category;
    private String imagePath;

    // สถิติ
    private int bookmarks; // จำนวน bookmark (จากตาราง BOOKMARKS)
    private int likes;     // จำนวนกดใจ (จาก writings.likes_count)
    private int views;     // จำนวนวิว (จาก writings.views_count)

    // ===== Constructors =====
    public WritingItem() {}

    // constructor เดิม (คงไว้ให้โค้ดเก่าใช้ได้)
    public WritingItem(int id, String title, String tagline, String tag, String category, String imagePath) {
        this(id, title, tagline, tag, category, imagePath, 0, 0, 0);
    }

    // constructor ใหม่ (รองรับสถิติครบ)
    public WritingItem(int id, String title, String tagline, String tag, String category, String imagePath,
                       int bookmarks, int likes, int views) {
        this.id = id;
        this.title = title;
        this.tagline = tagline;
        this.tag = tag;
        this.category = category;
        this.imagePath = imagePath;
        this.bookmarks = bookmarks;
        this.likes = likes;
        this.views = views;
    }

    // ===== Getters หลัก =====
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getTagline() { return tagline; }
    public String getTag() { return tag; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }

    public int getBookmarks() { return bookmarks; }
    public int getLikes() { return likes; }
    public int getViews() { return views; }

    // ===== Alias Getters (ให้เข้ากันกับโค้ดที่เรียกชื่อเดิม) =====
    public int getBookmarkCount() { return bookmarks; }
    public int getLikesCount() { return likes; }
    public int getViewsCount() { return views; }

    // ===== Setters =====
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public void setTag(String tag) { this.tag = tag; }
    public void setCategory(String category) { this.category = category; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public void setBookmarks(int bookmarks) { this.bookmarks = bookmarks; }
    public void setLikes(int likes) { this.likes = likes; }
    public void setViews(int views) { this.views = views; }

    @Override
    public String toString() {
        return "WritingItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", likes=" + likes +
                ", views=" + views +
                ", bookmarks=" + bookmarks +
                '}';
    }
}
