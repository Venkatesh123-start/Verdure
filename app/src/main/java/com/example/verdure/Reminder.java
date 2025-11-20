package com.example.verdure;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Reminder {
    public int id;
    public String title;
    public String text;
    public long timeMillis;
    public boolean repeatDaily;
    public int plantId; // NEW: store linked plant id (-1 = none)

    public Reminder(int id, String title, String text, long timeMillis, boolean repeatDaily, int plantId) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.timeMillis = timeMillis;
        this.repeatDaily = repeatDaily;
        this.plantId = plantId;
    }

    public String prettyTime() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
            return sdf.format(new Date(timeMillis));
        } catch (Exception e) {
            return String.valueOf(timeMillis);
        }
    }

    public JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("title", title);
            o.put("text", text);
            o.put("timeMillis", timeMillis);
            o.put("repeatDaily", repeatDaily);
            o.put("plantId", plantId);
        } catch (JSONException ignored) {}
        return o;
    }

    public static Reminder fromJson(JSONObject o) {
        try {
            int id = o.getInt("id");
            String title = o.optString("title", "Reminder");
            String text = o.optString("text", "");
            long timeMillis = o.optLong("timeMillis", System.currentTimeMillis());
            boolean repeat = o.optBoolean("repeatDaily", false);
            int plantId = o.optInt("plantId", -1);
            return new Reminder(id, title, text, timeMillis, repeat, plantId);
        } catch (JSONException e) {
            return null;
        }
    }
}
