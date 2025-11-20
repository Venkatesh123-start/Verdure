package com.example.verdure.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.RecyclerView;

import com.example.verdure.AddEditActivity;
import com.example.verdure.PlantDetailActivity;
import com.example.verdure.R;
import com.example.verdure.db.DBHelper;
import com.example.verdure.model.Plant;
import com.example.verdure.util.ImageUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.VH> {

    private Context ctx;
    private List<Plant> list;
    private DBHelper db;

    public PlantAdapter(Context ctx, List<Plant> list) {
        this.ctx = ctx;
        this.list = list;
        db = new DBHelper(ctx);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_plant, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Plant p = list.get(position);

        // Text fields
        holder.name.setText(p.getName() != null ? p.getName() : "Unnamed");
        holder.notes.setText((p.getNotes() == null || p.getNotes().isEmpty()) ? "No notes" : p.getNotes());

        // Load image from saved path (fallback to vector)
        try {
            if (p.getImagePath() != null && !p.getImagePath().isEmpty()) {
                Bitmap bm = ImageUtils.loadBitmap(p.getImagePath(), 400);
                if (bm != null) {
                    holder.imagePlant.setImageBitmap(bm);
                } else {
                    holder.imagePlant.setImageResource(R.drawable.ic_plant);
                }
            } else {
                holder.imagePlant.setImageResource(R.drawable.ic_plant);
            }
        } catch (Exception e) {
            holder.imagePlant.setImageResource(R.drawable.ic_plant);
        }

        // Last watered display
        String last = p.getLastWatered();
        if (last == null || last.isEmpty()) {
            holder.lastWatered.setText("Last watered: never");
        } else {
            holder.lastWatered.setText("Last watered: " + niceDate(last));
        }

        // Set a unique transition name for the image (so shared element transition works)
        String transName = "plantImage-" + p.getId();
        holder.imagePlant.setTransitionName(transName);

        // Item click -> open PlantDetailActivity with shared element transition
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, PlantDetailActivity.class);
            i.putExtra(PlantDetailActivity.EXTRA_PLANT_ID, p.getId());

            // Try to create ActivityOptionsCompat; if ctx is not Activity, fallback to normal start
            try {
                Activity activity = (Activity) ctx;
                Pair<View, String> pair = Pair.create((View) holder.imagePlant, transName);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, pair);
                ctx.startActivity(i, options.toBundle());
            } catch (ClassCastException ex) {
                // fallback
                ctx.startActivity(i);
            }
        });

        // Edit button opens AddEditActivity
        holder.btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(ctx, AddEditActivity.class);
            i.putExtra("plantId", p.getId());
            ctx.startActivity(i);
        });

        // Delete action with confirmation
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(ctx)
                    .setTitle("Delete")
                    .setMessage("Delete " + p.getName() + " ?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        db.deletePlant(p.getId());
                        // remove from local list and notify adapter
                        list.remove(position);
                        notifyItemRemoved(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Watered action (update last_watered to now)
        holder.btnWatered.setOnClickListener(v -> {
            String isoNow = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(new Date());
            db.updateLastWatered(p.getId(), isoNow);
            p.setLastWatered(isoNow);
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    private String niceDate(String iso) {
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date d = in.parse(iso);
            SimpleDateFormat out = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault());
            return out.format(d);
        } catch (Exception e) {
            return iso;
        }
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView imagePlant;
        TextView name, notes, lastWatered;
        Button btnWatered, btnEdit, btnDelete;

        public VH(@NonNull View itemView) {
            super(itemView);
            imagePlant = itemView.findViewById(R.id.imagePlant);
            name = itemView.findViewById(R.id.textPlantName);
            notes = itemView.findViewById(R.id.textPlantNotes);
            lastWatered = itemView.findViewById(R.id.textLastWatered);
            btnWatered = itemView.findViewById(R.id.buttonWatered);
            btnEdit = itemView.findViewById(R.id.buttonEdit);
            btnDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}