package com.example.smeno.dronecontroller;

import android.app.Activity;
import java.util.ArrayList;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.view.View;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.bluetooth.*;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.widget.Toast;

public class MainActivity extends Activity implements SensorEventListener {

    // motion instance variables
    private ArrayList<BluetoothDevice> devices;
    private SensorManager sensormanager;
    private Sensor gyroscope;
    private final float PI=3.14159265f;
    private final float EPSILON=PI/24;
    float [] position=new float[3];

    //bluetooth instance variables
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMotion();
        initBluetooth();
    }

    private void initBluetooth() {
        devices=new ArrayList<BluetoothDevice>();
        bluetoothManager=(BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter=bluetoothManager.getAdapter();
        if (bluetoothAdapter!=null&&!bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 7691243);
        }
    }

    // activity result handling
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode==76912343) {
            if (resultCode==RESULT_CANCELED||resultCode!=RESULT_OK)
                Toast.makeText(this, "Please turn on bluetooth to use this app", Toast.LENGTH_SHORT);
        }

    }


    private void initMotion() {
        for (int i=0;i<3;i++)
            position[i]=0;
        sensormanager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroscope=sensormanager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensormanager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //
    //
    // onSensorChanged() - gets called whenever the Sensor senses new data
    // @param {SensorEvent event} - the event that occurs when the Sensor senses new data
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

            position[0]+=omegaX*dT;
            position[1]+=omegaY*dT;
            position[2]+=omegaZ*dT;

        }
    }

    public void connectToBluetooth(View view) {
        BluetoothLeScanner scanner=bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(bleCallbackFunction);
        scanner.stopScan(bleCallbackFunction);

        for (BluetoothDevice device:devices) {
            String name=device.getName();
            if (name.equals("Adafruit EZ-Link af69")) {

            }
        }
    }

    ScanCallback bleCallbackFunction = new ScanCallback(){
        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getApplicationContext(), "Couldn't start scan", Toast.LENGTH_SHORT);
        }

        @Override
        public void onScanResult(int callbacktype, ScanResult result) {
            BluetoothDevice device=result.getDevice();
            devices.add(device);
        }
    };
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
