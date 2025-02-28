package edu.cs4730.firebasemessagedemo;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

/**
 * Created by Seker on 4/14/2017.
 * <p>
 * This class is where the push message will go.  onMessageReceived is called, when then
 * creates a notification.  This would be changed to call an activity, update a service, any number
 * of things the developer wants to do.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";


    /**
     * Replacement for the FirebaseInstanceID service, which was depreciated in 17.x
     * <p>
     * This is called on at least the first startup.  it generates a unique token that is
     * used by the cloud messaging system.  definitely save the token for later use.
     *
     * @param token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.wtf(TAG, "FCM Token: " + token);
        //store the token for later use in the app.
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);
    }


    //This is called when we get a new push message.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.wtf(TAG, "From: " + remoteMessage.getFrom());
        //google rewrite this and I didn't see the update.
//        String message = remoteMessage.getData().toString();
//        //getData should have the data we need, but when sending from the console, it's in getBody,
//        //so this is accounting for the possibilities.
//        if (message.compareTo("{}") != 0) {  //none empty message
//            Log.d(TAG, "Notification Message Data: " + message);
//        } else {
//            message = remoteMessage.getNotification().getBody();
//            Log.d(TAG, "Notification Message Body: " + message);
//        }
        String message = "default";
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getTitle());
            sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
            sendNotification(message, message);
        }


        //if ( shorter then 10 seconds)
        //Calling method to generate notification

        // } else {  //processing will take too long, must use a job scheduler.
        //scheduleJob();
        //
    }

    /**
     * Schedule a job using Worker     so if longer then 10 seconds, schedule the job.
     */
    private void scheduleJob() {

        Data inputData = new Data.Builder()
            .putString("some_key", "some_value")
            .build();
        Constraints constraints = new Constraints.Builder()
            // The Worker needs Network connectivity
            .setRequiredNetworkType(NetworkType.CONNECTED)
            // Needs the device to be charging
            .setRequiresCharging(true)
            .build();

        OneTimeWorkRequest request =
            // Tell which work to execute
            new OneTimeWorkRequest.Builder(myWorker.class)
                // Sets the input data for the ListenableWorker
                .setInputData(inputData)
                // If you want to delay the start of work by 60 seconds
                .setInitialDelay(60, TimeUnit.SECONDS)
                // Set additional constraints
                .setConstraints(constraints)
                .build();
        //start up the work request finally.
        WorkManager.getInstance(getApplicationContext())
            .enqueueUniqueWork("my-unique-name", ExistingWorkPolicy.KEEP, request);

    }

    /**
     * This method generates a notification on the device.
     * If there is correctly formatted json, it will use it, other just display the message
     */

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE);
        } else {
            //lint is wrong, it can't see the if statement correctly.
            pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);
        }

//        //read for a json object.  if that fails, just show the data as message body.
//        Log.v(TAG, "mssages: " + messageBody);
//        try {
//            JSONObject json = new JSONObject(messageBody);
//            JSONObject data = json.getJSONObject("data");
//            title = data.getString("title");
//            if (title == null || title.compareTo("{}") == 0) {
//                //something wrong the json or there is no json.
//                title = "Firebase Push Notification";
//                message = messageBody;
//            } else {
//                title = "FCM: " + title;
//                message = data.getString("message");
//            }
//        } catch (JSONException e) {
//            Log.v(TAG, "no JSON, fail back.");
//            title = "Firebase Push Notification";
//            message = messageBody;
//        }
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);


        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setChannelId(getString(R.string.default_notification_channel_id))
            .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
