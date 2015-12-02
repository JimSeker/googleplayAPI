package edu.cs4730.actmapdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ResultCallback<Status> {

    String TAG = "MainActivity";
    ViewPager viewPager;
    myListFragment listfrag;
    myMapFragment mapfrag;

    int currentActivity = DetectedActivity.UNKNOWN;
    List<String> DataList = new ArrayList<String>();
    List<objData> objDataList = new ArrayList<objData>();

    //for the location.
    GoogleApiClient mGoogleApiClient;
    Boolean mRequestingLocationUpdates = false;
    LocationRequest mLocationRequest;
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

        buildGoogleApiClient();
    }


    protected synchronized void buildGoogleApiClient() {
        Log.v(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(ActivityRecognition.API)
                .build();

    }

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
            //add end marker
            Location mlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mapfrag.finishMap(new objData(
                    mlocation.getLatitude(),
                    mlocation.getLongitude(),
                    mlocation.getTime(),
                    currentActivity
            ));
            stopLocationUpdates();
            setupActivityRec(false);
            mRequestingLocationUpdates = false;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        setupActivityRec(false);
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
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

    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    void setupActivityRec(boolean gettingupdates) {
        if (!mGoogleApiClient.isConnected()) {
            Log.v(TAG, "GoogleAPIclient is not connected, ActRec issues.");
            return;
        }
        if (gettingupdates) { //we are already getting updates, so stop
            ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,
                    DETECTION_INTERVAL_IN_MILLISECONDS,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
            Log.v(TAG, "starting ActLocation");
        } else {
            ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                    mGoogleApiClient,
                    getActivityDetectionPendingIntent()
            ).setResultCallback(this);
        }
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    //we want to get the intent back here without starting another instanced
    //so  we get the data here, hopefully.
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent");

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        //get most probable activity
        DetectedActivity probably = result.getMostProbableActivity();
        if (probably.getConfidence() >= 50) {  //doc's say over 50% is likely, under is not sure at all.
           currentActivity = probably.getType();
        }
    }
    /**
     * Returns a human readable String corresponding to a detected activity type.
     */

    public static String getActivityString(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In a Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On a bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still (not moving)";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown Activity";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown Type";
        }
    }
    //GoogleApiCloient call back methods
    @Override
    public void onConnected(Bundle bundle) {

        Log.v(TAG, "onConnected");
        Location mlocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        mapfrag.setupInitialloc(mlocation.getLatitude(), mlocation.getLongitude());
        //initial spot maybe?
        if (mRequestingLocationUpdates)
          addData(mlocation);

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "onConnectionSuspected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed");
        mRequestingLocationUpdates = false;
    }
    //resultcallback ... not sure if need this or not.
    @Override
    public void onResult(Status status) {
        Log.v(TAG, "onResult");
        if (status.isSuccess()) {
            Log.v(TAG, "onResult success");
        } else {
            Log.v(TAG, "onResult failed. " + status.getStatusMessage());
        }
    }

    //location listener methods
    @Override
    public void onLocationChanged(Location location) {
        addData(location);
    }

    public void addData(Location mlocation) {
        if (mlocation != null) {
            DataList.add(
                    DateFormat.getTimeInstance().format(new Date()) + ": "
                    + String.valueOf(mlocation.getLatitude())  +" "
                    + String.valueOf(mlocation.getLongitude())
            );
            objDataList.add(new objData(
                    mlocation.getLatitude(),
                    mlocation.getLongitude(),
                    mlocation.getTime(),
                    currentActivity
            ));

            listfrag.updateAdatper(DataList.toArray(new String[DataList.size()]));
            mapfrag.updateMapDraw(new objData(
                    mlocation.getLatitude(),
                    mlocation.getLongitude(),
                    mlocation.getTime(),
                    currentActivity
            ));
        }
    }
    //view page for the two fragments map and list.
    public class myFragmentPagerAdapter extends FragmentPagerAdapter {
        int PAGE_COUNT =2;

        //required constructor that simply supers.
        public myFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        // return the correct fragment based on where in pager we are.
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0: return mapfrag;
                case 1: return listfrag;
                default: return null;
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
                case 0: return "Map";
                case 1: return "List";
                default: return null;
            }
            //return String.valueOf(position);  //returns string of position for title


        }

    }

}
