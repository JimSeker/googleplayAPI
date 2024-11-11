package edu.cs4730.fusedorientationdemo_kt

import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.DeviceOrientationListener
import com.google.android.gms.location.DeviceOrientationRequest
import com.google.android.gms.location.FusedOrientationProviderClient
import com.google.android.gms.location.LocationServices
import edu.cs4730.fusedorientationdemo_kt.databinding.ActivityMainBinding
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * A simple demo of the FusedOrientation APIs in google play.
 */

class MainActivity : AppCompatActivity() {
    private lateinit var mFusedOrientationProviderClient: FusedOrientationProviderClient
    private lateinit var mDeviceOrientationListener: DeviceOrientationListener
    private lateinit var request: DeviceOrientationRequest
    private lateinit var executors: ExecutorService
    private val TAG = "MainActivity"
    private var rotation = 0
    private val prefValues = FloatArray(3)
    private var rotationMatrix = FloatArray(9)
    private var mAzimuth = 0f
    private var mPitch = 0.0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(binding.main.id)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        mFusedOrientationProviderClient = LocationServices.getFusedOrientationProviderClient(
            applicationContext
        )
        mDeviceOrientationListener = DeviceOrientationListener { deviceOrientation ->
            var msg = "nothing"
            rotation = rotationInfo()
            if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //holding it portrait mode
                msg = "Y axis (0 to 360 degrees): " + deviceOrientation.headingDegrees
            } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //holding it landscape
                msg = "X axis (0 to 360 degrees): " + deviceOrientation.headingDegrees
            }
            binding.logger.text = msg

            //math to convert the information to useful information.
            SensorManager.getRotationMatrixFromVector(
                rotationMatrix, deviceOrientation.attitude
            )
            // got a good rotation matrix
            SensorManager.getOrientation(rotationMatrix, prefValues)
            mAzimuth = Math.toDegrees(prefValues[0].toDouble()).toFloat()
            if (mAzimuth < 0) {
                mAzimuth += 360.0f
            }
            //mPitch = 180.0  + Math.toDegrees(prefValues[1]); //so it goes from 0 to 360, instead of -180 to 180
            mPitch = Math.toDegrees(prefValues[1].toDouble())
            msg = String.format(
                Locale.getDefault(),
                "Preferred:\nazimuth (Z): %7.3f \npitch (X): %7.3f\nroll (Y): %7.3f",
                mAzimuth,  //heading
                mPitch,
                Math.toDegrees(prefValues[2].toDouble())
            )
            binding.preferred.text = msg
        }

        rotation = rotationInfo()
        executors = Executors.newSingleThreadExecutor()
        request =
            DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build()

    }

    private fun rotationInfo(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            display!!.rotation
        } else {
            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.rotation
        }
    }

    override fun onStart() {
        super.onStart()
        mFusedOrientationProviderClient.requestOrientationUpdates(
            request, executors, mDeviceOrientationListener
        ).addOnSuccessListener { logthis("Fused orientation registered success") }
            .addOnFailureListener { e ->
                logthis(e.message!!)
            }
    }

    override fun onPause() {
        super.onPause()
        mFusedOrientationProviderClient.removeOrientationUpdates(mDeviceOrientationListener)
    }

    fun logthis(item: String) {
        Log.d(TAG, item)
    }


}