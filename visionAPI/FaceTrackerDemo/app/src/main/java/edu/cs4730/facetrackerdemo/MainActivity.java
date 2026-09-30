package edu.cs4730.facetrackerdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.SurfaceHolder;
import android.util.Log;

import androidx.activity.result.ActivityResult;
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
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.LargestFaceFocusingProcessor;

import java.io.IOException;
import java.util.Map;

import edu.cs4730.facetrackerdemo.databinding.ActivityMainBinding;

/**
 * very simple example using the facetracker.  it checks if the eyes are open and the person is
 * smiling.  It doesn't draw anything.  but speaks telling you too open eyes, smile etc.
 * <p>
 * note this api is deprecated.  the landmarks have failed (which is why it doesn't draw).  I fully
 * expect the rest ot fail at any time.  https://developers.google.com/vision
 */
public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, OnInitListener {

    String TAG = "MainActivity";
    CameraSource mCameraSource;
    ActivityMainBinding binding;
    private boolean mSurfaceAvailable;
    //handler, since the facetracker is on another thread.
    protected Handler handler;
    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA"};
    ActivityResultLauncher<String[]> rpl;
    //speech variables.
    private TextToSpeech mTts;
    private String myUtteranceId = "txt2spk";
    private boolean canspeak;

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

        // setup the preview pieces
        binding.CameraView.getHolder().addCallback(this);

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

        //using the new startActivityForResult method.
        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getResultCode() == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                        // TTS is up and running
                        mTts = new TextToSpeech(getApplicationContext(), MainActivity.this);
                        Log.v(TAG, "Pico is installed okay");
                    } else
                        Log.e(TAG, "Got a failure. TTS apparently not available");
                }
            });
        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        myActivityResultLauncher.launch(checkIntent);


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


    public void createCameraSource() {
        //Setup the FaceDetector
        Context context = getApplicationContext();

        FaceDetector detector = new FaceDetector.Builder(context)
            .setProminentFaceOnly(true)   //track only one face... makes it faster.
            .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)  //allows for eye and smile detection!
            .build();

        detector.setProcessor(
            new LargestFaceFocusingProcessor(detector, new FaceTracker()));


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

    @SuppressLint("MissingPermission")
    void startPreview() {
        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        if (!allPermissionsGranted()) {
            return;  //permissions are asked elsewhere.  but the surface created, calls this at start and will crash otherwise.
            //asking permissions twice causes one of them to say no, while waiting on the other.
        }
        if (mSurfaceAvailable && mCameraSource != null) {

            try {
                mCameraSource.start(binding.CameraView.getHolder());
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
        //if we lose focus, stop talking.
        if (mTts != null)
            mTts.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraSource.release();
        mTts.shutdown();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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

    /*
     *  FaceDetector code
     */
    class FaceTracker extends Tracker<Face> {
        boolean RightEye, LeftEye, Smile;
        boolean AskRight, AskLeft, AskSmile;
        String TAG = " Tracker";

        public void onNewItem(int id, @NonNull Face face) {
            Log.i(TAG, "Awesome person detected.  Hello!");
            //mLogger.setText("New Face");
            sendmessage("New Face");
        }

        public void onUpdate(@NonNull Detector.Detections<Face> detections, Face face) {


            //Is the left Eye open?
            LeftEye = face.getIsLeftEyeOpenProbability() > 0.75;
            // Log.i(TAG, "LeftEye Open is " + LeftEye);

            //Is the Right Eye open?
            RightEye = face.getIsRightEyeOpenProbability() > 0.75;
            //  Log.i(TAG, "RightEye Open is " + RightEye);

            //Is the face smiling?
            Smile = face.getIsSmilingProbability() > 0.75;
            if (canspeak) {  //don't speak if it's not setup, otherwise force close...
                //  Log.i(TAG, "Smile is " + Smile);
                //mLogger.setText("Smile: " + Smile + " Left: " + LeftEye + " Right:" +RightEye);
                if (!mTts.isSpeaking()) {  //If not speaking.
                    if (!LeftEye) {  //checking left eye first.
                        if (!AskLeft) { //have I already asked?
                            Speech("Please Open your Left Eye");
                            AskLeft = true;
                            AskRight = false;
                            AskSmile = false;
                        }
                    } else if (!RightEye) {
                        if (!AskRight) {
                            Speech("Please Open your Right Eye");
                            AskLeft = false;
                            AskRight = true;
                            AskSmile = false;
                        }
                    } else if (!Smile) {
                        if (!AskSmile)
                            Speech("Please Smile");
                        AskLeft = false;
                        AskRight = false;
                        AskSmile = true;
                    } else if (AskSmile) {
                        AskLeft = false;
                        AskRight = false;
                        AskSmile = false;
                        Speech("Perfect!");
                    }
                }
            }
            sendmessage("Smile: " + Smile + " Left: " + LeftEye + " Right:" + RightEye);


        }

        public void onDone() {
            Log.i(TAG, "Elvis has left the building.");
            //mLogger.setText("No Face dectected");
            sendmessage("No Face detected.");
        }
    }

    public void Speech(String text) {
        mTts.speak(text, TextToSpeech.QUEUE_ADD, null, myUtteranceId);
    }

    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;

        msg.what = 1;  //so the empty message is not used!
        // System.out.println("About to Send message"+ logthis);
        handler.sendMessage(msg);
        // System.out.println("Sent message"+ logthis);
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
            Log.wtf(TAG, "TextToSpeech.SUCCESS");
            canspeak = true;
        } else if (status == TextToSpeech.ERROR) {
            Log.wtf(TAG, "TextToSpeech.ERROR");
        } else {
            Log.wtf(TAG, "status is " + status);
        }
    }
}
