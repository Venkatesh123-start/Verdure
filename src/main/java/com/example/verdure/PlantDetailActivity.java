package com.example.verdure;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.example.verdure.util.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class PlantDetailActivity extends AppCompatActivity {

    public static final String EXTRA_PLANT_ID = "plantId";

    ImageView imageFull;
    TextView tvName, tvNotes, tvLastWatered, tvPrediction, tvDisease;
    Button btnEdit, btnDelete, btnWatered;
    DBHelper db;
    Plant current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_detail);

        imageFull = findViewById(R.id.imageFull);
        tvName = findViewById(R.id.tvName);
        tvNotes = findViewById(R.id.tvNotes);
        tvLastWatered = findViewById(R.id.tvLastWatered);
        tvPrediction = findViewById(R.id.tvPrediction);
        tvDisease = findViewById(R.id.tvDisease);
        btnEdit = findViewById(R.id.btnDetailEdit);
        btnDelete = findViewById(R.id.btnDetailDelete);
        btnWatered = findViewById(R.id.btnDetailWatered);

        db = new DBHelper(this);

        int plantId = getIntent().getIntExtra(EXTRA_PLANT_ID, -1);
        if (plantId == -1) {
            Toast.makeText(this, "Invalid plant", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadPlant(plantId);

        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(PlantDetailActivity.this, AddEditActivity.class);
            i.putExtra("plantId", current.getId());
            startActivity(i);
            finish();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Delete " + current.getName() + " ?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.deletePlant(current.getId());
                        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        btnWatered.setOnClickListener(v -> {
            String isoNow = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).format(new java.util.Date());
            db.updateLastWatered(current.getId(), isoNow);
            tvLastWatered.setText("Last watered: " + new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(new java.util.Date()));
            Toast.makeText(this, "Marked watered", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadPlant(int plantId) {
        current = db.getPlant(plantId);
        if (current == null) {
            Toast.makeText(this, "Plant not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvName.setText(current.getName());
        tvNotes.setText((current.getNotes()==null || current.getNotes().isEmpty()) ? "No notes" : current.getNotes());
        String last = current.getLastWatered();
        if (last == null || last.isEmpty()) tvLastWatered.setText("Last watered: never");
        else tvLastWatered.setText("Last watered: " + niceDate(last));

        // load image (full)
        if (current.getImagePath()!=null && !current.getImagePath().isEmpty()) {
            Bitmap bm = ImageUtils.loadBitmap(current.getImagePath(), 1200);
            if (bm!=null) imageFull.setImageBitmap(bm);
            else imageFull.setImageResource(R.drawable.ic_plant);
        } else {
            imageFull.setImageResource(R.drawable.ic_plant);
        }

        // Prediction: find best match among other plants by histogram similarity
        float bestScore = 0f;
        Plant best = null;
        float[] hist = ImageUtils.stringToHist(current.getImageHist());
        List<Plant> all = db.getAllPlants();
        for (Plant p : all) {
            if (p.getId() == current.getId()) continue;
            float[] oh = ImageUtils.stringToHist(p.getImageHist());
            float sim = ImageUtils.cosineSimilarity(hist, oh);
            if (sim > bestScore) {
                bestScore = sim;
                best = p;
            }
        }
        if (best != null && bestScore > 0.60f) {
            tvPrediction.setText("Looks similar to: " + best.getName() + " (score " + String.format("%.2f", bestScore) + ")");
        } else {
            tvPrediction.setText("No close match found in your collection.");
        }

        // Disease heuristic (brown fraction)
        float brown = 0f;
        try {
            if (current.getImagePath()!=null && !current.getImagePath().isEmpty()) {
                Bitmap bmp = ImageUtils.loadBitmap(current.getImagePath(), 800);
                brown = ImageUtils.brownFraction(bmp);
            }
        } catch (Exception e) { e.printStackTrace(); }
        if (brown > 0.07f) {
            tvDisease.setText("Warning: brown spots detected (approx. " + String.format("%.2f", brown*100) + "%). Consider inspecting for disease.");
            tvDisease.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvDisease.setText("No obvious brown spots detected.");
            tvDisease.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private String niceDate(String iso) {
        try {
            java.text.SimpleDateFormat in = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
            java.util.Date d = in.parse(iso);
            java.text.SimpleDateFormat out = new java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault());
            return out.format(d);
        } catch (Exception e) {
            return iso;
        }
    }
}
