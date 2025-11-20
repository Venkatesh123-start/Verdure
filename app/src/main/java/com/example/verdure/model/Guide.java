package com.example.verdure.model;

public class Guide {
    private int id;
    private String title;
    private String summary;
    private String content;
    private int iconRes; // drawable resource id

    public Guide(int id, String title, String summary, String content, int iconRes) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.iconRes = iconRes;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public int getIconRes() { return iconRes; }
}
