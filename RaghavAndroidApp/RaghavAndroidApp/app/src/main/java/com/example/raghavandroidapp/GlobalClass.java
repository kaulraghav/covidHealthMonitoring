package com.example.raghavandroidapp;

import android.app.Application;
public class GlobalClass extends Application {
    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getRespRate() {
        return respRate;
    }

    public void setRespRate(int respRate) {
        this.respRate = respRate;
    }

    private int heartRate;
    private int respRate;
}
