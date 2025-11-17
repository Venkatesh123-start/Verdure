package com.example.verdure;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "verdure_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        int plantId = intent.getIntExtra("plantId", -1);
        DBHelper db = new DBHelper(context);
        Plant p = db.getPlant(plantId);
        String title = "Water time";
        String text = (p != null) ? "Time to water: " + p.getName() : "Time to water your plant";

        createChannel(context);

        Intent open = new Intent(context, MainActivity.class);
        PendingIntent pi = PendingIntent.getActivity(context, plantId+1000, open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(text)
                .setAutoCancel(true)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(plantId + 2000, nb.build());
    }

    private void createChannel(Context ctx) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "Verdure Reminders", NotificationManager.IMPORTANCE_HIGH);
            ch.setDescription("Reminders to water your plants");
            nm.createNotificationChannel(ch);
        }
    }
}
