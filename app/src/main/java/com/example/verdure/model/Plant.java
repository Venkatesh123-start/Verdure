package com.example.verdure.model;

public class Plant {
    private int id;
    private String name;
    private String notes;
    private int intervalDays; // watering interval
    private String reminderTime; // "HH:mm" or empty
    private String lastWatered; // ISO datetime string or empty
    private String imagePath; // internal file path to saved image
    private String imageHist; // serialized histogram CSV

    public Plant() {}

    public Plant(int id, String name, String notes, int intervalDays, String reminderTime, String lastWatered, String imagePath, String imageHist) {
        this.id = id;
        this.name = name;
        this.notes = notes;
        this.intervalDays = intervalDays;
        this.reminderTime = reminderTime;
        this.lastWatered = lastWatered;
        this.imagePath = imagePath;
        this.imageHist = imageHist;
    }

    // convenience constructor used in previous code (keeps compatibility)
    public Plant(int id, String name, String notes, int intervalDays, String reminderTime, String lastWatered) {
        this(id, name, notes, intervalDays, reminderTime, lastWatered, "", "");
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public int getIntervalDays() { return intervalDays; }
    public void setIntervalDays(int intervalDays) { this.intervalDays = intervalDays; }

    public String getReminderTime() { return reminderTime; }
    public void setReminderTime(String reminderTime) { this.reminderTime = reminderTime; }

    public String getLastWatered() { return lastWatered; }
    public void setLastWatered(String lastWatered) { this.lastWatered = lastWatered; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getImageHist() { return imageHist; }
    public void setImageHist(String imageHist) { this.imageHist = imageHist; }
}
