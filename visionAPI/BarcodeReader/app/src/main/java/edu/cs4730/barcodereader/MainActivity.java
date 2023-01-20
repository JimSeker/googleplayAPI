package edu.cs4730.barcodereader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.util.Map;

/**
 * This is a simple example to using the barcode via the vision api's.
 * Point the camera at a barcode and it will ask you if you want to search amazon for the barcode
 * or open a web page (assumes the web address is correctly formed with a http://bah.com
 * <p>
 * Please note, the https://developers.google.com/vision is deprecated.  the barcode still works
 * really well.  but google/android wants you to switch the ML Kit version, which doesn't seem to work
 * as well in recognizing bar codes.
 */

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {
    String TAG = "MainActivity";
    CameraSource mCameraSource;
    SurfaceView mPreview;
    TextView mLogger;
    private boolean mSurfaceAvailable;
    boolean alreadyaskingpremission = false;
    //for getting permissions to use the camara in API 23+
    final String[] permissions = new String[]{Manifest.permission.CAMERA};
    private static final int RC_HANDLE_CAMERA_PERM = 2;
    //handler, since the facetracker is on another thread.
    protected Handler handler;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ActivityResultLauncher<String[]> rpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get the views first.
        mPreview = findViewById(R.id.CameraView);
        //finally, setup the preview pieces
        mPreview.getHolder().addCallback(this);

        mLogger = findViewById(R.id.logger);

        //handler to display a dialog about what to do with the barcode
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                Bundle stuff = msg.getData();
                String bc = stuff.getString("barcode");
                mLogger.setText(bc);
                mLogger.invalidate();  //should not need this...
                //now start a dialog about web or amazon search.
                myDialogFragment myDialog = myDialogFragment.newInstance(bc);
                myDialog.show(getSupportFragmentManager(), null);
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
                    if (granted) startPreview();
                    // else finish();
                }
            }
        );

        createCameraSource();

    }

    //create the create source.  Once the permissions have been granted above/below.
    public void createCameraSource() {
        //Setup the BarCodeDetector
        Context context = getApplicationContext();
        BarcodeDetector detector = new BarcodeDetector.Builder(context)
            .build();

        //note the barcodeTrackerFactory is defined below and is very simple.
        detector.setProcessor(new MultiProcessor.Builder<>(new BarcodeTrackFactory()).build());

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

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the barcode detector to detect small barcodes
        // at long distances.
        CameraSource.Builder builder = new CameraSource.Builder(getApplicationContext(), detector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600, 1024)
            .setRequestedFps(15.0f);

        builder = builder.setAutoFocusEnabled(true);

        mCameraSource = builder.build();
    }

    //now that we have the camera source, we can actually show the preview picture to find the barcode
    @SuppressLint("MissingPermission")
    //I really am checking, but studio can't tell.
    void startPreview() {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (!allPermissionsGranted()) {
            return;  //permissions are asked elsewhere.  but the surface created, calls this at start and will crash otherwise.
                    //asking permissions twice causes one of them to say no, while waiting on the other.
        }

        if (mSurfaceAvailable && mCameraSource != null) {
            try {
                mCameraSource.start(mPreview.getHolder());
                Log.v("TAG", "started, I think");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.v(TAG, "preview failed.");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (!allPermissionsGranted()) {
            rpl.launch(REQUIRED_PERMISSIONS);
        } else
            startPreview();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (mCameraSource != null)
            mCameraSource.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
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


    //A simple implementation of the MultiProcessor factory.
    // appears to be necessary for the Detector.
    public class BarcodeTrackFactory implements MultiProcessor.Factory<Barcode> {

        @NonNull
        @Override
        public Tracker<Barcode> create(@NonNull Barcode barcode) {
            //BarCodeTracker is defined below
            return new BarCodeTracker();
        }

    }

    //again this is very simple.  once we find a barcode, ask the user what do with it.
    //I only care about finding a barcode, so the other methods are not implemented.
    class BarCodeTracker extends Tracker<Barcode> {
        @Override
        public void onNewItem(int id, Barcode barcode) {
            sendmessage(barcode.displayValue);
            if (barcode.format == Barcode.QR_CODE) {
                Log.v(TAG, "www:" + barcode.displayValue);
            } else {
                Log.v(TAG, "Other: " + barcode.displayValue);
            }
        }
    }

    /*
    send a message back to main thread so see what the user wants to do
    */
    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("barcode", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;

        msg.what = 1;  //so the empty message is not used!
        // System.out.println("About to Send message"+ logthis);
        handler.sendMessage(msg);
        // System.out.println("Sent message"+ logthis);
    }

    /*
     *  methods needed for the surfaceView callback methods.
     */
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        mSurfaceAvailable = true;
        startPreview();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        //should not be called, app is locked in portrait mode.
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        mSurfaceAvailable = false;
    }
}
