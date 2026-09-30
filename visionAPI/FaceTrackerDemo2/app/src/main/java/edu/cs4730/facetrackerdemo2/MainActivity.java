package edu.cs4730.facetrackerdemo2;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.Map;

import edu.cs4730.facetrackerdemo2.databinding.ActivityMainBinding;

/**
 * This example uses the face tracker (only one face though), so show if the eyes are open and
 * the face is smiling.  It needs a graphic overloay, which camerapreview to do the grpahics overlay.
 * the cameraSourcePreview and GraphicOverlay is googles code, unchanged.
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource;
    ActivityMainBinding binding;
    //for getting permissions to use the camara in API 23+
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ActivityResultLauncher<String[]> rpl;
    //handler, since the facetracker is on another thread.
    protected Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        //message handler for textivew.
        handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                Bundle stuff = msg.getData();
                binding.logger.setText(stuff.getString("logthis"));
                binding.logger.invalidate();  //should not need this...
                return true;
            }
        });

        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet())
                        if (!x.getValue()) granted = false;
                    if (granted) startCameraSource();
                    // else finish();
                }
            }
        );

        createCameraSource();

    }

    public void createCameraSource() {
        Context context = getApplicationContext();
        FaceDetector detector = new FaceDetector.Builder(context)
            .setProminentFaceOnly(true)   //track only one face... makes it faster.
            // .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)  //allows for eye and smile detection!
            .setClassificationType(FaceDetector.ALL_LANDMARKS)  //allows for eye and smile detection!
            .build();

        detector.setProcessor(
            //new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory()).build());
            new LargestFaceFocusingProcessor(detector, new GraphicFaceTracker(binding.faceOverlay)));

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setRequestedFps(30.0f)
            .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS);
        } else
            startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        binding.CameraView.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null)
            mCameraSource.release();

    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (!allPermissionsGranted()) {
            return;  //permissions are asked elsewhere.  but the surface created, calls this at start and will crash otherwise.
            //asking permissions twice causes one of them to say no, while waiting on the other.
        }
        try {
            binding.CameraView.start(mCameraSource, binding.faceOverlay);
        } catch (IOException e) {
            Log.e(TAG, "Unable to start camera source.", e);
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @NonNull
        @Override
        public Tracker<Face> create(@NonNull Face face) {
            return new GraphicFaceTracker(binding.faceOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private final GraphicOverlay mOverlay;
        private final FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, handler);
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, @NonNull Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(@NonNull FaceDetector.Detections<Face> detectionResults, @NonNull Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(@NonNull FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}