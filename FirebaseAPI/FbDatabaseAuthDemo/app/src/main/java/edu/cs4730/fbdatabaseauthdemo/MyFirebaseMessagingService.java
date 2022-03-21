package edu.cs4730.fbdatabaseauthdemo;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import androidx.annotation.NonNull;

/**
 *   this is the id and message service (combined in 17).
 *   see https://stackoverflow.com/questions/51123197/firebaseinstanceidservice-is-deprecated for more info.
 *
 *   This is used to caught the notifications (now called cloud messaging) from the console when the app is running.
 *
 */


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFBMessagingService";
    private static final String ENGAGE_TOPIC = "notes_engage";

    /**
     * The Application's current Instance ID token is no longer valid and thus a new one must be requested.
     */

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "FCM Token: " + token);

        // Once a token is generated, we subscribe to topic.  May take 24 hours to show in the console.
        FirebaseMessaging.getInstance().subscribeToTopic(ENGAGE_TOPIC);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        //Normally, we would send an intent/message to the activity or create a notification for the user.
        //here we are just logging the message.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());
        Log.d(TAG, "FCM Notification Message: " + remoteMessage.getNotification());
        //remoteMessage.getData() is coming up blank...
        Log.d(TAG, "FCM Title Message: " + remoteMessage.getNotification().getTitle());
        Log.d(TAG, "FCM Data Message: " + remoteMessage.getNotification().getBody());

    }
}