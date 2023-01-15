package com.example.lab4;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.yandex.mapkit.geometry.Point;

import java.util.ArrayList;

public class History extends AppCompatActivity {
    private Button closeButton;
    private ListView listView;
    private SharedPreferences way;
    private ArrayList<ArrayList<Point>> mapPoints;
    private ArrayList<Integer> indexes;
    private Context context;
    private Integer cnt;
    private Button clearButton;
    private ArrayList<String> info;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        init();
    }
    private void init()
    {
        context = this;
        mapPoints = new ArrayList<>();
        indexes = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        way = getSharedPreferences("way", Context.MODE_PRIVATE);
        cnt = 0;
        info = new ArrayList<>();
        while(way.contains(String.valueOf(cnt)))
        {
            String key = String.valueOf(cnt);
            String data = "";
            data = way.getString(key, "");
            if (data.compareTo("-1") != 0) {
                key = String.valueOf(cnt) + ".distance";
                data += "\nDistance: ";
                data += way.getString(key, "");
                data += "\nSpeed: ";
                key = String.valueOf(cnt) + ".speed";
                data += way.getString(key, "");
                info.add(data);
                key = String.valueOf(cnt) + ".truckSize";
                int size = Integer.parseInt(way.getString(key, ""));
                ArrayList<Point> curPoints = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    key = String.valueOf(cnt) + ".truck" + String.valueOf(i) + "Lat";
                    Double latitude = Double.parseDouble(way.getString(key, "0.0"));
                    key = String.valueOf(cnt) + ".truck" + String.valueOf(i) + "Lon";
                    Double longitude = Double.parseDouble(way.getString(key, "0.0"));
                    curPoints.add(new Point(latitude, longitude));
                }
                mapPoints.add(curPoints);
                indexes.add(cnt);
            }
            cnt++;
        }
        if (cnt != 0) {
            String[] sInfo = info.toArray(new String[info.size()]);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sInfo);
            listView.setAdapter(adapter);
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                double[] latitudes = new double[mapPoints.get(i).size()];
                double[] longitudes = new double[mapPoints.get(i).size()];
                for(int j = 0; j < mapPoints.get(i).size(); j++) {
                    latitudes[j] = mapPoints.get(i).get(j).getLatitude();
                    longitudes[j] = mapPoints.get(i).get(j).getLongitude();
                }
                intent.putExtra(MainActivity.lastSizeStr, mapPoints.get(i).size());
                intent.putExtra(MainActivity.lastLatitudesStr, latitudes);
                intent.putExtra(MainActivity.lastLongitudesStr,  longitudes);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder alert_builder = new AlertDialog.Builder(context);
                alert_builder.setMessage("Вы действительно хотите удалить маршрут?");
                alert_builder.setCancelable(false);
                alert_builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        way = getSharedPreferences("way", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = way.edit();
                        editor.putString(String.valueOf(indexes.get(i)), "-1");
                        editor.apply();
                        for (int j = i; j < indexes.size() - 1; j ++)
                            indexes.set(j, indexes.get(j + 1));
                        info.remove(i);
                        String[] new_sInfo = info.toArray(new String[info.size()]);
                        ArrayAdapter<String> new_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new_sInfo);
                        listView.setAdapter(new_adapter);
                    }
                });
                alert_builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                    }
                });
                AlertDialog alert = alert_builder.create();
                alert.setTitle("Delete");
                alert.show();
                return true;
            }
        });
        closeButton = (Button)findViewById(R.id.closeButton3);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        clearButton = (Button)findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_builder = new AlertDialog.Builder(context);
                alert_builder.setMessage("Вы действительно хотите удалить все маршруты?");
                alert_builder.setCancelable(false);
                alert_builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        way = getSharedPreferences("way", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = way.edit();
                        for (int i = 0; i < info.size(); i++)
                        {
                            editor.putString(String.valueOf(indexes.get(i)), "-1");
                            editor.apply();
                        }
                        info.clear();
                        String[] new_sInfo = info.toArray(new String[info.size()]);
                        ArrayAdapter<String> new_adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, new_sInfo);
                        listView.setAdapter(new_adapter);
                    }
                });
                alert_builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which){

                    }
                });
                AlertDialog alert = alert_builder.create();
                alert.setTitle("Delete");
                alert.show();
            }
        });
    }
}