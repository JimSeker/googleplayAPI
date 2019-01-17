package edu.cs4730.actmapdemo;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // for checking permissions.
    public static final int REQUEST_ACCESS_startLocationUpdates = 0;
    public static final int REQUEST_ACCESS_onConnected = 1;
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

    //for the activity rec
    private ActivityRecognitionClient mActivityRecognitionClient;
    //for location.
    Boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //setup fragments
        listfrag = new myListFragment();
        mapfrag = new myMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();

        viewPager = (ViewPager) findViewById(R.id.pager);
        myFragmentPagerAdapter adapter = new myFragmentPagerAdapter(fragmentManager);
        viewPager.setAdapter(adapter);
        //viewPager.setCurrentItem(1);
        //new Tablayout from the support design library
        TabLayout mTabLayout = (TabLayout) findViewById(R.id.tablayout1);
        mTabLayout.setupWithViewPager(viewPager);

        //setup the client activity piece.
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        //setup the location pieces.
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        createLocationRequest();
        createLocationCallback();
        buildLocationSettingsRequest();
        initialsetup();  //get last location and call it current spot.
    }


    /**
     * The service will call the handler to send back information.
     **/
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            currentActivity = msg.arg1;
            Log.v(TAG, "handler, update activity");
            return true;
        }
    });

    //menu methods
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_start) {
            //first reset everything (in case this is the 2+ time)
            mapfrag.clearmap();
            DataList.clear();
            objDataList.clear();
            //now do the stetup and start it.
            createLocationRequest();
            startLocationUpdates();
            setupActivityRec(true);
            mRequestingLocationUpdates = true;
            return true;
        } else if (id == R.id.action_stop) {
            //this should never happen.
            //noinspection StatementWithEmptyBody
            if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                    (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                //I'm on not explaining why, just asking for permission.
                //we don't have permission here to do anything with location, but it is started.  So ... can't stop it... odd possibility.
            } else {
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


            }
            stopLocationUpdates();
            setupActivityRec(false);
            mRequestingLocationUpdates = false;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /*
        @Override
        protected void onStop() {
            setupActivityRec(false);
            stopLocationUpdates();

            super.onStop();
        }
    */
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
            setupActivityRec(true);
            startLocationUpdates();
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
    void setupActivityRec(boolean gettingupdates) {

        if (gettingupdates) { //true to start it.
            Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent());
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.v(TAG, "starting ActLocation");
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    mRequestingLocationUpdates = false;
                    Log.w(TAG, "Failed to enable activity updates");
                }
            });

        } else {
            Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                    getActivityDetectionPendingIntent());
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.w(TAG, "disabled activity recognition.");
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
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
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        Messenger messenger = new Messenger(handler);
        intent.putExtra("MESSENGER", messenger);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    public void initialsetup() {
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
            //Toast.makeText(this, "GPS access granted", Toast.LENGTH_SHORT).show();
            if (requestCode == REQUEST_ACCESS_startLocationUpdates) {
                startLocationUpdates();
            } else if (requestCode == REQUEST_ACCESS_onConnected) {
                initialsetup();
            }

        } else {
            // permission denied,    Disable this feature or close the app.
            Log.v(TAG, "GPS permission was NOT granted.");
            Toast.makeText(this, "GPS access NOT granted", Toast.LENGTH_SHORT).show();
            finish();
        }
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

}
