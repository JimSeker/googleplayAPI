package edu.cs4730.fbdatabaseauthdemo;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Map;

/**
 * This is a simple demo of Remote Config Firebase functions.
 */
public class RCFragment extends Fragment {

    private static final String TAG = "RCFragment";
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    public static final String NOTE_LENGTH_KEY = "note_msg_length";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 140;

    TextView rc_value;

    public RCFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_rc, container, false);

        rc_value = myView.findViewById(R.id.rc_value);
        myView.findViewById(R.id.btn_rc).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchConfig();
            }
        });


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        // Create Remote Config Setting to enable developer mode.
        // Fetching configs from the server is normally limited to 5 requests per hour.
        // Enabling developer mode allows many more requests to be made per hour, so developers
        // can test different config values during development.
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
            .setDeveloperModeEnabled(BuildConfig.DEBUG)
            .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        // Define default config values. Defaults are used when fetched config values are not
        // available. Eg: if an error occurred fetching values from the server.
        Map<String, Object> defaultConfigMap = new HashMap<>();
        defaultConfigMap.put(NOTE_LENGTH_KEY, DEFAULT_MSG_LENGTH_LIMIT);
        mFirebaseRemoteConfig.setDefaults(defaultConfigMap);
        fetchConfig();

        return myView;
    }


    // Fetch the config to determine the allowed length of messages.
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hour in seconds
        // If developer mode is enabled reduce cacheExpiration to 0 so that each fetch goes to the
        // server. This should not be used in release builds.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        Log.d(TAG, "cache is " + cacheExpiration);
        mFirebaseRemoteConfig.fetch(cacheExpiration)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Make the fetched config available
                    // via FirebaseRemoteConfig get<type> calls, e.g., getLong, getString.
                    mFirebaseRemoteConfig.activateFetched();

                    // Update the config's.  In our case, just display it.
                    applyRCvalue();

                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // An error occurred when fetching the config.
                    Log.w(TAG, "Error fetching config", e);
                    applyRCvalue();
                }
            });
    }

    void applyRCvalue() {
       long note_msg_length = mFirebaseRemoteConfig.getLong(NOTE_LENGTH_KEY);
        rc_value.setText(String.valueOf(note_msg_length));
    }


}
