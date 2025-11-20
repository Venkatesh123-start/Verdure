package com.example.verdure;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.example.verdure.util.ImageUtils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AddEditActivity extends AppCompatActivity {

    TextInputEditText editName, editNotes, editInterval, editReminderTime;
    TextInputLayout tilName, tilInterval, tilReminder;
    TextView txtLastWatered;
    Button btnSave;
    ImageView headerImage;
    DBHelper db;
    int plantId = -1;

    // image state
    Bitmap pickedBitmap = null;
    String savedImagePath = null;   // absolute path on disk
    String savedImageHist = null;

    // Launchers
    private ActivityResultLauncher<String> pickImageLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;

    // permission launcher
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    // temp photo file used for camera
    private File cameraImageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        // views
        headerImage = findViewById(R.id.imagePlant);
        tilName = findViewById(R.id.tilPlantName);
        tilInterval = findViewById(R.id.tilInterval);
        tilReminder = findViewById(R.id.tilReminder);

        editName = findViewById(R.id.editPlantName);
        editNotes = findViewById(R.id.editPlantNotes);
        editInterval = findViewById(R.id.editIntervalDays);
        editReminderTime = findViewById(R.id.editReminderTime);
        txtLastWatered = findViewById(R.id.textLastWateredDisplay);
        btnSave = findViewById(R.id.buttonSave);

        db = new DBHelper(this);

        // Register gallery picker (GetContent)
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
                (Uri uri) -> {
                    if (uri != null) {
                        File f = copyUriToFile(uri);
                        if (f != null) {
                            savedImagePath = f.getAbsolutePath();
                            Bitmap bm = ImageUtils.loadBitmap(savedImagePath, 1200);
                            if (bm != null) {
                                pickedBitmap = bm;
                                headerImage.setImageBitmap(bm);
                            }
                        } else {
                            Toast.makeText(this, "Failed to import image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // Register TakePicture that writes to the provided Uri
        takePictureLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
                (Boolean success) -> {
                    if (success && cameraImageFile != null) {
                        savedImagePath = cameraImageFile.getAbsolutePath();
                        Bitmap bm = ImageUtils.loadBitmap(savedImagePath, 1200);
                        if (bm != null) {
                            pickedBitmap = bm;
                            headerImage.setImageBitmap(bm);
                        }
                    } else {
                        // user cancelled or failed - delete temp file if present
                        if (cameraImageFile != null && cameraImageFile.exists()) {
                            cameraImageFile.delete();
                            cameraImageFile = null;
                        }
                    }
                });

        // Register permission request launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCameraWithFile(); // permission granted — proceed
                    } else {
                        Toast.makeText(this, "Camera permission needed to take photos", Toast.LENGTH_SHORT).show();
                    }
                });

        // Click the image to open chooser
        headerImage.setOnClickListener(v -> showImageChooserDialog());

        // load plant if editing
        if (getIntent() != null && getIntent().hasExtra("plantId")) {
            plantId = getIntent().getIntExtra("plantId", -1);
            if (plantId != -1) {
                Plant p = db.getPlant(plantId);
                if (p != null) {
                    editName.setText(p.getName());
                    editNotes.setText(p.getNotes());
                    editInterval.setText(String.valueOf(p.getIntervalDays()));
                    editReminderTime.setText(p.getReminderTime());
                    String last = p.getLastWatered();
                    if (last != null && !last.isEmpty()) txtLastWatered.setText("Last watered: " + niceDate(last));
                    else txtLastWatered.setText("Last watered: never");

                    if (p.getImagePath()!=null && !p.getImagePath().isEmpty()) {
                        Bitmap bm = ImageUtils.loadBitmap(p.getImagePath(), 1200);
                        if (bm != null) {
                            headerImage.setImageBitmap(bm);
                            savedImagePath = p.getImagePath();
                            savedImageHist = p.getImageHist();
                        } else {
                            headerImage.setImageResource(R.drawable.ic_plant);
                        }
                    } else {
                        headerImage.setImageResource(R.drawable.ic_plant);
                    }
                }
            }
        } else {
            headerImage.setImageResource(R.drawable.ic_plant);
            txtLastWatered.setText("Last watered: never");
        }

        // Save button logic
        btnSave.setOnClickListener(v -> {
            // clear errors
            tilName.setError(null); tilInterval.setError(null); tilReminder.setError(null);

            String name = editName.getText()!=null ? editName.getText().toString().trim() : "";
            String notes = editNotes.getText()!=null ? editNotes.getText().toString().trim() : "";
            String intervalStr = editInterval.getText()!=null ? editInterval.getText().toString().trim() : "";
            String reminder = editReminderTime.getText()!=null ? editReminderTime.getText().toString().trim() : "";

            if (TextUtils.isEmpty(name)) { tilName.setError("Enter plant name"); return; }

            int interval = 0;
            if (!TextUtils.isEmpty(intervalStr)) {
                try { interval = Integer.parseInt(intervalStr); } catch (NumberFormatException e) { tilInterval.setError("Enter a valid number"); return; }
            }

            if (!TextUtils.isEmpty(reminder)) {
                if (!reminder.matches("^([01]?\\d|2[0-3]):[0-5]\\d$")) { tilReminder.setError("Invalid time. Use HH:mm"); return; }
            }

            // If pickedBitmap exists but we haven't saved path, save to app folder
            if (pickedBitmap != null && (savedImagePath == null || savedImagePath.isEmpty())) {
                try {
                    File outF = createImageFile();
                    FileOutputStream fos = new FileOutputStream(outF);
                    pickedBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                    fos.close();
                    savedImagePath = outF.getAbsolutePath();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // compute histogram if needed
            if (savedImagePath!=null && (savedImageHist==null || savedImageHist.isEmpty())) {
                Bitmap loaded = ImageUtils.loadBitmap(savedImagePath, 1200);
                if (loaded != null) {
                    float[] hist = ImageUtils.computeHistogram(loaded);
                    savedImageHist = ImageUtils.histToString(hist);
                }
            }

            if (plantId == -1) {
                Plant p = new Plant();
                p.setName(name); p.setNotes(notes); p.setIntervalDays(interval);
                p.setReminderTime(reminder); p.setLastWatered("");
                p.setImagePath(savedImagePath); p.setImageHist(savedImageHist);
                long id = db.insertPlant(p);
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                if (!reminder.isEmpty() && id>0) scheduleReminder((int)id, reminder);
            } else {
                Plant p = new Plant(plantId, name, notes, interval, reminder, "");
                Plant existing = db.getPlant(plantId);
                if (existing != null) p.setLastWatered(existing.getLastWatered());
                p.setImagePath(savedImagePath); p.setImageHist(savedImageHist);
                db.updatePlant(p);
                Toast.makeText(this, "Updated", Toast.LENGTH_SHORT).show();
                if (!reminder.isEmpty()) scheduleReminder(plantId, reminder);
            }

            // optional match/disease toast
            if (savedImagePath!=null && savedImageHist!=null) {
                Bitmap bmp = ImageUtils.loadBitmap(savedImagePath, 1200);
                float[] hist = ImageUtils.stringToHist(savedImageHist);
                float bestScore = 0f; int bestId=-1;
                for (Plant other : db.getAllPlants()) {
                    if (other.getId() == plantId) continue;
                    float[] oh = ImageUtils.stringToHist(other.getImageHist());
                    float sim = ImageUtils.cosineSimilarity(hist, oh);
                    if (sim > bestScore) { bestScore = sim; bestId = other.getId(); }
                }
                String matchText = "No similar plant found";
                if (bestScore > 0.65 && bestId!=-1) {
                    Plant matched = db.getPlant(bestId);
                    if (matched!=null) matchText = "Looks similar to: " + matched.getName() + " (score " + String.format("%.2f", bestScore) + ")";
                }
                float brownFrac = 0f;
                if (bmp!=null) brownFrac = ImageUtils.brownFraction(bmp);
                String diseaseText = (brownFrac > 0.07f) ? "Warning: image shows brown spots, may have disease." : "No obvious brown spots detected.";
                Toast.makeText(this, matchText + "\n" + diseaseText, Toast.LENGTH_LONG).show();
            }

            finish();
        });
    }

    // show chooser dialog
    private void showImageChooserDialog() {
        String[] options = {"Choose image", "Take photo", "Remove photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Image");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    pickImageLauncher.launch("image/*");
                    break;
                case 1:
                    ensureCameraPermissionThenLaunch();
                    break;
                case 2:
                    // remove image
                    pickedBitmap = null;
                    savedImagePath = null;
                    savedImageHist = null;
                    headerImage.setImageResource(R.drawable.ic_plant);
                    break;
                default:
                    dialog.dismiss();
            }
        });
        builder.show();
    }

    // ensure camera permission then start camera
    private void ensureCameraPermissionThenLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCameraWithFile();
        } else {
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // create file + launch camera
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

    // copy a content Uri (gallery) into app private file and return File
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

    // create image file in app external pictures directory
    private File createImageFile() throws Exception {
        String name = "img_" + System.currentTimeMillis();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) storageDir = getFilesDir();
        if (!storageDir.exists()) storageDir.mkdirs();
        return new File(storageDir, name + ".png");
    }

    // Schedule reminder
    private void scheduleReminder(int id, String timeHHmm) {
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm");
        try {
            Date d = fmt.parse(timeHHmm);
            Calendar now = Calendar.getInstance();
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            now.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            now.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            now.set(Calendar.SECOND, 0);
            if (now.before(Calendar.getInstance())) now.add(Calendar.DATE, 1);

            Intent intent = new Intent(this, ReminderReceiver.class);
            intent.putExtra("plantId", id);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
            if (am!=null) am.setRepeating(AlarmManager.RTC_WAKEUP, now.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        } catch (ParseException e) { e.printStackTrace(); }
    }

    private String niceDate(String iso) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date d = in.parse(iso);
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy, HH:mm");
            return out.format(d);
        } catch (Exception e) {
            return iso;
        }
    }
}
