/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.activityrecognition;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static final String TAG = "MainActivity";
    public static final int REQUEST_ACCESS_Activity_Updates = 0;
    private Context mContext;

    // UI elements.
    private Button mRequestActivityUpdatesButton;
    private Button mRemoveActivityUpdatesButton;

    /**
     * Adapter backed by a list of DetectedActivity objects.
     */
    private DetectedActivitiesAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        mContext = this;

        // Get the UI widgets.
        mRequestActivityUpdatesButton = findViewById(R.id.request_activity_updates_button);
        mRemoveActivityUpdatesButton = findViewById(R.id.remove_activity_updates_button);
        ListView detectedActivitiesListView = findViewById(
            R.id.detected_activities_listview);

        // Enable either the Request Updates button or the Remove Updates button depending on
        // whether activity updates have been requested.
        setButtonsEnabledState();

        ArrayList<DetectedActivity> detectedActivities = Utils.detectedActivitiesFromJson(
            PreferenceManager.getDefaultSharedPreferences(this).getString(
                Constants.KEY_DETECTED_ACTIVITIES, ""));

        // Bind the adapter to the ListView responsible for display data for detected activities.
        mAdapter = new DetectedActivitiesAdapter(this, detectedActivities);
        detectedActivitiesListView.setAdapter(mAdapter);

    }


    //ask for permissions when we start.
    public void CheckPerm() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                MainActivity.REQUEST_ACCESS_Activity_Updates);

        } else {
            // start what, I can't tell in google's code, so was in onresume?
            PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
            updateDetectedActivitiesList();
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
                PreferenceManager.getDefaultSharedPreferences(this)
                    .registerOnSharedPreferenceChangeListener(this);
                updateDetectedActivitiesList();
            } else {
                // permission denied,    Disable this feature or close the app.
                Log.v(TAG, "Activity permission was NOT granted.");
                Toast.makeText(this, "Activity access NOT granted", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPerm();

    }

    @Override
    protected void onPause() {
        PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    /**
     * Registers for activity recognition updates using
     * {@link ActivityRecognitionClient#requestActivityUpdates(long, PendingIntent)}.
     * Registers success and failure callbacks.
     */
    @SuppressLint("MissingPermission")  //there are checked, studio just can't tell.
    public void requestActivityUpdatesButtonHandler(View view) {
        Task<Void> task = ActivityRecognition.getClient(this).requestActivityUpdates(
            Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
            getActivityDetectionPendingIntent());

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                    getString(R.string.activity_updates_enabled),
                    Toast.LENGTH_SHORT)
                    .show();
                setUpdatesRequestedState(true);
                updateDetectedActivitiesList();
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, getString(R.string.activity_updates_not_enabled));
                Toast.makeText(mContext,
                    getString(R.string.activity_updates_not_enabled),
                    Toast.LENGTH_SHORT)
                    .show();
                setUpdatesRequestedState(false);
            }
        });
    }


    /**
     * Removes activity recognition updates using
     * {@link ActivityRecognitionClient#removeActivityUpdates(PendingIntent)}. Registers success and
     * failure callbacks.
     */
    @SuppressLint("MissingPermission") //it's checked.
    public void removeActivityUpdatesButtonHandler(View view) {
         Task<Void> task = ActivityRecognition.getClient(this).removeActivityUpdates(
            getActivityDetectionPendingIntent());
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Toast.makeText(mContext,
                    getString(R.string.activity_updates_removed),
                    Toast.LENGTH_SHORT)
                    .show();
                setUpdatesRequestedState(false);
                // Reset the display.
                mAdapter.updateActivities(new ArrayList<DetectedActivity>());
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Failed to enable activity recognition.");
                Toast.makeText(mContext, getString(R.string.activity_updates_not_removed),
                    Toast.LENGTH_SHORT).show();
                setUpdatesRequestedState(true);
            }
        });
    }

    /**
     * Gets a PendingIntent to be sent for each activity detection.
     */
    @SuppressLint("UnspecifiedImmutableFlag")
    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            pendingIntent =  PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_MUTABLE);
        } else {
            pendingIntent =  PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingIntent;
    }

    /**
     * Ensures that only one button is enabled at any time. The Request Activity Updates button is
     * enabled if the user hasn't yet requested activity updates. The Remove Activity Updates button
     * is enabled if the user has requested activity updates.
     */
    private void setButtonsEnabledState() {
        if (getUpdatesRequestedState()) {
            mRequestActivityUpdatesButton.setEnabled(false);
            mRemoveActivityUpdatesButton.setEnabled(true);
        } else {
            mRequestActivityUpdatesButton.setEnabled(true);
            mRemoveActivityUpdatesButton.setEnabled(false);
        }
    }

    /**
     * Retrieves the boolean from SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private boolean getUpdatesRequestedState() {
        return PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, false);
    }

    /**
     * Sets the boolean in SharedPreferences that tracks whether we are requesting activity
     * updates.
     */
    private void setUpdatesRequestedState(boolean requesting) {
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putBoolean(Constants.KEY_ACTIVITY_UPDATES_REQUESTED, requesting)
            .apply();
        setButtonsEnabledState();
    }

    /**
     * Processes the list of freshly detected activities. Asks the adapter to update its list of
     * DetectedActivities with new {@code DetectedActivity} objects reflecting the latest detected
     * activities.
     */
    protected void updateDetectedActivitiesList() {
        ArrayList<DetectedActivity> detectedActivities = Utils.detectedActivitiesFromJson(
            PreferenceManager.getDefaultSharedPreferences(mContext)
                .getString(Constants.KEY_DETECTED_ACTIVITIES, ""));

        mAdapter.updateActivities(detectedActivities);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(Constants.KEY_DETECTED_ACTIVITIES)) {
            updateDetectedActivitiesList();
        }
    }
}