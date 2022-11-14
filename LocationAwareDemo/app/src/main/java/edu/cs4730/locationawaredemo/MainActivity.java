package edu.cs4730.locationawaredemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.text.DateFormat;
import java.util.Date;

/*
 * https://github.com/googlesamples/android-play-location/tree/master/LocationAddress/app/src/main
 * http://developer.android.com/training/location/index.html
 *
 * https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition
 *
 * This shows how to get location updates, either the last known and continuing.
 * getLastLocation is get the last known location  and startLocationUpdates() is continuing.
 * Also uses the intent service to get address locations as well.
 *
 * use this for the location aware parts, not the android.location.
 * https://developers.google.com/android/reference/com/google/android/gms/location/package-summary
 */


public class MainActivity extends AppCompatActivity {
    // for checking permissions.
    public static final int REQUEST_ACCESS_startLocationUpdates = 0;
    public static final int REQUEST_ACCESS_onConnected = 1;
    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    private FusedLocationProviderClient mFusedLocationClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    TextView logger;
    Button btn;
    Boolean mRequestingLocationUpdates = false;
    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;
    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;
    /**
     * Callback for Location events.
     */
    private LocationCallback mLocationCallback;


    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        logger = findViewById(R.id.logger);
        btn = findViewById(R.id.button);
        btn.setText("Start location updates");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                locationUpdates();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        createLocationRequest();
        createLocationCallback();
        buildLocationSettingsRequest();
        Log.v(TAG, "starting");
        getLastLocation();
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    public void locationUpdates() {
        if (mRequestingLocationUpdates) {
            //true, so start them

            startLocationUpdates();
            btn.setText("Stop location updates");
        } else {
            //false, so stop them.
            stopLocationUpdates();
            btn.setText("Start location updates");
        }

    }

    protected void createLocationRequest() {
        //deprecated.
//        mLocationRequest = LocationRequest.create()
//            .setInterval(10000)
//            .setFastestInterval(5000)
//            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
//            .setWaitForAccurateLocation(true)  //waits a couple of second initially for a accurate measurement.
//            .setMaxWaitTime(10000);


        mLocationRequest = new LocationRequest.Builder(100000)  //create a requrest with 10000 interval and default rest.
                //now set the rest of the pieces we want to change.
                //.setIntervalMillis(10000)  //not neeeded, since it is part of the builder.
                .setMinUpdateIntervalMillis(50000)  //get an update no faster then 5 seconds.
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(true)  //waits a couple of second initially for a accurate measurement.
                .setMaxUpdateDelayMillis(10000)  //wait only 10 seconds max between
                .build();
    }

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mLastLocation = locationResult.getLastLocation();
                logger.append("\n\n" + DateFormat.getTimeInstance().format(new Date()) + ": ");
                logger.append(" Lat: " + String.valueOf(mLastLocation.getLatitude()));
                logger.append(" Long: " + String.valueOf(mLastLocation.getLongitude()) + "\n");
               // startIntentService();
                startWorker();
            }
        };
    }

    protected void startLocationUpdates() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.REQUEST_ACCESS_startLocationUpdates);
            return;
        }

        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(@NonNull LocationSettingsResponse locationSettingsResponse) {
                        Log.i(TAG, "All location settings are satisfied.");

                        //noinspection MissingPermission
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                                mLocationCallback, Looper.myLooper());

                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                                try {
                                    // Show the dialog by calling startResolutionForResult(), and check the
                                    // result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                                } catch (IntentSender.SendIntentException sie) {
                                    Log.i(TAG, "PendingIntent unable to execute request.");
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                String errorMessage = "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings.";
                                Log.e(TAG, errorMessage);
                                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                                mRequestingLocationUpdates = false;
                        }


                    }
                });


    }

    protected void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    //This shows how to get a "one off" location.  instead of using the location updates
    //
    public void getLastLocation() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    MainActivity.REQUEST_ACCESS_onConnected);
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.w(TAG, "onSuccess:null");
                            logger.append("Last location: None");
                            return;
                        }
                        mLastLocation = location;

                        Log.v(TAG, "getLastLocation");
                        if (mLastLocation != null) {
                            logger.append("Last location: ");
                            logger.append(" Lat: " + String.valueOf(mLastLocation.getLatitude()));
                            logger.append(" Long: " + String.valueOf(mLastLocation.getLongitude()) + "\n");
                            //startIntentService();
                            startWorker();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getLastLocation:onFailure", e);
                        logger.append("Last location: Fail");
                    }
                });

    }

    /**
     * Creates an Worker, adds location data to it as an extra, and starts worker for
     * fetching an address.  It then sets an observer to get the data back and display it via
     * the logger.
     */


    public void startWorker() {
        Data myData = new Data.Builder()
                .putDouble(Constants.LATITUDE, mLastLocation.getLatitude())
                .putDouble(Constants.LONGITUDE, mLastLocation.getLongitude())
                .build();
        OneTimeWorkRequest locationWork = new OneTimeWorkRequest.Builder(FetchAddressWorker.class)
                .setInputData(myData)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(locationWork);
        //now set the observer to get the result.
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(locationWork.getId())
                .observe(this, new Observer<WorkInfo>() {
                    @Override
                    public void onChanged(@Nullable WorkInfo status) {
                        if (status != null && status.getState().isFinished()) {
                            String mAddressOutput = status.getOutputData().getString(Constants.RESULT_DATA_KEY);
                            Log.i(TAG, "address received: " + mAddressOutput);
                            logger.append(mAddressOutput);

                        }
                    }
                });
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        Log.v(TAG, "onRequest result called.");
        boolean coarse = false, fine = false;

        //received result for GPS access
        for (int i = 0; i < grantResults.length; i++) {
            if ((permissions[i].compareTo(Manifest.permission.ACCESS_COARSE_LOCATION) == 0) &&
                    (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                coarse = true;
            else if ((permissions[i].compareTo(Manifest.permission.ACCESS_FINE_LOCATION) == 0) &&
                    (grantResults[i] == PackageManager.PERMISSION_GRANTED))
                fine = true;
        }
        Log.v(TAG, "Received response for gps permission request.");
        // If request is cancelled, the result arrays are empty.
        if (coarse && fine) {
            // permission was granted
            Log.v(TAG, permissions[0] + " permission has now been granted. Showing preview.");
            Toast.makeText(this, "GPS access granted",
                    Toast.LENGTH_SHORT).show();
            if (requestCode == REQUEST_ACCESS_startLocationUpdates) {
                startLocationUpdates();
            } else if (requestCode == REQUEST_ACCESS_onConnected) {
                getLastLocation();
            }

        } else {
            // permission denied,    Disable this feature or close the app.
            Log.v(TAG, "GPS permission was NOT granted.");
            Toast.makeText(this, "GPS access NOT granted", Toast.LENGTH_SHORT).show();
            finish();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }


}
