package edu.cs4730.ocrdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

/**
 * This is a simplier example of the text detector then android example code.  It's also in line with the rest of my examples.
 *
 * This does in a guesture dectector so you can tap the text and it will show up in the logger and at the top of the screen.
 *
 */



public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FaceTracker";

    private CameraSource mCameraSource;
    private CameraSourcePreview mPreview;
    private GraphicOverlay<OcrGraphic> mGraphicOverlay;
    TextView mLogger;
    //for getting permissions to use the camara in API 23+
    final String[] permissions = new String[]{Manifest.permission.CAMERA};
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogger = findViewById(R.id.mylogger);
        mPreview =  findViewById(R.id.CameraView);
        mGraphicOverlay = findViewById(R.id.faceOverlay);
        gestureDetector = new GestureDetector(this, new myGestureListener());

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
        return  gestureDetector.onTouchEvent(e)|| super.onTouchEvent(e);
    }
    private class myGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

           float rawX = e.getRawX(), rawY= e.getRawY();
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
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mPreview.stop();
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
        mLogger.setText(item);
    }
    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            startCameraSource();
            return;
        }
        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
            " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

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
        //this is the quick and dirty version and it doesn't explain why we want permission.  Which is not how google wants us to do it.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        try {
            mPreview.start(mCameraSource, mGraphicOverlay);
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

        private GraphicOverlay<OcrGraphic> mGraphicOverlay;

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
