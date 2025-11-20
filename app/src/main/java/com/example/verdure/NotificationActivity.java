package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

public class NotificationActivity extends AppCompatActivity {

    private static final String PREFS = "verdure_notifs";
    private static final String KEY_HISTORY = "history_json";

    LinearLayout container;
    Button btnClear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        Toolbar toolbar = findViewById(R.id.toolbarNotification);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        container = findViewById(R.id.containerNotificationList);
        btnClear = findViewById(R.id.btnClearHistory);

        btnClear.setOnClickListener(v -> {
            clearHistory();
        });

        loadAndShow();
    }

    private void loadAndShow() {
        container.removeAllViews();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String raw = prefs.getString(KEY_HISTORY, null);
        if (raw == null) {
            TextView t = new TextView(this);
            t.setText("No notifications yet.");
            t.setPadding(16,16,16,16);
            container.addView(t);
            return;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            if (arr.length() == 0) {
                TextView t = new TextView(this);
                t.setText("No notifications yet.");
                t.setPadding(16,16,16,16);
                container.addView(t);
                return;
            }

            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String title = o.optString("title", "Reminder");
                String text = o.optString("text", "");
                String time = o.optString("time", "");
                int plantId = o.optInt("plantId", -1);

                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(18,18,18,18);

                TextView tvTitle = new TextView(this);
                tvTitle.setText(title);
                tvTitle.setTextSize(16f);
                tvTitle.setTextColor(getResources().getColor(R.color.text_primary));

                TextView tvTime = new TextView(this);
                tvTime.setText(time);
                tvTime.setTextSize(12f);
                tvTime.setTextColor(getResources().getColor(R.color.text_secondary));

                TextView tvText = new TextView(this);
                tvText.setText(text);
                tvText.setTextSize(14f);
                tvText.setTextColor(getResources().getColor(R.color.text_primary));
                tvText.setPadding(0,6,0,0);

                row.addView(tvTitle);
                row.addView(tvTime);
                row.addView(tvText);

                if (plantId > 0) {
                    Button btnOpenPlant = new Button(this);
                    btnOpenPlant.setText("Open plant");
                    btnOpenPlant.setOnClickListener(v -> {
                        Intent intentOpenPlant = new Intent(NotificationActivity.this, PlantDetailActivity.class);
                        intentOpenPlant.putExtra("plantId", plantId);
                        startActivity(intentOpenPlant);
                    });
                    row.addView(btnOpenPlant);
                }

                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                lp.setMargins(0,12,0,12);
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

                container.addView(row);
                container.addView(divider);
            }
        } catch (Exception e) {
            e.printStackTrace();
            TextView t = new TextView(this);
            t.setText("Failed to load notifications.");
            t.setPadding(16,16,16,16);
            container.addView(t);
        }
    }

    private void clearHistory() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        prefs.edit().remove(KEY_HISTORY).apply();
        Toast.makeText(this, "Notification history cleared", Toast.LENGTH_SHORT).show();
        loadAndShow();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
