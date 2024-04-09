package edu.cs4730.fusedorienationdemo;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.DeviceOrientation;
import com.google.android.gms.location.DeviceOrientationListener;
import com.google.android.gms.location.DeviceOrientationRequest;
import com.google.android.gms.location.FusedOrientationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.cs4730.fusedorienationdemo.databinding.ActivityMainBinding;

/**
 * A simple demo of the FusedOrientation APIs in google play.
 */

public class MainActivity extends AppCompatActivity {

    private FusedOrientationProviderClient mFusedOrientationProviderClient;
    private DeviceOrientationListener mDeviceOrientationListener;
    private DeviceOrientationRequest request;
    private ExecutorService executors;
    private static final String TAG = "MainActivity";
    int rotation;
    private final float[] prefValues = new float[3];
    float[] rotationMatrix = new float[9];
    float mAzimuth;
    double mPitch;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mFusedOrientationProviderClient = LocationServices.getFusedOrientationProviderClient(getApplicationContext());
        mDeviceOrientationListener = new DeviceOrientationListener() {
            @Override
            public void onDeviceOrientationChanged(@NonNull DeviceOrientation deviceOrientation) {
                String msg = "nothing";

                rotation = rotationInfo();
                if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) { //holding it portrait mode
                    msg = "Y axis (0 to 360 degrees): " + deviceOrientation.getHeadingDegrees();
                } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) { //holding it landscape
                    msg = "X axis (0 to 360 degrees): " + deviceOrientation.getHeadingDegrees();
                }
                binding.logger.setText(msg);

                //math to convert the information to useful information.
                SensorManager.getRotationMatrixFromVector(rotationMatrix, deviceOrientation.getAttitude());
                // got a good rotation matrix
                SensorManager.getOrientation(rotationMatrix, prefValues);
                mAzimuth = (float) Math.toDegrees(prefValues[0]);
                if (mAzimuth < 0) {
                    mAzimuth += 360.0f;
                }
                //mPitch = 180.0  + Math.toDegrees(prefValues[1]); //so it goes from 0 to 360, instead of -180 to 180
                mPitch = Math.toDegrees(prefValues[1]);
                msg = String.format(Locale.getDefault(),
                    "Preferred:\nazimuth (Z): %7.3f \npitch (X): %7.3f\nroll (Y): %7.3f",
                    mAzimuth, //heading
                    mPitch,
                    Math.toDegrees(prefValues[2]));
                binding.preferred.setText(msg);
            }
        };
        rotation = rotationInfo();
        executors = Executors.newSingleThreadExecutor();
        request = new DeviceOrientationRequest.Builder(DeviceOrientationRequest.OUTPUT_PERIOD_DEFAULT).build();
    }

    public int rotationInfo() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            return getDisplay().getRotation();
        } else {

            return getWindowManager().getDefaultDisplay().getRotation();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        mFusedOrientationProviderClient.requestOrientationUpdates(
            request, executors, mDeviceOrientationListener
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                logthis("Fused orientation registered success");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis(e.getMessage());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        mFusedOrientationProviderClient.removeOrientationUpdates(mDeviceOrientationListener);
    }

    void logthis(String item) {
        Log.d(TAG, item);
    }
}