package edu.cs4730.locationawaredemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Map;

/**
 * https://github.com/googlesamples/android-play-location/tree/master/LocationAddress/app/src/main
 * http://developer.android.com/training/location/index.html
 *
 * https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition
 *
 * This shows how to get location updates, either the last known and continuing.
 * getLastLocation is get the last known location  and startLocationUpdates() is continuing.
 * Also uses the worker to get address locations as well.
 *
 * use this for the location aware parts, not the android.location.
 * https://developers.google.com/android/reference/com/google/android/gms/location/package-summary
 */


public class MainActivity extends AppCompatActivity {

    // Constant used in the location settings dialog.
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    // for checking permissions.
    ActivityResultLauncher<String[]> rpl_onConnected, rpl_startLocationUpdates;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

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

        // for checking permissions.
        rpl_onConnected = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {

                        getLastLocation();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );
        // for checking permissions.
        rpl_startLocationUpdates = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        startLocationUpdates();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

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
     * Uses a LocationSettingsRequest.Builder to build a LocationSettingsRequest that is used
     * for checking if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    //to save te battery pasue requests
    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        stopLocationUpdates();
    }

    //and when we come back, start them again, if were started before onPause.
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

    /**
     * helper function to create a locationRequest for the variable. otherwise, this could be
     * done in onCreate.
     */
    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest.Builder(100000)  //create a request with 10000 interval and default rest.
            //now set the rest of the pieces we want to change.
            //.setIntervalMillis(10000)  //not needed, since it is part of the builder.
            .setMinUpdateIntervalMillis(50000)  //get an update no faster then 5 seconds.
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(true)  //waits a couple of second initially for a accurate measurement.
            .setMaxUpdateDelayMillis(20000)  //wait only 20 seconds max between
            .build();
    }

    /**
     * Creates a callback for receiving location events.  again a helper function, this could
     * also be done in onCreate.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                mLastLocation = locationResult.getLastLocation();
                logthis("\n\n" + DateFormat.getTimeInstance().format(new Date()) + ": " +
                    " Lat: " + mLastLocation.getLatitude() +
                    " Long: " + mLastLocation.getLongitude());
                startWorker();
            }
        };
    }

    /**
     * If we have all the permissions, then it will create the location updates
     */
    @SuppressLint("MissingPermission")  //I'm really checking, but studio can't tell.
    protected void startLocationUpdates() {
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl_startLocationUpdates.launch(REQUIRED_PERMISSIONS);
            return;
        }
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @Override
                public void onSuccess(@NonNull LocationSettingsResponse locationSettingsResponse) {
                    logthis("All location settings are satisfied, starting locationUpdates.");
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
                            logthis("Location settings are not satisfied. Attempting to upgrade location settings ");
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException sie) {
                                logthis("PendingIntent unable to execute request.");
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            logthis("Location settings are inadequate, and cannot be fixed here. Fix in Settings.");
                            mRequestingLocationUpdates = false;
                    }
                }
            });
    }

    /**
     * this will stop locationUpdates.
     */
    protected void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            logthis("stopLocationUpdates: updates never requested, no-op.");
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

    /**
     *  This shows how to get a "one off" location.  instead of using the location updates shown in
     *  above the methods.
     */
    @SuppressLint("MissingPermission") //I'm really checking, but studio can't tell.
    public void getLastLocation() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl_onConnected.launch(REQUIRED_PERMISSIONS);
            return;
        }
        mFusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location == null) {
                        logthis("Last location: null");
                        return;
                    }
                    mLastLocation = location;

                    Log.v(TAG, "getLastLocation");
                    if (mLastLocation != null) {
                        logthis("Last location: " +
                            " Lat: " + mLastLocation.getLatitude() +
                            " Long: " + mLastLocation.getLongitude());
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
     * fetching an address.  It then sets an observer to get the data back and displayed
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
                        logthis("address received: " + mAddressOutput);
                    }
                }
            });
    }

    //helper function to check if all the permissions are granted.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //help function to print the screen and log it.
    void logthis(String item) {
        Log.d(TAG, item);
        logger.append(item + "\n");
    }
}
