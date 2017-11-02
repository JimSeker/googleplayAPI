package edu.cs4730.activityrecognitiondemo;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.util.List;

//uses the singletop and a onNewIntent method so all the code is kept in this single
//method.   it will say the most probable action and then anything at least 50%
// for testing purposes inside, you can simple move the phone up and down slowly to get walking
//pretty fast for running.  You don't actually have move for those.   walking as good pace and holding
//the device study, which result in driving.   At least on a Moto G (v1) GPE device.


//https://developers.google.com/android/reference/com/google/android/gms/location/ActivityRecognition
//https://github.com/googlesamples/android-play-location/tree/master/ActivityRecognition


public class MainActivity extends AppCompatActivity implements  TextToSpeech.OnInitListener {
    //for the speech part.
    private static final int REQ_TTS_STATUS_CHECK = 0;
    private TextToSpeech mTts;
    private  String myUtteranceId = "txt2spk";
    private boolean canspeak = false;

    private Context mContext;
    /**
     * The desired time between activity detections. Larger values result in fewer activity
     * detections while improving battery life. A value of 0 results in activity detections at the
     * fastest possible rate. Getting frequent updates negatively impact battery life and a real
     * app may prefer to request less frequent updates.
     */
    public static final long DETECTION_INTERVAL_IN_MILLISECONDS = 0;  //fastest rate which appear to be about 10 seconds.
                                                                 //30 * 1000; // 30 seconds
    /**
     * The entry point for interacting with activity recognition.
     */
    private ActivityRecognitionClient mActivityRecognitionClient;

    boolean gettingupdates = false;

    //widgets
    Button btn;
    TextView logger;
    static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do something...
                if (!gettingupdates) {
                    startActivityUpdates();
                } else {
                    stopActivityUpdates();
                }
            }
        });
        mContext = this;
        logger = (TextView) findViewById(R.id.logger);
        // Check to be sure that TTS exists and is okay to use
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        //The result will come back in onActivityResult with our REQ_TTS_STATUS_CHECK number
        startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);

        //setup the client activity piece.
        mActivityRecognitionClient = new ActivityRecognitionClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        if (!gettingupdates)
            startActivityUpdates();
    }

    @Override
    protected void onStop() {
        Log.wtf(TAG, "onStop called.");
        // Unregister the broadcast receiver that was registered during onResume().
        if (gettingupdates)  //if turned on, stop them during pause.
         stopActivityUpdates();
        super.onStop();
    }


    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    public void startActivityUpdates() {
        Log.wtf(TAG, "start called.");
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                        "Activity updates enabled",
                        Toast.LENGTH_SHORT)
                        .show();
                gettingupdates = true;
                btn.setText("Stop Recognition");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to enable activity updates");
                Toast.makeText(mContext,
                        "Failed to enable activity updates",
                        Toast.LENGTH_SHORT)
                        .show();
                gettingupdates = false;
                btn.setText("Start Recognition");
            }
        });
    }

    /**
     * Removes activity recognition updates using
     * {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. Registers success and
     * failure callbacks.
     */
    public void stopActivityUpdates() {
        Log.wtf(TAG, "stop called.");
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                        "Activity updates removed",
                        Toast.LENGTH_SHORT)
                        .show();
                gettingupdates = false;
                btn.setText("Start Recognition");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to enable activity recognition.");
                Toast.makeText(mContext, "Failed to remove activity updates",
                        Toast.LENGTH_SHORT).show();
                gettingupdates = true;  //I think this is true???
                btn.setText("Stop Recognition");
            }
        });
    }



    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    //we want to get the intent back here without starting another instanced
    //so  we get the data here, hopefully.
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent");

        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
        //get most probable activity
        DetectedActivity probably = result.getMostProbableActivity();
        if (probably.getConfidence() >= 50) {  //doc's say over 50% is likely, under is not sure at all.
            speech(getActivityString(probably.getType()));
        }
        logger.append("Most Probable: " +getActivityString(probably.getType()) + " "+ probably.getConfidence()+"%\n" );
        //or we could go through the list, which is sorted by most likely to least likely.
        List<DetectedActivity> fulllist = result.getProbableActivities();
        for (DetectedActivity da: fulllist) {
            if (da.getConfidence() >=50) {
                logger.append("-->" + getActivityString(da.getType()) + " " + da.getConfidence() + "%\n");
                speech(getActivityString(da.getType()));

            }
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


    //simple method to do the speech
    public void speech(String words) {
        //Speech is simple.  send the words to speech aloud via the
        //the text to speech end and add it to the end queue. (maybe others already in line.)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //not sure what an utteranceId is supposed to be... we maybe able to setup a
            //listener for "utterances" and check to see if they completed or something.
            mTts.speak(words, TextToSpeech.QUEUE_ADD, null, myUtteranceId);
        } else {  //below lollipop and use this method instead.
            mTts.speak(words, TextToSpeech.QUEUE_ADD, null);
        }
    }
    //verify speech is available.
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_TTS_STATUS_CHECK) {
            switch (resultCode) {
                case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS:
                    // TTS is up and running
                    mTts = new TextToSpeech(this, this);
                    Log.v(TAG, "Pico is installed okay");
                    break;
                case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
                default:
                    Log.e(TAG, "Got a failure. TTS apparently not available");
            }
        }
        else {
            // Got something else
            Log.wtf(TAG, "Got something else in onActivityResult");
        }
    }

    @Override
    public void onInit(int status) {
        // Now that the TTS engine is ready, we enable the button
        if( status == TextToSpeech.SUCCESS) {
            canspeak = true;
        }
    }
}
