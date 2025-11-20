package com.example.verdure;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "verdure_reminders";
    public static final String CHANNEL_NAME = "Verdure Reminders";
    public static final int NOTIF_ID_BASE = 1000; // base id
    private static final String PREFS = "verdure_notifs";
    private static final String KEY_HISTORY = "history_json";

    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("reminderId", 0);
        String title = intent.getStringExtra("title");
        String text = intent.getStringExtra("text");
        int plantId = intent.getIntExtra("plantId", -1); // NEW

        if (title == null) title = "Plant Reminder";
        if (text == null) text = "Time to care for your plant";

        createChannelIfNeeded(context);

        // Build notification and make it open PlantDetailActivity when plantId provided
        Intent open;
        if (plantId > 0) {
            open = new Intent(context, PlantDetailActivity.class);
            open.putExtra("plantId", plantId);
        } else {
            open = new Intent(context, MainActivity.class);
        }
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        android.app.PendingIntent piOpen = android.app.PendingIntent.getActivity(context, id + 1, open,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_reminder)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(piOpen)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID_BASE + id, nb.build());
        }

        // Persist notification history with plantId
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            String raw = prefs.getString(KEY_HISTORY, null);
            JSONArray arr = raw != null ? new JSONArray(raw) : new JSONArray();

            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("title", title);
            obj.put("text", text);
            obj.put("plantId", plantId);

            String iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
            obj.put("time", iso);

            JSONArray newArr = new JSONArray();
            newArr.put(obj);
            for (int i = 0; i < arr.length(); i++) newArr.put(arr.get(i));
            prefs.edit().putString(KEY_HISTORY, newArr.toString()).apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createChannelIfNeeded(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                NotificationChannel ch = nm.getNotificationChannel(CHANNEL_ID);
                if (ch == null) {
                    ch = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
                    ch.setDescription("Reminders to water or care for plants");
                    nm.createNotificationChannel(ch);
                }
            }
        }
    }
}
