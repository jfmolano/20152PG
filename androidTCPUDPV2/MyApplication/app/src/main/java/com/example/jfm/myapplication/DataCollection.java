package com.example.jfm.myapplication;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class DataCollection implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mLight;
    private float luz;

    public DataCollection(Context context){
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        luz = -1;
        if (mLight == null){

        }else{
            register();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1){
        // TODO

    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if(event.sensor.getType()==Sensor.TYPE_LIGHT) {
            final float currentReading = event.values[0];
            luz = currentReading;
        }
    }

    public void register(){
        System.out.print("Registro del sensor de luz");
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void unregister(){
        System.out.print("Desregistro del sensor de luz");
        mSensorManager.unregisterListener(this);
    }

    public float darLuzActual(){
        return luz;
    }
}
