package com.example.raghavandroidapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.TextView;
import java.util.ArrayList;

public class accelerometer implements SensorEventListener {
    //Variables
    String TAG = "accelerometer";
    private Context context;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView respRateTextView;

    //Storing Sensor values in an array
    public ArrayList<Double> accelZ = new ArrayList<Double>();
    float timestamp;
    public float breathRate = 0;
    float tempFloat = 0.0f;
    int temp = 0;
    public int noOfSec = 45;
    public float seconds = 1.0f / 1000000000.0f;

    public accelerometer(Context context, float timestamp, TextView tv) {
        this.timestamp = timestamp;
        this.context = context;
        this.respRateTextView = tv;

        //Initializing Accelerometer Sensor
        sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer,SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregisterSensor(){
        sensorManager.unregisterListener(this);
    }

    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (temp == 0) {
                temp++;
                tempFloat = sensorEvent.timestamp * seconds;
            }

            //Calculate Remaining Time
            int t = (int) (tempFloat - (sensorEvent.timestamp * seconds));
            if (-t <= noOfSec) {
                double zVals = sensorEvent.values[2];
                accelZ.add(zVals);
            }
            //Unregister after 45 seconds
            if (-t == noOfSec)
            {
                this.unregisterSensor();
                calcBreathRate(accelZ);

            }
        }
    }

    public void calcBreathRate(ArrayList<Double> accelZ) {
        ArrayList<Float> accelZNew = new ArrayList<>();
        int windowSize = 30;
        int stepSize = 5;
        int len = accelZ.size();

        for (int i = len % windowSize; i + windowSize < len; i += stepSize) {
            int sum = 0;
            for (int j = i; j < windowSize + i; j++) {
                sum += accelZ.get(j);
            }
            accelZNew.add((float) sum / windowSize);
        }

        ArrayList<Float> zeroCrossings = new ArrayList<>();
        for (int i = 1; i < accelZNew.size(); i++) {
            zeroCrossings.add(accelZNew.get(i) - accelZNew.get(i - 1));
        }
        for (int i = 1; i < zeroCrossings.size(); i++) {
            if (zeroCrossings.get(i) == 0 || (zeroCrossings.get(i - 1) > 0 && zeroCrossings.get(i) < 0) || (zeroCrossings.get(i - 1) < 0 && zeroCrossings.get(i) > 0)) {
                breathRate++;
            }
        }

        breathRate = breathRate * 30 / 45;
        if (breathRate > 30) {
            respRateTextView.setText(breathRate / 10 + "");
        }
        else {
            respRateTextView.setText(breathRate + "");
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}



