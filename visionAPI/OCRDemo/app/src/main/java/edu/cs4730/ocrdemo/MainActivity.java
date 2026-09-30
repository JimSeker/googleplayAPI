package edu.cs4730.ocrdemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;


import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.util.Map;

import edu.cs4730.ocrdemo.databinding.ActivityMainBinding;

/**
 * This is a simpler example of the text detector then android example code.  It's also in line with the rest of my examples.
 * <p>
 * This does in a gesture detector so you can tap the text and it will show up in the logger and at the top of the screen.
 *
 */

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "OCRDemo";

    private CameraSource mCameraSource;
    ActivityMainBinding binding;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;

    //for getting permissions to use the Camara in API 23+
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ActivityResultLauncher<String[]> rpl;
    private GestureDetector gestureDetector;

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

        //this is an odd one with the cast,but it is correct.  don't fix it.
        mGraphicOverlay = (GraphicOverlay<OcrGraphic>) binding.faceOverlay;
        gestureDetector = new GestureDetector(this, new myGestureListener());


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
        // A text recognizer is created to find text.  An associated processor instance
        // is set to receive the text recognition results and display graphics for each text block
        // on screen.
        TextRecognizer detector = new TextRecognizer.Builder(context).build();
        detector.setProcessor(new OcrDetectorProcessor(mGraphicOverlay));

        if (!detector.isOperational()) {
            // Note: The first time that an app using face API is installed on a device, GMS will
            // download a native library to the device in order to do detection.  Usually this
            // completes before the app is run for the first time.  But if that download has not yet
            // completed, then the above call will not detect any faces.
            //
            // isOperational() can be used to check if the required native library is currently
            // available.  The detector will automatically become operational once the library
            // download completes on device.
            Log.w(TAG, "text detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
            .setRequestedPreviewSize(640, 480)
            //.setFacing(CameraSource.CAMERA_FACING_FRONT)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setAutoFocusEnabled(true)
            .setRequestedFps(2.0f)
            .build();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return gestureDetector.onTouchEvent(e) || super.onTouchEvent(e);
    }

    private class myGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            float rawX = e.getRawX(), rawY = e.getRawY();
            OcrGraphic graphic = mGraphicOverlay.getGraphicAtLocation(rawX, rawY);
            TextBlock text = null;
            if (graphic != null) {
                text = graphic.getTextBlock();
                if (text != null && text.getValue() != null) {
                    logthis(text.getValue());
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * Restarts the camera.
     */
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

    void logthis(String item) {
        Log.d(TAG, "text block is" + item);
        binding.logger.setText(item);
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
            binding.CameraView.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            Log.e(TAG, "Unable to start camera source.", e);
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    //==============================================================================================
    // Text Tracker
    //==============================================================================================

    /**
     * A very simple Processor which receives detected TextBlocks and adds them to the overlay
     * as OcrGraphics.
     */
    public class OcrDetectorProcessor implements Detector.Processor<TextBlock> {

        private final GraphicOverlay<OcrGraphic> mGraphicOverlay;

        OcrDetectorProcessor(GraphicOverlay<OcrGraphic> ocrGraphicOverlay) {
            mGraphicOverlay = ocrGraphicOverlay;
        }

        /**
         * Called by the detector to deliver detection results.
         * If your application called for it, this could be a place to check for
         * equivalent detections by tracking TextBlocks that are similar in location and content from
         * previous frames, or reduce noise by eliminating TextBlocks that have not persisted through
         * multiple detections.
         */
        @Override
        public void receiveDetections(Detector.Detections<TextBlock> detections) {
            mGraphicOverlay.clear();
            SparseArray<TextBlock> items = detections.getDetectedItems();
            for (int i = 0; i < items.size(); ++i) {
                TextBlock item = items.valueAt(i);
                OcrGraphic graphic = new OcrGraphic(mGraphicOverlay, item);
                mGraphicOverlay.add(graphic);
            }
        }

        /**
         * Frees the resources associated with this detection processor.
         */
        @Override
        public void release() {
            mGraphicOverlay.clear();
        }
    }
}
