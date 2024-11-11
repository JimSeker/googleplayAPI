package edu.cs4730.locationawaredemo_kt

import android.Manifest
import android.annotation.SuppressLint
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.android.gms.tasks.OnSuccessListener
import edu.cs4730.locationawaredemo_kt.databinding.ActivityMainBinding
import java.text.DateFormat
import java.util.Date

/**
 * https://github.com/googlesamples/android-play-location/tree/master/LocationAddress/app/src/main
 * http://developer.android.com/training/location/index.html
 * <p>
 * https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition
 * <p>
 * This shows how to get location updates, either the last known and continuing.
 * getLastLocation is get the last known location  and startLocationUpdates() is continuing.
 * Also uses the worker to get address locations as well.
 * <p>
 * use this for the location aware parts, not the android.location.
 * https://developers.google.com/android/reference/com/google/android/gms/location/package-summary
 */


class MainActivity() : AppCompatActivity() {
    // for checking permissions.
    private lateinit var rpl_onConnected: ActivityResultLauncher<Array<String>>
    private lateinit var rpl_startLocationUpdates: ActivityResultLauncher<Array<String>>
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
    )
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    var mLastLocation: Location? = null
    lateinit var mLocationRequest: LocationRequest
    lateinit var binding: ActivityMainBinding
    var mRequestingLocationUpdates = false

    /**
     * Provides access to the Location Settings API.
     */
    private lateinit var mSettingsClient: SettingsClient

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private lateinit var mLocationSettingsRequest: LocationSettingsRequest

    /**
     * Callback for Location events.
     */
    private lateinit var mLocationCallback: LocationCallback

    var TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(binding.main.id)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        setSupportActionBar(binding.toolbar)

        // for checking permissions.
        rpl_onConnected = registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (allPermissionsGranted()) {
                getLastLocation()
            } else {
                Toast.makeText(
                    applicationContext, "Permissions not granted by the user.", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        // for checking permissions.
        rpl_startLocationUpdates = registerForActivityResult<Array<String>, Map<String, Boolean>>(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (allPermissionsGranted()) {
                startLocationUpdates()
            } else {
                Toast.makeText(
                    applicationContext, "Permissions not granted by the user.", Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
        binding.button.text = "Start location updates"
        binding.button.setOnClickListener {
            mRequestingLocationUpdates = !mRequestingLocationUpdates
            locationUpdates()
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        createLocationRequest()
        createLocationCallback()
        buildLocationSettingsRequest()
        Log.v(TAG, "starting")
        getLastLocation()
    }

    /**
     * Uses a LocationSettingsRequest.Builder to build a LocationSettingsRequest that is used
     * for checking if a device has the needed location settings.
     */
    private fun buildLocationSettingsRequest() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
    }

    //to save te battery pasue requests
    override fun onPause() {
        super.onPause()
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        stopLocationUpdates()
    }

    //and when we come back, start them again, if were started before onPause.
    public override fun onResume() {
        super.onResume()
        if (mRequestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    fun locationUpdates() {
        if (mRequestingLocationUpdates) {
            //true, so start them
            startLocationUpdates()
            binding.button.text = "Stop location updates"
        } else {
            //false, so stop them.
            stopLocationUpdates()
            binding.button.text = "Start location updates"
        }
    }

    /**
     * helper function to create a locationRequest for the variable. otherwise, this could be
     * done in onCreate.
     */
    protected fun createLocationRequest() {
        mLocationRequest =
            LocationRequest.Builder(100000) //create a request with 10000 interval and default rest.
                //now set the rest of the pieces we want to change.
                //.setIntervalMillis(10000)  //not needed, since it is part of the builder.
                .setMinUpdateIntervalMillis(50000) //get an update no faster then 5 seconds.
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setWaitForAccurateLocation(true) //waits a couple of second initially for a accurate measurement.
                .setMaxUpdateDelayMillis(20000) //wait only 20 seconds max between
                .build()
    }

    /**
     * Creates a callback for receiving location events.  again a helper function, this could
     * also be done in onCreate.
     */
    private fun createLocationCallback() {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                mLastLocation = locationResult.lastLocation!!
                logthis(
                    "\n\n" + DateFormat.getTimeInstance()
                        .format(Date()) + ": " + " Lat: " + mLastLocation!!.latitude + " Long: " + mLastLocation!!.longitude
                )
                startWorker()
            }
        }
    }

    /**
     * If we have all the permissions, then it will create the location updates
     */
    @SuppressLint("MissingPermission") //I'm really checking, but studio can't tell.
    protected fun startLocationUpdates() {
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions")
            rpl_startLocationUpdates.launch(REQUIRED_PERMISSIONS)
            return
        }
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnSuccessListener(this) {
                logthis("All location settings are satisfied, starting locationUpdates.")
                mFusedLocationClient.requestLocationUpdates(
                    mLocationRequest, mLocationCallback, Looper.myLooper()
                )
            }.addOnFailureListener(this) { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        logthis("Location settings are not satisfied. Attempting to upgrade location settings ")
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                this@MainActivity, REQUEST_CHECK_SETTINGS
                            )
                        } catch (sie: SendIntentException) {
                            logthis("PendingIntent unable to execute request.")
                        }
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        logthis("Location settings are inadequate, and cannot be fixed here. Fix in Settings.")
                        mRequestingLocationUpdates = false
                    }
                }
            }
    }

    /**
     * this will stop locationUpdates.
     */
    protected fun stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            logthis("stopLocationUpdates: updates never requested, no-op.")
            return
        }
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback).addOnCompleteListener(
                this
            ) { mRequestingLocationUpdates = false }
    }

    /**
     * This shows how to get a "one off" location.  instead of using the location updates shown in
     * above the methods.
     */
    @SuppressLint("MissingPermission") //I'm really checking, but studio can't tell.
    fun getLastLocation() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions")
            rpl_onConnected.launch(REQUIRED_PERMISSIONS)
            return
        }
        mFusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener { location ->
                if (location == null) {
                    logthis("Last location: null")
                    return@OnSuccessListener
                }
                mLastLocation = location
                Log.v(TAG, "getLastLocation")
                if (mLastLocation != null) {
                    logthis(
                        ("Last location: " + " Lat: " + mLastLocation!!.latitude + " Long: " + mLastLocation!!.longitude)
                    )
                    startWorker()
                }
            }).addOnFailureListener(this) { e ->
                Log.w(TAG, "getLastLocation:onFailure", e)
                binding.logger.append("Last location: Fail")
            }
    }

    /**
     * Creates an Worker, adds location data to it as an extra, and starts worker for
     * fetching an address.  It then sets an observer to get the data back and displayed
     */
    fun startWorker() {
        val myData = Data.Builder().putDouble(Constants.LATITUDE, mLastLocation!!.latitude)
            .putDouble(Constants.LONGITUDE, mLastLocation!!.longitude).build()
        val locationWork: OneTimeWorkRequest =
            OneTimeWorkRequest.Builder(FetchAddressWorker::class.java).setInputData(myData).build()
        WorkManager.getInstance(applicationContext).enqueue(locationWork)
        //now set the observer to get the result.
        WorkManager.getInstance(applicationContext).getWorkInfoByIdLiveData(locationWork.id)
            .observe(this) { status ->
                if (status != null && status.state.isFinished) {
                    val mAddressOutput = status.outputData.getString(Constants.RESULT_DATA_KEY)
                    logthis("address received: $mAddressOutput")
                }
            }
    }

    //helper function to check if all the permissions are granted.
    private fun allPermissionsGranted(): Boolean {
        for (permission: String in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                    this, permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    //help function to print the screen and log it.
    fun logthis(item: String) {
        Log.d(TAG, item)
        binding.logger.append(item + "\n")
    }

    companion object {
        // Constant used in the location settings dialog.
        private const val REQUEST_CHECK_SETTINGS = 0x1
    }
}
