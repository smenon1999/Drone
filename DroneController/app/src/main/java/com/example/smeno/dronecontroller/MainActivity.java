package com.example.smeno.dronecontroller;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;

public class MainActivity extends Activity implements SensorEventListener{

    private SensorManager sensormanager;
    private Sensor gyroscope;
    private final float PI=3.14159265f;
    private final float EPSILON=PI/24;
    float [] position=new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMotion();
    }

    private void initMotion() {
        for (int i=0;i<3;i++)
            position[i]=0;
        sensormanager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope=sensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensormanager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected long timestamp;
    @Override
    public void onSensorChanged(SensorEvent event) {

        if (timestamp!=0) {

            // integrate rotational velocity to find position.
            float dT=(event.timestamp-timestamp)/1000000000.0f;
            float omegaX=event.values[0];
            float omegaY=event.values[1];
            float omegaZ=event.values[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float)Math.sqrt(omegaX*omegaX + omegaY*omegaY + omegaZ*omegaZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                omegaX /= omegaMagnitude;
                omegaY /= omegaMagnitude;
                omegaZ /= omegaMagnitude;
            }

            position[0]=omegaX*dT;
            position[1]=omegaY*dT;
            position[2]=omegaZ*dT;

        }
    }

    // don't implement
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }




    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }


}
