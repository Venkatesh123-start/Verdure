package com.example.verdure;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class GuideDetailActivity extends AppCompatActivity {

    TextView tvTitle, tvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_detail);

        tvTitle = findViewById(R.id.detailTitle);
        tvContent = findViewById(R.id.detailContent);

        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");

        if (title != null) tvTitle.setText(title);
        if (content != null) tvContent.setText(content);
    }
}
