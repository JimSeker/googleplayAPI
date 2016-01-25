package edu.cs4730.locationawaredemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormat;
import java.util.Date;

/*
  * https://github.com/googlesamples/android-play-location/tree/master/LocationAddress/app/src/main
  * http://developer.android.com/training/location/index.html
  *
  * https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition
  *
  * This shows how to get location updates, either the last known and continuing.
  * Also uses the intent service to get address locations as well.
  *
 */


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    // for checking permissions.
    public static final int REQUEST_ACCESS_startLocationUpdates = 0;
    public static final int REQUEST_ACCESS_onConnected = 1;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    TextView logger;
    Button btn;
    Boolean mRequestingLocationUpdates = false;

    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        logger = (TextView) findViewById(R.id.logger);
        btn = (Button) findViewById(R.id.button);
        btn.setText("Start location updates");
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestingLocationUpdates = !mRequestingLocationUpdates;
                locationUpdates();
            }
        });
        mResultReceiver = new AddressResultReceiver(new Handler());
        buildGoogleApiClient();
        Log.v(TAG, "starting");
    }

    protected synchronized void buildGoogleApiClient() {
        Log.v(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        // createLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
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
            startLocationUpdates();
        }
    }

    public void locationUpdates() {
        if (mRequestingLocationUpdates) {
            //true, so start them
            createLocationRequest();
            startLocationUpdates();
            btn.setText("Stop location updates");
        } else {
            //false, so stop them.
            stopLocationUpdates();
            btn.setText("Start location updates");
        }

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
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
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onConnected(Bundle bundle) {

        getLastLocation();

    }

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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        Log.v(TAG, "onConnected");
        if (mLastLocation != null) {
            logger.append("Last location: ");
            logger.append(" Lat: " + String.valueOf(mLastLocation.getLatitude()));
            logger.append(" Long: " + String.valueOf(mLastLocation.getLongitude()) + "\n");
            startIntentService();
        }
    }

    /**
     * Receiver registered with this activity to get the response from FetchAddressIntentService.
     */
    private AddressResultReceiver mResultReceiver;

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            String mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            logger.append(mAddressOutput);

        }
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, FetchAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mResultReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    //location listener methods
    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "onConnectionSuspected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed");
        mRequestingLocationUpdates = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        logger.append(DateFormat.getTimeInstance().format(new Date()) + ": ");
        logger.append(" Lat: " + String.valueOf(location.getLatitude()));
        logger.append(" Long: " + String.valueOf(location.getLongitude()) + "\n");
        mLastLocation = location;
        startIntentService();
    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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



    }


}
