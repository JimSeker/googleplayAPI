package edu.cs4730.actmapdemo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.Manifest;


import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // for checking permissions.
    ActivityResultLauncher<String[]> rpl_LocationUpdates, rpl_Activity, rpl_onConnected;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACTIVITY_RECOGNITION};


    static final LatLng LARAMIE = new LatLng(41.312928, -105.587253);
    String TAG = "MainActivity";
    ViewPager viewPager;
    myListFragment listfrag;
    myMapFragment mapfrag;

    int currentActivity = DetectedActivity.UNKNOWN;
    List<String> DataList = new ArrayList<String>();
    List<objData> objDataList = new ArrayList<objData>();

    float Milecheck = 5280.0f;
    float MileIncr = 5280.0f;

    //for location.
    Boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 1 * 1000;
    // Action fired when transitions are triggered.
    private final String ACTIVITY_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "ACTIVITY_RECEIVER_ACTION";
    private ActivityReceiver mActivityReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setup fragments
        listfrag = new myListFragment();
        mapfrag = new myMapFragment();
        mActivityReceiver = new ActivityReceiver();

        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager = findViewById(R.id.pager);
        myFragmentPagerAdapter adapter = new myFragmentPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);
        //new Tablayout from the support design library
        TabLayout mTabLayout = findViewById(R.id.tablayout1);
        mTabLayout.setupWithViewPager(viewPager);

       //permission pieces
        // for checking permissions.
        rpl_LocationUpdates = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        //We have permissions, so ...
                        startLocationUpdates();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );
        rpl_onConnected = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        //We have permissions, so ...
                        initialsetup();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );
        rpl_LocationUpdates = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        //We have permissions, so ...
                        setupActivityRec(true);
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

        //setup the location pieces.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        createLocationRequest();
        createLocationCallback();
        buildLocationSettingsRequest();
        initialsetup();  //get last location and call it current spot.
    }

    //menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @SuppressLint("MissingPermission")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_start) {
            //first reset everything (in case this is the 2+ time)
            mapfrag.clearmap();
            DataList.clear();
            objDataList.clear();
            //now do the stetup and start it.
            createLocationRequest();
            startLocationUpdates();
            CheckPermActivity();//            setupActivityRec(true);
            mRequestingLocationUpdates = true;
            return true;
        } else if (id == R.id.action_stop) {
            //this should never happen.
            if (!allPermissionsGranted()) {
                logthis("Requesting permissions");
                return false;
            }
            //add end marker
            mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location == null) {
                            Log.w(TAG, "onSuccess:null");
                            return;
                        }
                        Log.v(TAG, "getLastLocation");
                        mapfrag.finishMap(new objData(
                            location.getLatitude(),
                            location.getLongitude(),
                            location.getTime(),
                            currentActivity
                        ));
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getLastLocation:onFailure", e);
                    }
                });


            stopLocationUpdates();
            setupActivityRec(false);
            mRequestingLocationUpdates = false;
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            CheckPermActivity();
            startLocationUpdates();
        }

    }

    //on start setup the receiver
    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(mActivityReceiver, new IntentFilter(ACTIVITY_RECEIVER_ACTION));
    }

    //onstop turn off updates and remove the receiver.
    @Override
    protected void onStop() {
        Log.wtf(TAG, "onStop called.");
        // Unregister the broadcast receiver that was registered during onStart().
        unregisterReceiver(mActivityReceiver);
        super.onStop();
    }


    //ask for permissions when we start, this is likely over kill, since I ask for this permission in other places as well.
    public void CheckPermActivity() {
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl_Activity.launch(REQUIRED_PERMISSIONS);
            return;
        }
        //We have permissions, so ...
        setupActivityRec(true);
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest.Builder(1000)  //create a request with 10000 interval and default rest.
            .setMinUpdateIntervalMillis(500)  //get an update no faster then 5 seconds.
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setWaitForAccurateLocation(true)  //waits a couple of second initially for a accurate measurement.
            .setMaxUpdateDelayMillis(200)  //wait only 20 seconds max between
            .build();
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

    /**
     * Creates a callback for receiving location events.
     */
    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                addData(locationResult.getLastLocation());
            }
        };
    }


    //two methods to start and stop location updates.
    protected void startLocationUpdates() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl_LocationUpdates.launch(REQUIRED_PERMISSIONS);
            return;
        }
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                    Log.i(TAG, "All location settings are satisfied.");

                    //noinspection MissingPermission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                        mLocationCallback, Looper.myLooper());

                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mRequestingLocationUpdates = false;
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "Location settings are not satisfied. ");
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


    //method to start or stop the activity recognition.
    @SuppressLint("MissingPermission")
    //already checked.
    void setupActivityRec(boolean gettingupdates) {

        if (gettingupdates) { //true to start it.
            ActivityRecognition.getClient(this).requestActivityUpdates(
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.v(TAG, "starting ActLocation");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mRequestingLocationUpdates = false;
                        Log.w(TAG, "Failed to enable activity updates");
                    }
                });

        } else {
            ActivityRecognition.getClient(this).removeActivityUpdates(
                    getActivityDetectionPendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.w(TAG, "disabled activity recognition.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Failed to disable activity recognition.");
                    }
                });
        }
    }


    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(ACTIVITY_RECEIVER_ACTION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            return PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }


    @SuppressLint("MissingPermission")
    public void initialsetup() {
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
                        Log.w(TAG, "onSuccess:null");
                        return;
                    }
                    Log.v(TAG, "getLastLocation");
                    mapfrag.setupInitialloc(location.getLatitude(), location.getLongitude());
                    //initial spot maybe?
                    if (mRequestingLocationUpdates)
                        addData(location);
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w(TAG, "getLastLocation:onFailure", e);
                }
            });

    }


    public void addData(Location mlocation) {
        objData newData;


        if (mlocation != null) {

            newData = new objData(
                mlocation.getLatitude(),
                mlocation.getLongitude(),
                mlocation.getTime(),
                currentActivity
            );

            //figure distance info.
            if (objDataList.isEmpty()) {
                newData.distance = 0.0f;
            } else {
                newData.distance = distanceBetween(objDataList.get(objDataList.size() - 1).myLatlng, newData.myLatlng) * 3.28f; //converted to feet
                newData.distance += objDataList.get(objDataList.size() - 1).distance;  //previous distance, to ge the total.
            }
            //add everything and add to the data structures.
            objDataList.add(newData);
            listfrag.updateAdatper(objDataList);
            mapfrag.updateMapDraw(newData);
            if (newData.distance >= Milecheck) {
                mapfrag.mileMarker(newData, Milecheck / 5280 + " Miles");
                Milecheck += MileIncr;
            }
        }
    }

    private float distanceBetween(LatLng latLng1, LatLng latLng2) {

        Location loc1 = new Location(LocationManager.GPS_PROVIDER);
        Location loc2 = new Location(LocationManager.GPS_PROVIDER);

        loc1.setLatitude(latLng1.latitude);
        loc1.setLongitude(latLng1.longitude);

        loc2.setLatitude(latLng2.latitude);
        loc2.setLongitude(latLng2.longitude);


        return loc1.distanceTo(loc2);
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
        //logger.append(item + "\n");
    }

    //view page for the two fragments map and list.
    public class myFragmentPagerAdapter extends FragmentPagerAdapter {
        int PAGE_COUNT = 2;

        //required constructor that simply supers.
        myFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // return the correct fragment based on where in pager we are.
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return mapfrag;
                case 1:
                    return listfrag;
                default:
                    return null;
            }
        }

        //how many total pages in the viewpager there are.  3 in this case.
        @Override
        public int getCount() {

            return PAGE_COUNT;
        }

        //getPageTitle required for the PageStripe to work and have a value.
        @Override
        public CharSequence getPageTitle(int position) {

            switch (position) {
                case 0:
                    return "Map";
                case 1:
                    return "List";
                default:
                    return null;
            }
            //return String.valueOf(position);  //returns string of position for title
        }
    }


    /**
     * Handles intents from from the Activity Updates API.
     */
    public class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            logthis("onReceive(): " + intent);
            if (!TextUtils.equals(ACTIVITY_RECEIVER_ACTION, intent.getAction())) {
                logthis("Received an unsupported action in TransitionsReceiver: action = " +
                    intent.getAction());
                return;
            }

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            //get most probable activity
            DetectedActivity probably = result.getMostProbableActivity();
            currentActivity = probably.getType();
        }
    }
}
