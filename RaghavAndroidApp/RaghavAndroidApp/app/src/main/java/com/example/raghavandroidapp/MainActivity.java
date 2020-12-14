package com.example.raghavandroidapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    String heartRate;
    String respRate;
    String latitude, longitude;
    String coordinates;
    private static final int VIDEO_CAPTURE = 101;
    private Uri fileUri;
    private MediaRecorder mMediaRecorder;
    private Camera mCamera;
    private SurfaceView mSurfaceView;
    TextView heartTextView;
    TextView respTextView;
    String finalBPM;
    db covidDB;
    float curTime;

    //Location Variables
    private TextView textViewLatitude, textViewLongitude;
    private LocationManager locationManager;

    //Local Server Variables
    Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Heading TextView
        TextView tv = (TextView) findViewById(R.id.textView);
        //tv.setText("Measure Heart and Respiratory Rate");

        //HeartRate TextView
        heartTextView = (TextView)findViewById(R.id.textView3);
        heartRate = "";

        //HeartRate Button
        Button btn1 = (Button) findViewById(R.id.heartBtn);
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                Intent intent = new Intent(getApplicationContext(), camera.class);
                startActivityForResult(intent, 123);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

        //Respiratory Rate TextView
        respTextView = (TextView)findViewById(R.id.textView4);
        respRate = "";

        //Respiratory Rate Button (Service)
        Button btn3 = (Button) findViewById(R.id.buttonRes);
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            try {
                Toast.makeText(MainActivity.this, "Respiratory rate is now being recorded.", Toast.LENGTH_LONG).show();
                curTime = (float) (System.currentTimeMillis() / 1000.0);
                accelerometer sensorAccelerometer = new accelerometer(view.getContext(), curTime, respTextView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            }
        });

        //Create Database Button
        Button btn2 = (Button)findViewById(R.id.buttondb);
        btn2.setOnClickListener(new OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                //Log.i(TAG, "Database button clicked ");
                Toast.makeText(getApplicationContext() , "Creating Database!", Toast.LENGTH_LONG).show();
                covidDB = new db(getApplicationContext());
                covidDB.onUpgrade(covidDB.getWritableDatabase(),0,1);
            }
        }
        );

        //Symptoms Button
        Button btn = (Button) findViewById(R.id.button);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                respRate = respTextView.getText().toString();
                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                try {
                    intent.putExtra("heart", heartRate);
                    intent.putExtra("resp", respRate);
                    intent.putExtra("gps", coordinates);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //Location Button
        textViewLatitude = findViewById(R.id.latitude);
        textViewLongitude = findViewById(R.id.longitude);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        Button locationBtn = findViewById(R.id.locationBtn);

        locationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED);
                    {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                    }

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, new LocationListener() {
                        @Override
                        public void onLocationChanged(@NonNull Location location) {
                            latitude = String.valueOf(location.getLatitude());
                            longitude = String.valueOf(location.getLongitude());
                            coordinates = String.valueOf(location.getLatitude()) + ',' + ' ' + String.valueOf(location.getLongitude());

                            textViewLatitude.setText(String.valueOf(location.getLatitude()));
                            textViewLongitude.setText(String.valueOf(location.getLongitude()));

                            //Toast.makeText(getApplicationContext() , "Coordinates: "+ coordinates, Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Log.i(TAG, "Coming in OnActivityResult!");
        if (requestCode == 123 && resultCode == 321) {
            if (data.hasExtra("heartRate")) {
                heartRate = data.getStringExtra("heartRate");
                heartTextView.setText(heartRate);
            }
        }
    }
}