package edu.cs4730.actmapdemo;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


/**
 * Simple intent service to get the activity Recognition info and send back to activity
 * the most probable activity (or unknown) via a messager.
 **/

public class DetectedActivitiesIntentService extends IntentService {

    protected static final String TAG = "DetectedActivitiesIS";

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     *
     * @param intent The Intent is provided (inside a PendingIntent) when requestActivityUpdates()
     *               is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        int currentActivity = DetectedActivity.UNKNOWN;
        //get the message handler
        Messenger messenger = null;
        Bundle extras = intent.getExtras();
        if (extras != null) {
            messenger = (Messenger) extras.get("MESSENGER");
        }
        //get the activity info
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        //get most probable activity
        DetectedActivity probably = result.getMostProbableActivity();
        if (probably.getConfidence() >= 50) {  //doc's say over 50% is likely, under is not sure at all.
            currentActivity = probably.getType();
        }
        Log.v(TAG, "about to send message");
        if (messenger != null) {
            Message msg = Message.obtain();
            msg.arg1 = currentActivity;
            Log.v(TAG, "Sent message");
            try {
                messenger.send(msg);
            } catch (android.os.RemoteException e1) {
                Log.w(getClass().getName(), "Exception sending message", e1);
            }
        }

    }
}
