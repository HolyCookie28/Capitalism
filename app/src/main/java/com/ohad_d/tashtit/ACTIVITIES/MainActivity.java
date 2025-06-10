package com.ohad_d.tashtit.ACTIVITIES;

import android.os.Bundle;

import com.ohad_d.tashtit.R;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.ohad_d.tashtit.ACTIVITIES.BASE.BaseActivity;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);
        getLayoutInflater().inflate(R.layout.activity_main, findViewById(R.id.content_frame));
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
    }

    @Override
    protected void initializeViews() {

        setListeners();
    }

    @Override
    protected void setListeners() {

    }

    @Override
    protected void setViewModel() {

    }
}