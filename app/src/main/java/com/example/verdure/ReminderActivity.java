package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReminderActivity extends AppCompatActivity {

    private static final String PREFS = "verdure_reminders";
    private static final String KEY_LIST = "reminders_json";

    TextInputEditText editTitle, editMessage;
    Button btnPickDate, btnPickTime, btnSave;
    MaterialCheckBox chkRepeat;
    LinearLayout containerReminders;
    Spinner spinnerPlants;

    Calendar chosen; // holds chosen date/time
    List<Reminder> scheduled = new ArrayList<>();
    private static final AtomicInteger ID_GEN = new AtomicInteger((int) (System.currentTimeMillis() % 10000));

    DBHelper db;
    List<Plant> plantList = new ArrayList<>();
    List<String> plantNames = new ArrayList<>(); // first element will be "None"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        Toolbar toolbar = findViewById(R.id.toolbarReminder);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = new DBHelper(this);
        spinnerPlants = findViewById(R.id.spinnerPlants);

        editTitle = findViewById(R.id.editTitle);
        editMessage = findViewById(R.id.editMessage);
        btnPickDate = findViewById(R.id.btnPickDate);
        btnPickTime = findViewById(R.id.btnPickTime);
        btnSave = findViewById(R.id.btnSaveReminder);
        chkRepeat = findViewById(R.id.chkRepeatDaily);
        containerReminders = findViewById(R.id.containerReminders);

        chosen = Calendar.getInstance();

        loadPlantsIntoSpinner();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());

        btnSave.setOnClickListener(v -> {
            if (chosen == null) {
                Toast.makeText(this, "Pick date and time first", Toast.LENGTH_SHORT).show();
                return;
            }
            saveAndSchedule();
        });

        loadReminders();
        refreshUi();
    }

    private void loadPlantsIntoSpinner() {
        plantList = db.getAllPlants();
        plantNames.clear();
        plantNames.add("None"); // index 0 = no plant
        if (plantList != null) {
            for (Plant p : plantList) plantNames.add(p.getName());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, plantNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlants.setAdapter(adapter);
    }

    private void showDatePicker() {
        Calendar now = Calendar.getInstance();
        DatePickerDialog dp = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    chosen.set(Calendar.YEAR, year);
                    chosen.set(Calendar.MONTH, month);
                    chosen.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    btnPickDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
        dp.show();
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog tp = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    chosen.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    chosen.set(Calendar.MINUTE, minute);
                    chosen.set(Calendar.SECOND, 0);
                    btnPickTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true);
        tp.show();
    }

    private void saveAndSchedule() {
        String title = editTitle.getText()!=null ? editTitle.getText().toString().trim() : "Reminder";
        String text = editMessage.getText()!=null ? editMessage.getText().toString().trim() : "Time to care for your plant";
        boolean repeat = chkRepeat.isChecked();

        long when = chosen.getTimeInMillis();
        if (when < System.currentTimeMillis() - 1000L) {
            Toast.makeText(this, "Selected time is in the past. Pick a future time.", Toast.LENGTH_SHORT).show();
            return;
        }

        // find selected plantId: spinner index 0 is "None"
        int spinnerIndex = spinnerPlants.getSelectedItemPosition();
        int selectedPlantId = -1;
        if (spinnerIndex > 0 && plantList != null && spinnerIndex-1 < plantList.size()) {
            selectedPlantId = plantList.get(spinnerIndex - 1).getId();
        }

        int rid = ID_GEN.getAndIncrement();
        Reminder r = new Reminder(rid, title, text, when, repeat, selectedPlantId);
        scheduled.add(r);
        persistReminders();
        scheduleAlarm(r);

        refreshUi();
        Toast.makeText(this, "Reminder saved.", Toast.LENGTH_SHORT).show();
    }

    private void scheduleAlarm(Reminder r) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        Intent intent = new Intent(this, ReminderReceiver.class);
        intent.putExtra("reminderId", r.id);
        intent.putExtra("title", r.title);
        intent.putExtra("text", r.text);
        intent.putExtra("plantId", r.plantId); // NEW: include plantId

        PendingIntent pi = PendingIntent.getBroadcast(this, r.id, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (am.canScheduleExactAlarms()) {
                    if (r.repeatDaily) {
                        am.setRepeating(AlarmManager.RTC_WAKEUP, r.timeMillis, AlarmManager.INTERVAL_DAY, pi);
                    } else {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.timeMillis, pi);
                    }
                } else {
                    if (r.repeatDaily) {
                        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, r.timeMillis, AlarmManager.INTERVAL_DAY, pi);
                    } else {
                        am.set(AlarmManager.RTC_WAKEUP, r.timeMillis, pi);
                    }
                }
            } else {
                if (r.repeatDaily) {
                    am.setRepeating(AlarmManager.RTC_WAKEUP, r.timeMillis, AlarmManager.INTERVAL_DAY, pi);
                } else {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, r.timeMillis, pi);
                }
            }
        } catch (SecurityException se) {
            if (r.repeatDaily) {
                am.setInexactRepeating(AlarmManager.RTC_WAKEUP, r.timeMillis, AlarmManager.INTERVAL_DAY, pi);
            } else {
                am.set(AlarmManager.RTC_WAKEUP, r.timeMillis, pi);
            }
        }
    }

    private void persistReminders() {
        try {
            JSONArray arr = new JSONArray();
            for (Reminder r : scheduled) arr.put(r.toJson());
            SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
            prefs.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadReminders() {
        scheduled.clear();
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String s = prefs.getString(KEY_LIST, null);
        if (s == null) return;
        try {
            JSONArray arr = new JSONArray(s);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                Reminder r = Reminder.fromJson(o);
                if (r != null) scheduled.add(r);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String findPlantNameById(int pid) {
        if (pid <= 0) return "No plant";
        if (plantList == null) return "Plant";
        for (Plant p : plantList) if (p.getId() == pid) return p.getName();
        return "Plant";
    }

    private void refreshUi() {
        containerReminders.removeAllViews();
        if (scheduled.isEmpty()) {
            TextView t = new TextView(this);
            t.setText("No reminders yet.");
            t.setPadding(6,12,6,12);
            containerReminders.addView(t);
            return;
        }
        for (Reminder r : scheduled) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(6,12,6,12);

            TextView tv = new TextView(this);
            String plantName = (r.plantId>0) ? findPlantNameById(r.plantId) : "No plant";
            tv.setText(r.title + "\n" + plantName + " • " + r.prettyTime() + (r.repeatDaily ? " (daily)" : ""));
            tv.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            Button btnCancel = new Button(this);
            btnCancel.setText("Cancel");
            btnCancel.setOnClickListener(v -> {
                cancelReminder(r.id);
            });

            row.addView(tv);
            row.addView(btnCancel);
            containerReminders.addView(row);
        }
    }

    private void cancelReminder(int id) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            Intent intent = new Intent(this, ReminderReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pi != null) am.cancel(pi);
        }
        Iterator<Reminder> it = scheduled.iterator();
        while (it.hasNext()) {
            if (it.next().id == id) {
                it.remove();
                break;
            }
        }
        persistReminders();
        refreshUi();
        Toast.makeText(this, "Reminder cancelled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
