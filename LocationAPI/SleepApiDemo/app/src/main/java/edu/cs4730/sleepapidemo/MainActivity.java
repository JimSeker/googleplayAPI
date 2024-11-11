package edu.cs4730.sleepapidemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.SleepSegmentRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Map;

import edu.cs4730.sleepapidemo.databinding.ActivityMainBinding;


/**
 * very simple implementation of the sleep APIs.  It just subscribes and the the broadcast receiver
 * will send to the logcat the information. Likely you really want a database (like room) and the main activity
 * could then show you what it thinks is your sleep cycles.
 * <p>
 * Note if you don't unsubscribe, it will still calls to the broadcast receiver until the phone is rebooted.
 */

public class MainActivity extends AppCompatActivity {

    public final static String TAG = "MainActivity";
    // for checking permissions.
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACTIVITY_RECOGNITION};

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        // for checking permissions.
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        //We have permissions, so ...
                        logthis("We have permission, starting");
                        subscribeToSleep();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );

        binding.start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckPerm();
            }
        });
        binding.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unsubscriptToSleep();
            }
        });
        binding.stop.setEnabled(false);

    }


    @SuppressLint({"MissingPermission", "UnspecifiedImmutableFlag"})
        //it's already handled.
    void subscribeToSleep() {
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast(
                getApplicationContext(), 0, new Intent(getApplicationContext(), SleepReceiver.class),
                PendingIntent.FLAG_MUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(
                getApplicationContext(), 0, new Intent(getApplicationContext(), SleepReceiver.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        }
        ActivityRecognition.getClient(getApplicationContext()).requestSleepSegmentUpdates(
                pi, SleepSegmentRequest.getDefaultSleepSegmentRequest())
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {
                                          logthis("Successfully subscribed to sleep data");
                                          logthis("Note, all data goes the logcat, not the screen.");
                                          binding.start.setEnabled(false);
                                          binding.stop.setEnabled(true);
                                      }
                                  }

            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to subscribe to sleep data");
                    binding.start.setEnabled(true);
                    binding.stop.setEnabled(false);
                }
            });

    }

    @SuppressLint({"MissingPermission", "UnspecifiedImmutableFlag"})
    void unsubscriptToSleep() {
        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pi = PendingIntent.getBroadcast(
                getApplicationContext(), 0, new Intent(getApplicationContext(), SleepReceiver.class),
                PendingIntent.FLAG_MUTABLE);
        } else {
            pi = PendingIntent.getBroadcast(
                getApplicationContext(), 0, new Intent(getApplicationContext(), SleepReceiver.class),
                PendingIntent.FLAG_CANCEL_CURRENT);
        }
        ActivityRecognition.getClient(getApplicationContext()).removeActivityUpdates(pi)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid) {
                                          logthis("Successfully Unsubscribed to sleep data");
                                          binding.start.setEnabled(true);
                                          binding.stop.setEnabled(false);
                                      }
                                  }

            ).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to Unsubscribe to sleep data");
                    //not sure if we are subscribed or not.
                    binding.start.setEnabled(true);
                    binding.stop.setEnabled(false);
                }
            });

    }

    //ask for permissions when we start.
    public void CheckPerm() {
        if (!allPermissionsGranted()) {
            logthis("Requesting permissions");
            rpl.launch(REQUIRED_PERMISSIONS);
            return;
        }
        subscribeToSleep();
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

    //helper function to print the screen and log it.
    void logthis(String item) {
        Log.v(TAG, item);
        binding.logger.append("\n" + item);
    }
}