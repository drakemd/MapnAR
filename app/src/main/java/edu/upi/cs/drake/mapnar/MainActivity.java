package edu.upi.cs.drake.mapnar;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.app.Dialog;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
//import android.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LocationListener, ConnectionCallbacks, OnConnectionFailedListener{

    SurfaceView cameraPreview;
    SurfaceHolder previewHolder;
    Camera camera;
    boolean inPreview;
    final static String TAG = "PAAR";
    SensorManager sensorManager;
    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;
    int accelerometerSensor;
    float xAxis;
    float yAxis;
    float zAxis;
    LocationManager locationManager;
    double latitude;
    double longitude;
    double altitude;
    TextView xAxisValue;
    TextView yAxisValue;
    TextView zAxisValue;
    TextView headingValue;
    TextView pitchValue;
    TextView rollValue;
    TextView altitudeValue;
    TextView latitudeValue;
    TextView longitudeValue;
    Button button;
    private static int REQUEST_CODE_RECOVER_PLAY_SERVICES = 200;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    double bearing;
    double distance;
    float lat;
    float lon;
    Location setLoc;
    Location locationInUse;
    TextView bearingValue;
    TextView distanceValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkGooglePlayServices()) {
            buildGoogleApiClient();
        }
        setLoc = new Location("");
        setLoc.setLatitude(-6.859894);
        setLoc.setLongitude(107.590003);
        createLocationRequest();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        orientationSensor = Sensor.TYPE_ORIENTATION;
        accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
        inPreview = false;
        cameraPreview = (SurfaceView)findViewById(R.id.cameraPreview);
        previewHolder = cameraPreview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        xAxisValue = (TextView) findViewById(R.id.xAxisValue);
        yAxisValue = (TextView) findViewById(R.id.yAxisValue);
        zAxisValue = (TextView) findViewById(R.id.zAxisValue);
        headingValue = (TextView) findViewById(R.id.headingValue);
        pitchValue = (TextView) findViewById(R.id.pitchValue);
        rollValue = (TextView) findViewById(R.id.rollValue);
        altitudeValue = (TextView) findViewById(R.id.altitudeValue);
        longitudeValue = (TextView) findViewById(R.id.longitudeValue);
        latitudeValue = (TextView) findViewById(R.id.latitudeValue);
        bearingValue = (TextView) findViewById(R.id.bearingValue);
        distanceValue = (TextView) findViewById(R.id.distanceValue);
        button = (Button) findViewById(R.id.helpButton);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showHelp();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES) {

            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Google Play Services must be installed.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }

    public void launchFlatBack() {
        Intent flatBackIntent = new Intent(this, MapsView.class);
        startActivity(flatBackIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.help:
                showHelp();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showHelp() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.help);
        dialog.setTitle("Help");
        dialog.setCancelable(true);
        //there are a lot of settings, for dialog, check them all out!
        //set up text
        TextView text = (TextView) dialog.findViewById(R.id.TextView01);
        text.setText(R.string.help);
        //set up button
        Button button = (Button) dialog.findViewById(R.id.Button01);
        button.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                dialog.cancel();
            }
        });
        //now that the dialog is set up, it's time to show it
        dialog.show();
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
                headingValue.setText(String.valueOf(headingAngle));
                pitchValue.setText(String.valueOf(pitchAngle));
                rollValue.setText(String.valueOf(rollAngle));
                if (pitchAngle < 7 && pitchAngle > -7 && rollAngle < 7 &&
                        rollAngle > -7)
                {
                    launchFlatBack();
                }
            }else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                xAxis = sensorEvent.values[0];
                yAxis = sensorEvent.values[1];
                zAxis = sensorEvent.values[2];
                Log.d(TAG, "X Axis: " + String.valueOf(xAxis));
                Log.d(TAG, "Y Axis: " + String.valueOf(yAxis));
                Log.d(TAG, "Z Axis: " + String.valueOf(zAxis));
                xAxisValue.setText(String.valueOf(xAxis));
                yAxisValue.setText(String.valueOf(yAxis));
                zAxisValue.setText(String.valueOf(zAxis));
            }
        }
        public void onAccuracyChanged (Sensor senor, int accuracy) {
            //Not used
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, sensorManager
                .getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
        //Camera camera;
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }
        sensorManager.unregisterListener(sensorEventListener);
        if (camera != null)
        {
            camera.release();
            camera=null;
        }
        inPreview=false;
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null)
        {
            camera.release();
            camera=null;
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }else{
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;
                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }
        return(result);
    }

    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            if (camera == null) {
                camera = Camera.open();
            }
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e(TAG, "Exception in setPreviewDisplay()", t);
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
            Camera.Parameters parameters=camera.getParameters();
            Camera.Size size=getBestPreviewSize(width, height, parameters);
            if (size!=null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview=true;
            }
        }
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                camera.stopPreview();
                camera.setPreviewCallback(null);
                camera.release();
                camera = null;
            }
        }
    };

    private boolean checkGooglePlayServices() {

        int checkGooglePlayServices = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (checkGooglePlayServices != ConnectionResult.SUCCESS) {
			/*
			* google play services is missing or update is required
			*  return code could be
			* SUCCESS,
			* SERVICE_MISSING, SERVICE_VERSION_UPDATE_REQUIRED,
			* SERVICE_DISABLED, SERVICE_INVALID.
			*/
            GooglePlayServicesUtil.getErrorDialog(checkGooglePlayServices,
                    this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();

            return false;
        }

        return true;

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

    }


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
            //Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude()+", Longitude:"+mLastLocation.getLongitude(),Toast.LENGTH_LONG).show();
            latitude = mLastLocation.getLatitude();
            longitude = mLastLocation.getLongitude();
            altitude = mLastLocation.getAltitude();
            Log.d(TAG, "Latitude: " + String.valueOf(latitude));
            Log.d(TAG, "Longitude: " + String.valueOf(longitude));
            Log.d(TAG, "Altitude: " + String.valueOf(altitude));
            latitudeValue.setText(String.valueOf(latitude));
            longitudeValue.setText(String.valueOf(longitude));
            altitudeValue.setText(String.valueOf(altitude));
            bearing = mLastLocation.bearingTo(setLoc);
            distance = mLastLocation.distanceTo(setLoc);
            bearingValue.setText(String.valueOf(bearing));
            distanceValue.setText(String.valueOf(distance));
        }
        startLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        //Toast.makeText(this, "Latitude:" + mLastLocation.getLatitude()+", Longitude:"+mLastLocation.getLongitude(),Toast.LENGTH_LONG).show();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        Log.d(TAG, "Latitude: " + String.valueOf(latitude));
        Log.d(TAG, "Longitude: " + String.valueOf(longitude));
        Log.d(TAG, "Altitude: " + String.valueOf(altitude));
        latitudeValue.setText(String.valueOf(latitude));
        longitudeValue.setText(String.valueOf(longitude));
        altitudeValue.setText(String.valueOf(altitude));
        bearing = mLastLocation.bearingTo(setLoc);
        distance = mLastLocation.distanceTo(setLoc);
        bearingValue.setText(String.valueOf(bearing));
        distanceValue.setText(String.valueOf(distance));
    }
}
