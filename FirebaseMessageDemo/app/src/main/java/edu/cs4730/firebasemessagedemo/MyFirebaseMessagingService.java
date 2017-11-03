package edu.cs4730.firebasemessagedemo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Seker on 4/14/2017.
 * <p>
 * This class is where the push message will go.  onMessageReceived is called, when then
 * creates a notification.  This would be changed to call an activity, update a service, any number
 * of things the developer wants to do.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    //This is called when we get a new push message.
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Displaying data in log
        //It is optional
        Log.wtf(TAG, "From: " + remoteMessage.getFrom());
        String message = remoteMessage.getData().toString();
        //getData should have the data we need, but when sending from the console, it's in getBody,
        //so this is accounting for the possibilities.
        if (message.compareTo("{}") != 0) {  //none empty message
            Log.d(TAG, "Notification Message Data: " + message);
        } else {
            message = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Message Body: " + message);
        }

        //if ( shorter then 10 seconds)
        //Calling method to generate notification
        sendNotification(message);
        // } else {  //processing will take too long, must use a job scheduler.
        //scheduleJob();
        //
    }
    /**
     * Schedule a job using FirebaseJobDispatcher.     so if longer then 10 seconds, schedule the job.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
            .setService(MyJobService.class)
            .setTag("my-job-tag")
            .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }
    //This method generates a notification on the device.
    //If there is correctly formatted json, it will use it, other just display the message
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT);
        String title = "", message;

        //read for a json object.  if that fails, just show the data as message body.

        try {
            JSONObject json = new JSONObject(messageBody);
            JSONObject data = json.getJSONObject("data");
            title = data.getString("title");
            if (title == null || title.compareTo("{}") == 0) {
                //something wrong the json or there is no json.
                title = "Firebase Push Notification";
                message = messageBody;
            } else {
                title = "FCM: " + title;
                message = data.getString("message");
            }
        } catch (JSONException e) {
            Log.v(TAG, "no JSON, fail back.");
            title = "Firebase Push Notification";
            message = messageBody;
        }
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
