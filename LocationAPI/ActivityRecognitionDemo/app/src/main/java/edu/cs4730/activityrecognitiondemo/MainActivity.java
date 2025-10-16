package edu.cs4730.activityrecognitiondemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Map;

import edu.cs4730.activityrecognitiondemo.databinding.ActivityMainBinding;

/**
 * it will say the most probable action and then anything at least 50%
 * for testing purposes inside, you can simple move the phone up and down slowly to get walking
 * pretty fast for running.  You don't actually have move for those.   walking as good pace and holding
 * the device study, which result in driving.   At least on a Moto G (v1) GPE device.
 * <p>
 * https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
 * https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition
 * <p>
 * This does not auto start, the user click the button to start it.
 * this is very slow to receive if nothing changes early on, but afterwards it responds on pretty regularly.
 */

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    // for checking permissions.
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACTIVITY_RECOGNITION};

    //speech pieces.
    private TextToSpeech mTts;
    private String myUtteranceId = "txt2spk";
    private boolean canspeak = false;

    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 1 * 1000;  //fastest rate which appear to be about 10 seconds.
    //30 * 1000; // 30 seconds

    // Action fired when transitions are triggered.
    private final String ACTIVITY_RECEIVER_ACTION = BuildConfig.APPLICATION_ID + "ACTIVITY_RECEIVER_ACTION";
    private ActivityReceiver mActivityReceiver;

    boolean gettingupdates = false;

    static String TAG = "MainActivity";
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        //one of these should keep the screen on.
        setTurnScreenOn(true);

        // for checking permissions.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        //We have permissions, so ...
                        if (!gettingupdates) {
                            logthis("starting from permissions");
                            startActivityUpdates();
                        } else {
                            stopActivityUpdates();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPerm();

            }
        });

        // startActivityForResult method for the speech engine.
        ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getResultCode() == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                        // TTS is up and running
                        mTts = new TextToSpeech(getApplicationContext(), MainActivity.this);
                        logthis("Speech is installed and okay");
                    } else
                        logthis("Got a failure. speech apparently not available");
                }
            });
        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //The result will come back in onActivityResult with our REQ_TTS_STATUS_CHECK number
        myActivityResultLauncher.launch(checkIntent);
        //get the receiver ready.
        mActivityReceiver = new ActivityReceiver();
    }

    //ask for permissions when we start.
    public void CheckPerm() {
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl.launch(REQUIRED_PERMISSIONS);
            return;
        }
        //We have permissions, so ...
        if (!gettingupdates) {
            startActivityUpdates();
        } else {
            stopActivityUpdates();
        }
    }

    //on start setup the receiver
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(mActivityReceiver, new IntentFilter(ACTIVITY_RECEIVER_ACTION), Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(mActivityReceiver, new IntentFilter(ACTIVITY_RECEIVER_ACTION));
        }
    }

    //onstop turn off updates and remove the receiver.
    @Override
    protected void onStop() {
        Log.wtf(TAG, "onStop called.");
        // Unregister the broadcast receiver that was registered during onStart().
        if (gettingupdates)  //if turned on, stop them during pause.
            CheckPerm();
        unregisterReceiver(mActivityReceiver);
        super.onStop();
    }

    /**
     * Registers for activity recognition updates using requestActivityUpdates(long, PendingIntent).
     * Registers success and failure callbacks.
     */
    @SuppressLint("MissingPermission")
    public void startActivityUpdates() {
        logthis("beginning to start activity updates");

        ActivityRecognition.getClient(this).requestActivityUpdates(DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    gettingupdates = true;
                    logthis("Success, activity updates enabled.");
                    binding.button.setText("Stop Recognition");
                }
            })

            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to enable activity updates");
                    gettingupdates = false;
                    binding.button.setText("Start Recognition");
                }
            });
    }

    /**
     * Removes activity recognition updates using removeActivityUpdates(PendingIntent).
     * Registers success and failure callbacks.
     */
    @SuppressLint("MissingPermission")
    public void stopActivityUpdates() {
        logthis("beginning to stop activity updates");
        ActivityRecognition.getClient(this).removeActivityUpdates(getActivityDetectionPendingIntent())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void result) {
                    logthis("Activity updates removed");
                    gettingupdates = false;
                    binding.button.setText("Start Recognition");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to remove activity updates ?!");
                    gettingupdates = true;  //I think this is true???
                    binding.button.setText("Stop Recognition");
                }
            });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    @SuppressLint({"UnspecifiedImmutableFlag", "MutableImplicitPendingIntent"})
    //it's actually handled, but studio doesn't believe me.
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(ACTIVITY_RECEIVER_ACTION);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT);
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            return PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        }
    }


    /**
     * Returns a human readable String corresponding to a detected activity type.
     */
    public static String getActivityString(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return "In a Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On a bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still (not moving)";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.UNKNOWN:
                return "Unknown Activity";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown Type";
        }
    }


    /**
     * simple method to do the speech
     */
    public void speech(String words) {
        //Speech is simple.  send the words to speech aloud via the
        //the text to speech end and add it to the end queue. (maybe others already in line.)
        if (canspeak)
            mTts.speak(words, TextToSpeech.QUEUE_ADD, null, myUtteranceId);
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if (status == TextToSpeech.SUCCESS) {
            canspeak = true;
        }
    }

    //helper function to check if all the permissions are granted.
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    //help function to print the screen and log it.
    void logthis(String item) {
        Log.d(TAG, item);
        binding.logger.append(item + "\n");
    }

    /**
     * Handles intents from from the Activity Updates API.
     */
    public class ActivityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive(): " + intent);

            if (!TextUtils.equals(ACTIVITY_RECEIVER_ACTION, intent.getAction())) {
                logthis("Received an unsupported action in TransitionsReceiver: action = " +
                    intent.getAction());
                return;
            }

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            //get most probable activity
            DetectedActivity probably = result.getMostProbableActivity();
            if (probably.getConfidence() >= 50) {  //doc's say over 50% is likely, under is not sure at all.
                speech(getActivityString(probably.getType()));
            }
            logthis("Most Probable: " + getActivityString(probably.getType()) + " " + probably.getConfidence() + "%");
            //or we could go through the list, which is sorted by most likely to least likely.
            List<DetectedActivity> fulllist = result.getProbableActivities();
            for (DetectedActivity da : fulllist) {
                if (da.getConfidence() >= 50) {
                    logthis("-->" + getActivityString(da.getType()) + " " + da.getConfidence() + "%");
                    speech(getActivityString(da.getType()));

                }
            }
        }
    }
}
