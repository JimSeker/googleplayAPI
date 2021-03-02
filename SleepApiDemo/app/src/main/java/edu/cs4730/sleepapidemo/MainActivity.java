package edu.cs4730.sleepapidemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


/**
 * very simple implementation of the sleep APIs.  It just subscribes and the the broadcast receiver
 * will send to the logcat the information. Likely you really want a database (like room) and the main activity
 * could then show you what it thinks is your sleep cycles.
 * <p>
 * Note if you don't unsubscribe, it will still calls to the broadcast receiver until the phone is rebooted.
 */

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "MainActivity";
    public static final int REQUEST_ACCESS_Activity_Updates = 0;

    Button subscribe, unsubscribe;
    TextView logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CheckPerm();
        logger = findViewById(R.id.logger);
        subscribe = findViewById(R.id.start);
        unsubscribe = findViewById(R.id.stop);
        subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subscribeToSleep();
            }
        });
        unsubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscriptToSleep();
            }
        });
        unsubscribe.setEnabled(false);
    }


    @SuppressLint("MissingPermission")
        //it's already handled.
    void subscribeToSleep() {
        PendingIntent pi = PendingIntent.getBroadcast(
            getApplicationContext(),
            0,
            new Intent(getApplicationContext(), SleepReceiver.class),
            PendingIntent.FLAG_CANCEL_CURRENT
        );

        ActivityRecognition.getClient(getApplicationContext()).requestSleepSegmentUpdates(
            pi,
            SleepSegmentRequest.getDefaultSleepSegmentRequest()
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       logthis("Successfully subscribed to sleep data");
                                       subscribe.setEnabled(false);
                                       unsubscribe.setEnabled(true);
                                   }
                               }

        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("Failed to subscribe to sleep data");
                subscribe.setEnabled(true);
                unsubscribe.setEnabled(false);
            }
        });

    }

    @SuppressLint("MissingPermission")
    void unsubscriptToSleep() {
        PendingIntent pi = PendingIntent.getBroadcast(
            getApplicationContext(),
            0,
            new Intent(getApplicationContext(), SleepReceiver.class),
            PendingIntent.FLAG_CANCEL_CURRENT
        );
        ActivityRecognition.getClient(getApplicationContext()).removeActivityUpdates(
            pi
        ).addOnSuccessListener(new OnSuccessListener<Void>() {
                                   @Override
                                   public void onSuccess(Void aVoid) {
                                       logthis("Successfully UNsubscribed to sleep data");
                                       subscribe.setEnabled(true);
                                       unsubscribe.setEnabled(false);
                                   }
                               }

        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("Failed to UNsubscribe to sleep data");
                //not sure if we are subscribed or not.
                subscribe.setEnabled(true);
                unsubscribe.setEnabled(false);
            }
        });

    }

    //ask for permissions when we start.
    public void CheckPerm() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            logthis("asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                MainActivity.REQUEST_ACCESS_Activity_Updates);

        } else {
            subscribeToSleep();
        }

    }


    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "onRequest result called.");

        if (requestCode == REQUEST_ACCESS_Activity_Updates) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have permissions, so ...
                logthis("We have permission, starting");
                subscribeToSleep();
            } else {
                // permission denied,    Disable this feature or close the app.
                logthis("Activity permission was NOT granted.");
                Toast.makeText(this, "Activity access NOT granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    void logthis(String item) {
        Log.v(TAG, item);
        logger.append("\n" + item);
    }
}