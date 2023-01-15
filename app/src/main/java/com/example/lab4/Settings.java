package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends AppCompatActivity {
    private Double targetDistance;
    private Button closeButton;
    private Button acceptButton;
    private TextView tvDistance;
    private String DISTANCE = "DISTANCE";
    private SharedPreferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    public void init(){
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (settings.contains(DISTANCE)) {
            String dist = "";
            dist = settings.getString(DISTANCE, dist);
            targetDistance = Double.parseDouble(dist);
        } else
        {
            targetDistance = 100.0;
        }
        acceptButton = (Button)findViewById(R.id.acceptButton);
        tvDistance = (TextView)findViewById(R.id.distanceInput);
        tvDistance.setText(String.valueOf(targetDistance));
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String dist = String.valueOf(tvDistance.getText());
                targetDistance = Double.parseDouble(dist);
            }
        });
        closeButton = (Button)findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(MainActivity.targetDistanceStr, targetDistance);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DISTANCE, String.valueOf(targetDistance));
        editor.apply();
        super.onPause();
    }

    @Override
    protected void onStop() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(DISTANCE, String.valueOf(targetDistance));
        editor.apply();
        super.onStop();
    }
}