package edu.cs4730.fitdemo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * This will connect the the "step counter" sensor.  And display results as it is able to get them
 *  Note the step count is cumalutive, since I can't get the delta
 */
public class SensorFragment extends Fragment {

    final static String TAG = "SensorFrag";
    TextView logger;
    static final int REQUEST_OAUTH = 1;
    OnDataPointListener mlistener;
    Handler handler;

    public SensorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == 0) {
                    Bundle stuff = msg.getData();
                    logthis(stuff.getString("logthis"));
                }
                return true;
            }
        });
        View myView = inflater.inflate(R.layout.fragment_sensor, container, false);
        logger = myView.findViewById(R.id.logger);

        return myView;
    }


    /**
     * A wrapper. If the user account has OAuth permission,
     * continue to setupsensors , else request OAuth permission for the account.
     */
    private void findFitnessDataSourcesWrapper() {
        if (hasOAuthPermission()) {
            findFitnessDataSources();
        } else {
            requestOAuthPermission();
        }
    }

    /** Gets the {@link FitnessOptions} in order to check or request OAuth permission for the user. */
    private FitnessOptions getFitnessSignInOptions() {
        return FitnessOptions.builder().addDataType(DataType.TYPE_LOCATION_SAMPLE).build();
    }

    /** Checks if user's account has OAuth permission to Fitness API. */
    private boolean hasOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        return GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(requireContext()), fitnessOptions);
    }

    /** Launches the Google SignIn activity to request OAuth permission for the user. */
    private void requestOAuthPermission() {
        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignIn.requestPermissions(
            this,
            REQUEST_OAUTH,
            GoogleSignIn.getLastSignedInAccount(requireContext()),
            fitnessOptions);

    }

    public void disconnect() {
        //first clean up the session.
        unregisterFitnessDataListener();
        mlistener = null;

        FitnessOptions fitnessOptions = getFitnessSignInOptions();
        GoogleSignInOptions signInOptions = new com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder().addExtension(fitnessOptions).build();

        //GoogleSignInClient client = GoogleSignIn.getClient(getContext(), signInOptions);
        GoogleSignIn.getClient(requireContext(), signInOptions)
            .signOut();

        //instead we could just revoke access, but signout should clear everything.
//            .revokeAccess()
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "disconnected from google fit.");
//                    }
//                });

    }

    public void logthis(String item) {
        logger.append(item + "\n");
    }

    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;
        msg.what = 0;
        handler.sendMessage(msg);

    }


    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery.
        unregisterFitnessDataListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        // This ensures that if the user denies the permissions then uses Settings to re-enable
        // them, the app will start working.
        findFitnessDataSourcesWrapper();

    }

    /** Finds available data sources and attempts to register on a specific {@link DataType}. */
    private void findFitnessDataSources() {

        // Note: Fitness.SensorsApi.findDataSources() requires the ACCESS_FINE_LOCATION permission.
        Fitness.getSensorsClient(requireActivity(), GoogleSignIn.getLastSignedInAccount(requireContext()))
            .findDataSources(
                new DataSourcesRequest.Builder()
                    //.setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                    .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                    .setDataSourceTypes(DataSource.TYPE_RAW)
                    .build())
            .addOnSuccessListener(
                new OnSuccessListener<List<DataSource>>() {
                    @Override
                    public void onSuccess(List<DataSource> dataSources) {
                        for (DataSource dataSource : dataSources) {
                            Log.wtf(TAG, "Data source found: " + dataSource.toString());
                            Log.wtf(TAG, "Data Source type: " + dataSource.getDataType().getName());

                            // Let's register a listener to receive Activity data!
                            if (dataSource.getDataType().equals(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                                && mlistener == null) {
                                Log.i(TAG, "Data source for Step Count (cumulative) found!  Registering.");
                                setupSensors(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                            }
                        }
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "failed", e);
                    }
                });
    }


    public void setupSensors(DataSource dataSource, DataType dataType) {
        logthis("Setup Sensors start");
        mlistener = new OnDataPointListener() {
            @Override
            public void onDataPoint(@NonNull DataPoint dataPoint) {
                //we are not on the UI thread!
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i(TAG, "Detected DataPoint field: " + field.getName());
                    sendmessage("Detected DataPoint field: " + field.getName());
                    Log.i(TAG, "Detected DataPoint value: " + val);
                    sendmessage("Detected DataPoint value: " + val);
                }
            }
        };

        Fitness.getSensorsClient(requireActivity(), GoogleSignIn.getLastSignedInAccount(requireContext()))
            .add(
                new SensorRequest.Builder()
                    .setDataSource(dataSource) // Optional but recommended for custom data sets.
                    .setDataType(dataType) // Can't be omitted.
                    .setSamplingRate(10, TimeUnit.SECONDS)
                    .build(),
                mlistener)
            .addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.i(TAG, "Listener registered!");
                        } else {
                            Log.e(TAG, "Listener not registered.", task.getException());
                        }
                    }
                });

    }

    /** Unregisters the listener with the Sensors API. */
    private void unregisterFitnessDataListener() {
        if (mlistener == null) {
            // This code only activates one listener at a time.  If there's no listener, there's
            // nothing to unregister.
            return;
        }

        // [START unregister_data_listener]
        // Waiting isn't actually necessary as the unregister call will complete regardless,
        // even if called from within onStop, but a callback can still be added in order to
        // inspect the results.
        Fitness.getSensorsClient(requireActivity(), GoogleSignIn.getLastSignedInAccount(requireContext()))
            .remove(mlistener)
            .addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful() && task.getResult()) {
                            Log.i(TAG, "Listener was removed!");
                        } else {
                            Log.i(TAG, "Listener was not removed.");
                        }
                    }
                });
        // [END unregister_data_listener]
    }


    //required, because fit is still using the older methods.  so I can't remove it yet.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            if (resultCode == Activity.RESULT_OK) {
                findFitnessDataSources();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }
}
