package com.example.verdure;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.example.verdure.util.ImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ImagePredictionActivity extends AppCompatActivity {

    ImageView ivPreview;
    Button btnChoose, btnPhoto, btnPredict, btnSaveAsPlant;
    TextView tvDisease, tvStatus;
    LinearLayout llMatches;
    EditText edtNewName, edtNewNotes, edtNewInterval;
    DBHelper db;

    Bitmap pickedBitmap = null;
    String savedImagePath = null;
    String savedImageHist = null;

    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private File cameraImageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_prediction);

        ivPreview = findViewById(R.id.ivPreview);
        btnChoose = findViewById(R.id.btnChoose);
        btnPhoto = findViewById(R.id.btnPhoto);
        btnPredict = findViewById(R.id.btnPredict);
        btnSaveAsPlant = findViewById(R.id.btnSaveAsPlant);
        tvDisease = findViewById(R.id.tvDisease);
        tvStatus = findViewById(R.id.tvStatus);
        llMatches = findViewById(R.id.llMatches);
        edtNewName = findViewById(R.id.edtNewName);
        edtNewNotes = findViewById(R.id.edtNewNotes);
        edtNewInterval = findViewById(R.id.edtNewInterval);

        db = new DBHelper(this);

        // gallery picker
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                File f = copyUriToFile(uri);
                if (f != null) {
                    savedImagePath = f.getAbsolutePath();
                    Bitmap bm = ImageUtils.loadBitmap(savedImagePath, 1200);
                    if (bm != null) {
                        pickedBitmap = bm;
                        ivPreview.setImageBitmap(bm);
                        tvStatus.setText("Image ready. Tap Predict.");
                        llMatches.removeAllViews();
                        tvDisease.setText("");
                    }
                } else {
                    Toast.makeText(this, "Failed to import image", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // camera capture
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success && cameraImageFile != null) {
                savedImagePath = cameraImageFile.getAbsolutePath();
                Bitmap bm = ImageUtils.loadBitmap(savedImagePath, 1200);
                if (bm != null) {
                    pickedBitmap = bm;
                    ivPreview.setImageBitmap(bm);
                    tvStatus.setText("Image ready. Tap Predict.");
                    llMatches.removeAllViews();
                    tvDisease.setText("");
                }
            } else {
                if (cameraImageFile != null && cameraImageFile.exists()) {
                    cameraImageFile.delete();
                    cameraImageFile = null;
                }
            }
        });

        // permission launcher
        requestCameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) startCameraWithFile();
            else Toast.makeText(this, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show();
        });

        btnChoose.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnPhoto.setOnClickListener(v -> ensureCameraPermissionThenLaunch());

        btnPredict.setOnClickListener(v -> {
            if (pickedBitmap == null) {
                Toast.makeText(this, "Pick or take a photo first", Toast.LENGTH_SHORT).show();
                return;
            }
            runPrediction();
        });

        btnSaveAsPlant.setOnClickListener(v -> saveAsPlant());
    }

    // ensure camera permission then start camera
    private void ensureCameraPermissionThenLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraWithFile();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // start camera with file uri
    private void startCameraWithFile() {
        try {
            cameraImageFile = createImageFile();
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".fileprovider", cameraImageFile);
            takePictureLauncher.launch(photoUri);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Unable to start camera", Toast.LENGTH_SHORT).show();
        }
    }

    // copy a content Uri into app file
    private File copyUriToFile(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            File out = createImageFile();
            FileOutputStream fos = new FileOutputStream(out);
            byte[] buf = new byte[8192];
            int r;
            while ((r = is.read(buf)) != -1) fos.write(buf, 0, r);
            fos.close();
            is.close();
            return out;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // create image file
    private File createImageFile() throws Exception {
        String name = "img_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) storageDir = getFilesDir();
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, name + ".png");
    }

    private void runPrediction() {
        // compute histogram
        float[] hist = ImageUtils.computeHistogram(pickedBitmap);
        savedImageHist = ImageUtils.histToString(hist);

        // disease heuristic
        float brown = ImageUtils.brownFraction(pickedBitmap);
        if (brown > 0.07f) {
            tvDisease.setText("Warning: brown spots detected (approx " + String.format("%.2f", brown*100) + "%).");
            tvDisease.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvDisease.setText("No obvious brown spots detected.");
            tvDisease.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // find top matches
        List<Plant> all = db.getAllPlants();
        List<MatchItem> matches = new ArrayList<>();
        for (Plant p : all) {
            if (p.getImageHist() == null || p.getImageHist().isEmpty()) continue;
            float[] oh = ImageUtils.stringToHist(p.getImageHist());
            float sim = ImageUtils.cosineSimilarity(hist, oh);
            matches.add(new MatchItem(p, sim));
        }
        Collections.sort(matches, Comparator.comparingDouble(m -> -m.score));

        // show top 3
        llMatches.removeAllViews();
        int show = Math.min(3, matches.size());
        if (show == 0) {
            TextView t = new TextView(this);
            t.setText("No images in collection to compare.");
            llMatches.addView(t);
        } else {
            for (int idx = 0; idx < show; idx++) {
                MatchItem mi = matches.get(idx);
                View row = getLayoutInflater().inflate(R.layout.row_match_item, llMatches, false);
                TextView tvName = row.findViewById(R.id.rowName);
                TextView tvScore = row.findViewById(R.id.rowScore);
                ProgressBar pb = row.findViewById(R.id.rowProgress);
                Button btnView = row.findViewById(R.id.rowView);
                tvName.setText(mi.plant.getName());
                tvScore.setText(String.format("Score: %.2f", mi.score));
                pb.setProgress((int)(mi.score * 100));
                final MatchItem miFinal = mi;
                btnView.setOnClickListener(v -> {
                    Intent intent = new Intent(ImagePredictionActivity.this, PlantDetailActivity.class);
                    intent.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, miFinal.plant.getId());
                    startActivity(intent);
                });
                llMatches.addView(row);
            }
        }

        // save preview image to internal app folder for later use (if not already saved)
        if (savedImagePath == null || savedImagePath.isEmpty()) {
            try {
                File out = createImageFile();
                FileOutputStream fos = new FileOutputStream(out);
                pickedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
                savedImagePath = out.getAbsolutePath();
            } catch (Exception e) { e.printStackTrace(); }
        }
        tvStatus.setText("Prediction done.");
    }

    private void saveAsPlant() {
        if (savedImagePath == null || savedImageHist == null) {
            Toast.makeText(this, "Run prediction first to save as plant", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = edtNewName.getText() != null ? edtNewName.getText().toString().trim() : "";
        String notes = edtNewNotes.getText() != null ? edtNewNotes.getText().toString().trim() : "";
        String intervalStr = edtNewInterval.getText() != null ? edtNewInterval.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a name for the plant", Toast.LENGTH_SHORT).show();
            return;
        }
        int interval = 0;
        if (!intervalStr.isEmpty()) {
            try { interval = Integer.parseInt(intervalStr); } catch (NumberFormatException e) { Toast.makeText(this, "Interval must be a number", Toast.LENGTH_SHORT).show(); return; }
        }

        Plant p = new Plant();
        p.setName(name); p.setNotes(notes); p.setIntervalDays(interval);
        p.setReminderTime(""); p.setLastWatered("");
        p.setImagePath(savedImagePath); p.setImageHist(savedImageHist);
        long id = db.insertPlant(p);
        if (id > 0) {
            Toast.makeText(this, "Saved as plant \"" + name + "\"", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to save plant", Toast.LENGTH_SHORT).show();
        }
    }

    private static class MatchItem {
        Plant plant;
        float score;
        MatchItem(Plant p, float s){ plant = p; score = s; }
    }
}
