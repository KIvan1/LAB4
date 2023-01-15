package com.example.lab4;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.yandex.mapkit.Animation;
import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.geometry.Polyline;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.map.PolylineMapObject;
import com.yandex.mapkit.mapview.MapView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    public static String targetDistanceStr;
    public static String lastLatitudesStr = "LAST_LATITUDES";
    public static String lastLongitudesStr = "LAST_LONGITUDES";
    public static String lastSizeStr = "LAST_SIZE";
    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;
    private final String API_KEY = "1ad0f4e0-0800-4eeb-b39d-82a08b9049cb";
    private Double latitude;
    private Double longitude;
    private TextView tvVelocity;
    private TextView tvDistance;
    private Double distance;
    private int MY_RESQUE_CODE = 100;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastLocation;
    private ArrayList<Point> mapPoints;
    private ArrayList<Point> gotMapPoints;
    private Double targetDistance = 100.0;
    private Button stButton;
    private Point curPoint;
    private PlacemarkMapObject curGeoposition;
    private PolylineMapObject curTrack;
    private boolean isStarted = false;
    private Polyline polyline;
    private ImageButton settingsButton;
    private ActivityResultLauncher<Intent> settingActivityResultProcessor;
    private ActivityResultLauncher<Intent> historyActivityResultProcessor;
    private SharedPreferences settings;
    private String DISTANCE = "DISTANCE";
    private Double sumSpeed = 0.0;
    private int cnt = 1;
    private Button historyButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        createLocationRequest();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        this.locationRequest.setPriority(100);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(this.locationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        int REQUEST_CHECK_SETTINGS = 100;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                    }
                }
            }
        });
    }

    private void init() {
        settings = getSharedPreferences("settings", Context.MODE_PRIVATE);
        if (settings.contains(DISTANCE)) {
            String dist = "";
            dist = settings.getString(DISTANCE, dist);
            targetDistance = Double.parseDouble(dist);
        } else
        {
            targetDistance = 100.0;
        }
        mapPoints = new ArrayList<>();
        gotMapPoints = new ArrayList<>();
        MapKitFactory.setApiKey(API_KEY);
        MapKitFactory.initialize(this);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapView1);
        tvVelocity = (TextView) findViewById(R.id.tvVelocity);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        distance = 0.0;
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        if (location.getSpeed() * 3.6 > 1.5) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            tvVelocity.setText(String.format("%.1f", location.getSpeed() * 3.6));
                            curPoint = new Point(latitude, longitude);
                            curGeoposition.setGeometry(curPoint);
                            if (isStarted) {
                                distance += lastLocation.distanceTo(location);
                                tvDistance.setText(String.format("%.1f", distance) + " из " + String.format("%.1f", targetDistance));
                                mapPoints.add(curPoint);
                                polyline = new Polyline(mapPoints);
                                sumSpeed += location.getSpeed() * 3.6;
                                cnt++;
                                if (curTrack == null)
                                    curTrack = mapView.getMap().getMapObjects().addPolyline(polyline);
                                else
                                    curTrack.setGeometry(polyline);
                            }
                            else {
                                if (curTrack != null)
                                {
                                    mapPoints.clear();
                                    polyline = new Polyline(mapPoints);
                                    curTrack.setGeometry(polyline);
                                }
                            }
                            lastLocation = location;
                        }
                        else {
                            if (!isStarted){
                                if (curTrack != null)
                                {
                                    mapPoints.clear();
                                    polyline = new Polyline(mapPoints);
                                    curTrack.setGeometry(polyline);
                                }
                            }
                            tvVelocity.setText("0.0");
                        }
                    }
                }
            }
        };
        stButton = (Button)findViewById(R.id.startButton);
        stButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isStarted) {
                    isStarted = true;
                    tvDistance.setText(String.format("%.1f", distance) + " из " + String.format("%.1f", targetDistance));
                    stButton.setText("Stop");
                }
                else{
                    tvDistance.setText(String.format("%.1f", distance) + " из " + String.format("%.1f", targetDistance));
                    isStarted = false;
                    stButton.setText("Start");
                    ArrayList<Point> cur = new ArrayList<>();
                    cur.add(curPoint);
                    cur.add(curPoint);
                    polyline = new Polyline(cur);
                    if(curTrack != null)
                        curTrack.setGeometry(polyline);
                    else
                        curTrack = mapView.getMap().getMapObjects().addPolyline(polyline);
                    Intent intent = new Intent("com.example.LAB4.LastTruck");
                    intent.putExtra(LastTruck.distanceKey, distance);
                    intent.putExtra(LastTruck.speedKey, sumSpeed / cnt);
                    mapPoints.add(curPoint);
                    double[] latitudes = new double[mapPoints.size()];
                    double[] longitudes = new double[mapPoints.size()];
                    for(int i = 0; i < mapPoints.size(); i++) {
                        latitudes[i] = mapPoints.get(i).getLatitude();
                        longitudes[i] = mapPoints.get(i).getLongitude();
                    }
                    intent.putExtra(LastTruck.countStr, mapPoints.size());
                    intent.putExtra(LastTruck.latitudePointsKey, latitudes);
                    intent.putExtra(LastTruck.longitudePointsKey,  longitudes);
                    mapPoints.clear();
                    startActivity(intent);
                }
            }
        });
        settingActivityResultProcessor = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK){
                            Intent intent = result.getData();
                            targetDistance = intent.getDoubleExtra(targetDistanceStr, targetDistance);
                        }
                    }
                });
        settingsButton = (ImageButton)findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.example.LAB4.Settings");
                settingActivityResultProcessor.launch(intent);
            }
        });
        historyActivityResultProcessor = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            mapPoints = new ArrayList<>();
                            double[] latitudes = intent.getDoubleArrayExtra(lastLatitudesStr);
                            double[] longitudes = intent.getDoubleArrayExtra(lastLongitudesStr);
                            int size = intent.getIntExtra(lastSizeStr, 0);
                            ArrayList<Point> cur = new ArrayList<>();
                            cur.add(curPoint);
                            cur.add(curPoint);
                            polyline = new Polyline(cur);
                            if (curTrack == null)
                                curTrack = mapView.getMap().getMapObjects().addPolyline(polyline);
                            else
                                curTrack.setGeometry(polyline);
                            gotMapPoints.clear();
                            for (int i = 0; i < size; i++) {
                                gotMapPoints.add(new Point(latitudes[i], longitudes[i]));
                            }
                            polyline = new Polyline(gotMapPoints);
                            if (curTrack == null)
                                curTrack = mapView.getMap().getMapObjects().addPolyline(polyline);
                            else
                                curTrack.setGeometry(polyline);
                        }
                    }
                });
        historyButton = (Button)findViewById(R.id.historyButton);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.example.LAB4.History");
                historyActivityResultProcessor.launch(intent);
            }
        });
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
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void stopLocationUpdates() {
        if (curGeoposition != null)
            mapView.getMap().getMapObjects().remove(curGeoposition);
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_RESQUE_CODE);
        }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            mapView.getMap().move(
                                    new CameraPosition(new Point(latitude, longitude), 19.0f, 0.0f, 0.0f),
                                    new Animation(Animation.Type.SMOOTH, 0),
                                    null);
                            lastLocation = location;
                            if (location.getSpeed() * 3.6 > 1.5)
                                tvVelocity.setText(String.format("%.1f", location.getSpeed() * 3.6));
                            else
                                tvVelocity.setText("0.0");
                            if (isStarted) {
                                tvDistance.setText(String.format("%.1f", distance) + " из " + String.format("%.1f", targetDistance));
                            }
                            else
                                tvDistance.setText(String.format("%.1f", distance));
                            curPoint = new Point(latitude, longitude);
                            curGeoposition = mapView.getMap().getMapObjects().addPlacemark(curPoint);
                        }
                    }
                });
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }
}