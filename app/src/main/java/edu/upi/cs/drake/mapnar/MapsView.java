package edu.upi.cs.drake.mapnar;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationListener;

public class MapsView extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    final static String TAG = "PAAR";
    SensorManager sensorManager;
    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
    }


    final SensorEventListener sensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION)
            {
                headingAngle = sensorEvent.values[0];
                pitchAngle = sensorEvent.values[1];
                rollAngle = sensorEvent.values[2];
                Log.d(TAG, "Heading: " + String.valueOf(headingAngle));
                Log.d(TAG, "Pitch: " + String.valueOf(pitchAngle));
                Log.d(TAG, "Roll: " + String.valueOf(rollAngle));
                if (pitchAngle > 7 || pitchAngle < -7 || rollAngle > 7
                        || rollAngle < -7)
                {
                    launchCameraView();
                }
            }
        }
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub
        }
    };



    public void launchCameraView() {
        finish();
        Intent cameraView = new Intent(this, MainActivity.class);
        startActivity(cameraView);
    }

    /**
                 * Manipulates the map once available.
                 * This callback is triggered when the map is ready to be used.
                 * This is where we can add markers or lines, add listeners or move the camera. In this case,
                 * we just add a marker near Sydney, Australia.
                 * If Google Play services is not installed on the device, the user will be prompted to install
                 * it inside the SupportMapFragment. This method will only be triggered once the user has
                 * installed Google Play services and returned to the app.
                 */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
