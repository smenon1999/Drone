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
import android.util.Log;
public class MainActivity extends Activity implements SensorEventListener {

    // motion instance variables
    private ArrayList<BluetoothDevice> devices;
    private BluetoothGatt gatt;
    private SensorManager sensormanager;
    private Sensor accelerometer;
    private Sensor magnetometer;
    private float yaw,pitch,roll;
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
                Toast.makeText(this, "Please turn on bluetooth to use this app", Toast.LENGTH_SHORT).show();
        }

    }

	// everytime there is a change in data on the gyroscope, magnetometer, and accelerometer, register the flight controller data, and send via bluetooth
    private void initMotion() {
        for (int i=0;i<3;i++)
            position[i]=0;
        sensormanager=(SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer=sensormanager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer=sensormanager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensormanager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensormanager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    //
    //
    // onSensorChanged() - gets called whenever the Sensor senses new data
    // @param {SensorEvent event} - the event that occurs when the Sensor senses new data
	// function to register the angle data.
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] mGravity=new float[3];
        float[] mGeomagnetic=new float[3];
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                yaw = (float)Math.toDegrees(orientation[0]);
                pitch = (float)Math.toDegrees(orientation[1]);
                roll = (float)Math.toDegrees(orientation[2]);
            }
        }
    }
	
	/* make sure to fix bluetooth callbacks, so that we can connect to Arduino board and controll the drone.  
	 *
	 *
	 *
	 */

    // don't implement
    @Override
	// used to calibrate accuracy, doesn't require calibration precion
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void connectToBluetooth(View view) {
        BluetoothLeScanner scanner=bluetoothAdapter.getBluetoothLeScanner();
        scanner.startScan(bleCallbackFunction);

        for (BluetoothDevice device:devices) {
            String name=device.getName();
            if (name.equals("UART")) {
                gatt=device.connectGatt(this,false,gattCallback);
                scanner.stopScan(bleCallbackFunction);
            }
        }
    }


    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }

        }
    };

    ScanCallback bleCallbackFunction = new ScanCallback(){
        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getApplicationContext(), "Couldn't start scan", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onScanResult(int callbacktype, ScanResult result) {
            BluetoothDevice device=result.getDevice();
            devices.add(device);
        }
    };


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
