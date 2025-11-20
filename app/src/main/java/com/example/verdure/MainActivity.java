package com.example.verdure;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.example.verdure.adapter.PlantAdapter;
import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    RecyclerView rv;
    FloatingActionButton fab;
    DBHelper db;
    PlantAdapter adapter;
    List<Plant> plants;

    // For notification permission
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                // no-op for now
            });

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_predict) {
            // Open ImagePredictionActivity via toolbar/search icon
            Intent intent = new Intent(this, ImagePredictionActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ 1. Login guard before anything else
        Intent incoming = getIntent();
        int passedUserId = incoming != null ? incoming.getIntExtra("userId", -1) : -1;

        SharedPreferences prefs = getSharedPreferences("verdure_prefs", MODE_PRIVATE);
        int savedUserId = prefs.getInt("userId", -1);

        // If neither login nor saved session, send to LoginActivity
        if (passedUserId == -1 && savedUserId == -1) {
            Log.w(TAG, "No active session. Redirecting to LoginActivity...");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
            return; // stop execution
        }

        // If new login, save the session
        if (passedUserId != -1) {
            prefs.edit().putInt("userId", passedUserId).apply();
        }

        // ✅ 2. Load the dashboard layout
        setContentView(R.layout.activity_main);

        // ✅ 3. Setup toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Verdure");
            getSupportActionBar().setSubtitle("Your plant care dashboard");
        }

        // ✅ 4. Init database + UI
        db = new DBHelper(this);
        rv = findViewById(R.id.recyclerViewPlants);
        fab = findViewById(R.id.fabAddPlant);

        rv.setLayoutManager(new LinearLayoutManager(this));
        loadPlants();

        fab.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, AddEditActivity.class);
            startActivity(i);
        });

        // ✅ 5. Ask for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // Button: Plant Care Guides (existing)
        Button btnGuides = findViewById(R.id.btnPlantCareGuides);
        btnGuides.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, PlantCareGuideActivity.class);
            startActivity(i);
        });

        // NEW: Notifications button
        Button btnNotifications = findViewById(R.id.btnNotifications);
        btnNotifications.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, NotificationActivity.class);
            startActivity(i);
        });

        // NEW: Reminders button
        Button btnReminders = findViewById(R.id.btnReminders);
        btnReminders.setOnClickListener(v -> {
            Intent i = new Intent(MainActivity.this, ReminderActivity.class);
            startActivity(i);
        });

        FloatingActionButton fabLogout = findViewById(R.id.fabLogout);
        if (fabLogout != null) {
            fabLogout.setOnClickListener(v -> logout());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPlants(); // reload when returning from Add/Edit
    }

    private void loadPlants() {
        try {
            plants = db.getAllPlants();
            if (plants == null || plants.isEmpty()) {
                Toast.makeText(this, "No plants added yet.", Toast.LENGTH_SHORT).show();
            }
            adapter = new PlantAdapter(this, plants);
            rv.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load plants", e);
            Toast.makeText(this, "Error loading plants: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ✅ 6. Optional Logout function
    private void logout() {
        getSharedPreferences("verdure_prefs", MODE_PRIVATE)
                .edit().remove("userId").apply();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }
}
