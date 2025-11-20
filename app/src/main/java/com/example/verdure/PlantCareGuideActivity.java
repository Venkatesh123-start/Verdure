package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.verdure.adapter.GuideAdapter;
import com.example.verdure.model.Guide;

import java.util.ArrayList;
import java.util.List;

public class PlantCareGuideActivity extends AppCompatActivity implements GuideAdapter.OnGuideClickListener {

    RecyclerView rvGuides;
    GuideAdapter adapter;
    List<Guide> guides;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_care_guides);

        rvGuides = findViewById(R.id.rvGuides);
        rvGuides.setLayoutManager(new LinearLayoutManager(this));

        guides = createSampleGuides();
        adapter = new GuideAdapter(guides, this);
        rvGuides.setAdapter(adapter);
    }

    // Called by adapter when a guide is clicked
    @Override
    public void onGuideClick(Guide guide) {
        Intent i = new Intent(this, GuideDetailActivity.class);
        i.putExtra("title", guide.getTitle());
        i.putExtra("content", guide.getContent());
        startActivity(i);
    }

    private List<Guide> createSampleGuides() {
        List<Guide> list = new ArrayList<>();

        list.add(new Guide(1,
                "Watering Best Practices",
                "How and when to water different plant types.",
                "1) Check soil moisture: Insert a finger 2–3 cm into the soil. If it feels dry, water.\n\n" +
                        "2) Water deeply: Water until excess drains from the pot’s drainage holes — this encourages deep roots.\n\n" +
                        "3) Avoid daily light watering: Most houseplants prefer infrequent deep watering rather than frequent shallow watering.\n\n" +
                        "4) Adjust by season: Water more in summer and less in winter.\n\n" +
                        "5) Signs of overwatering: yellowing leaves, foul smell in soil, mushy stems.\n\n" +
                        "6) Succulents & cacti: Allow soil to dry completely between waterings.",
                R.drawable.ic_watering)); // use your drawable

        list.add(new Guide(2,
                "Sunlight & Placement",
                "Match plant light requirements to room locations.",
                "1) Understand light levels: Bright direct, bright indirect, low light.\n\n" +
                        "2) South-facing windows (northern hemisphere) provide strong light — good for sun-loving plants.\n\n" +
                        "3) East/West windows give morning or afternoon sun — good for many houseplants.\n\n" +
                        "4) Low light spots: choose ZZ plant, snake plant, pothos.\n\n" +
                        "5) Rotate plants every 2–3 weeks so growth remains even.",
                R.drawable.ic_sun));

        list.add(new Guide(3,
                "Soil & Potting",
                "Choose potting mixes and when to repot.",
                "1) Use well-draining potting mix for most houseplants. Succulents need gritty, sandy mix.\n\n" +
                        "2) Pick pots with drainage holes; standing water causes root rot.\n\n" +
                        "3) Repot when roots start to show through drainage holes or soil compacts (usually every 12–24 months).\n\n" +
                        "4) Use pots 1–2 sizes larger when repotting; don’t oversize drastically.",
                R.drawable.ic_soil));

        list.add(new Guide(4,
                "Fertilizer & Feeding",
                "When and how to fertilize for healthy growth.",
                "1) Fertilize during active growing season (spring/summer) every 4–6 weeks for most plants.\n\n" +
                        "2) Use balanced water-soluble fertilizer (e.g., 10-10-10) or a fertilizer specific to plant type.\n\n" +
                        "3) Reduce or stop feeding in autumn/winter when growth slows.\n\n" +
                        "4) Avoid over-fertilizing: causes leaf burn and salt buildup. Flush soil occasionally with water.",
                R.drawable.ic_fertilizer));

        list.add(new Guide(5,
                "Pest & Disease Prevention",
                "Detect and handle common pests and leaf diseases.",
                "1) Inspect new plants before bringing them indoors.\n\n" +
                        "2) Common pests: aphids, mealybugs, spider mites — wipe leaves with soapy water or use neem oil.\n\n" +
                        "3) For fungal diseases: improve airflow, avoid wetting leaves, remove affected leaves, consider fungicide if severe.\n\n" +
                        "4) Quarantine infected plants to prevent spread.",
                R.drawable.ic_pest));

        list.add(new Guide(6,
                "Seasonal Care",
                "Adjust care with seasons for best results.",
                "Spring/Summer:\n- Increase watering & light exposure; fertilize monthly.\n\n" +
                        "Autumn/Winter:\n- Reduce water and stop feeding; provide more light where possible.\n\n" +
                        "Temperature:\n- Most houseplants prefer 18–24°C (65–75°F); avoid cold drafts.",
                R.drawable.ic_season));

        return list;
    }
}
