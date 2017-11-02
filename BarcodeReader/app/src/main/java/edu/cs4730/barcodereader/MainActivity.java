package edu.cs4730.barcodereader;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
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

/*
  This is a simple example to using the barcode via the vision api's.
  Point the camera at a barcode and it will ask you if you want to search amazon for the barcode
  or open a web page (assumes the web address is correctly formed with a http://bah.com
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //get the views first.
        mPreview = (SurfaceView) findViewById(R.id.CameraView);
        //finally, setup the preview pieces
        mPreview.getHolder().addCallback(this);

        mLogger = (TextView) findViewById(R.id.logger);

        //handler to display a dialog about what to do with the barcode
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

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
    void startPreview() {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        //this is the quick and dirty version and it doesn't explain why we want permission.  Which is not how google wants us to do it.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc != PackageManager.PERMISSION_GRANTED) {
            if (!alreadyaskingpremission) {
                ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
                alreadyaskingpremission = true;
            }
            return;
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

    /*
     * So we have the result of the request for premissions, hopefully yes.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //because it's setup in oncreate and onresume, it's likely I'm asking twice very quickly.  the
        //alreadyaskingpremission vairable will stop the second request on top of the first request.
        alreadyaskingpremission = false;
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so start the preview.
            startPreview();
            return;
        }
        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
            " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));
        Toast.makeText(this, "Camera permission not granted, so exiting", Toast.LENGTH_LONG).show();
        finish();
    }

    //A simple implementation of the MultiProcessor factory.
    // appears to be necessary for the Detector.
    public class BarcodeTrackFactory implements MultiProcessor.Factory<Barcode> {

        @Override
        public Tracker<Barcode> create(Barcode barcode) {
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
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceAvailable = true;
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //should not be called, app is locked in portrait mode.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceAvailable = false;
    }
}
