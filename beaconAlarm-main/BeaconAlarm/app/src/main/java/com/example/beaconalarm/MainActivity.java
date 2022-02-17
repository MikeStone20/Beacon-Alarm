package com.example.beaconalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button createBtn;
    private Button statsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createBtn = findViewById(R.id.createBtn);
        statsBtn = findViewById(R.id.statsBtn);

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToCreate();
            }
        });

        statsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToStats();
            }
        });


    }

    private void goToCreate() {
        Intent intent = new Intent(this,CreateAlarms.class);
        startActivity(intent);
    }

    public void goToStats(){
        Intent intent = new Intent(this,ViewStats.class);
        startActivity(intent);
    }

}