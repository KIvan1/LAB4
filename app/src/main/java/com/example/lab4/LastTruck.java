package com.example.lab4;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;
import java.util.Date;

public class LastTruck extends AppCompatActivity {
    public static String distanceKey = "DISTANCE";
    public static String speedKey = "AVG_SPEED";
    public static String latitudePointsKey = "LATITUDE_POINTS";
    public static String longitudePointsKey = "LONGITUDE_POINTS";
    public static String countStr = "POINT_COUNT";
    private Button closeButton;
    private Button saveButton;
    private TextView tvSpeed;
    private TextView tvDistance;
    private Double distance;
    private Double avgSpeed;
    private String DISTANCE = "DISTANCE";
    private SharedPreferences settings;
    private SharedPreferences way;
    private Double targetDistance;
    private ProgressBar progressBar;
    private double[] latitudes = null;
    private double[] longitudes = null;
    private MapView mapView;
    private int count;
    private Polyline polyline;
    private ArrayList<Point> mapPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_last_truck);
        init();
    }

    private void init() {
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (settings.contains(DISTANCE)) {
            String dist = "";
            dist = settings.getString(DISTANCE, dist);
            targetDistance = Double.parseDouble(dist);
        }
        else
        {
            targetDistance = 100.0;
        }
        tvDistance = (TextView)findViewById(R.id.tvDistance1);
        tvSpeed = (TextView)findViewById(R.id.tvSpeed1);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        getData();
        tvDistance.setText(String.format("%.1f", distance) + " / " + String.valueOf(targetDistance));
        tvSpeed.setText(String.format("%.1f", avgSpeed));
        progressBar.setMax((int)Math.round(targetDistance));
        progressBar.setProgress((int)Math.round(distance));
        closeButton = (Button)findViewById(R.id.closeButton2);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cnt = 0;
                way = getSharedPreferences("way", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = way.edit();
                while(way.contains(String.valueOf(cnt)))
                {
                    if ("-1".compareTo(way.getString(String.valueOf(cnt), "")) == 0)
                        break;
                    cnt++;
                }
                String key = String.valueOf(cnt);
                Date date = new Date();
                editor.putString(key, String.valueOf(date));
                editor.apply();
                key = String.valueOf(cnt) + ".distance";
                editor.putString(key, String.format("%.1f", distance));
                editor.apply();
                key = String.valueOf(cnt) + ".speed";
                editor.putString(key, String.format("%.1f", avgSpeed));
                editor.apply();
                key = String.valueOf(cnt) + ".truckSize";
                editor.putString(key, String.valueOf(count));
                editor.apply();
                for (int i = 0; i < count; i++)
                {
                    key = String.valueOf(cnt) + ".truck" + String.valueOf(i) + "Lat";
                    editor.putString(key, String.valueOf(latitudes[i]));
                    editor.apply();
                    key = String.valueOf(cnt) + ".truck" + String.valueOf(i) + "Lon";
                    editor.putString(key, String.valueOf(longitudes[i]));
                    editor.apply();
                }
            }
        });
        initMapView();
    }

    private void initMapView(){
        mapView = (MapView) findViewById(R.id.mapView1);
        mapPoints = new ArrayList<>();
        mapView.getMap().move(
                new CameraPosition(new Point(latitudes[0], longitudes[0]),17.0f, 0.0f, 0.0f),
                new Animation(Animation.Type.SMOOTH, 0),
                null);
        for(int i = 0; i < count; i++)
        {
            mapPoints.add(new Point(latitudes[i], longitudes[i]));
        }
        polyline = new Polyline(mapPoints);
        mapView.getMap().getMapObjects().addPolyline(polyline);
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getData(){
        Bundle mainActivity = getIntent().getExtras();
        distance = mainActivity.getDouble(distanceKey);
        avgSpeed = mainActivity.getDouble(speedKey);
        latitudes = mainActivity.getDoubleArray(latitudePointsKey);
        longitudes = mainActivity.getDoubleArray(longitudePointsKey);
        count = mainActivity.getInt(countStr);
    }
}