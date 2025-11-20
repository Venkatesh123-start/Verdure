package com.example.verdure.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.verdure.R;
import com.example.verdure.model.Guide;

import java.util.List;

public class GuideAdapter extends RecyclerView.Adapter<GuideAdapter.VH> {

    public interface OnGuideClickListener {
        void onGuideClick(Guide guide);
    }

    private List<Guide> data;
    private OnGuideClickListener listener;

    public GuideAdapter(List<Guide> data, OnGuideClickListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_guide_item, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Guide g = data.get(position);
        holder.title.setText(g.getTitle());
        holder.summary.setText(g.getSummary());
        if (g.getIconRes() != 0) holder.icon.setImageResource(g.getIconRes());
        else holder.icon.setImageResource(R.drawable.ic_plant);
        holder.card.setOnClickListener(v -> {
            if (listener != null) listener.onGuideClick(g);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        CardView card;
        ImageView icon;
        TextView title, summary;
        VH(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.cardGuide);
            icon = itemView.findViewById(R.id.guideIcon);
            title = itemView.findViewById(R.id.guideTitle);
            summary = itemView.findViewById(R.id.guideSummary);
        }
    }
}
