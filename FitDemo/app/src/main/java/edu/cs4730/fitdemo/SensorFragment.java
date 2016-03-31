package edu.cs4730.fitdemo;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class SensorFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    String TAG = "SensorFrag";
    TextView logger;
    static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    //921882021599-sa40s3crq61b27kgiiin5bibo1ta6mdo.apps.googleusercontent.com
    GoogleApiClient mGoogleApiClient;
    OnDataPointListener mlistener;
    Handler handler;
    public SensorFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }


        //create the Google API Client
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                //select the fitness API
                .addApi(Fitness.SENSORS_API)
                        //specifiy the scope of access
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                        //provide callbacks
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
        View myView =  inflater.inflate(R.layout.fragment_sensor, container, false);
        logger = (TextView) myView.findViewById(R.id.logger);
        return myView;
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
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {

        mGoogleApiClient.disconnect();

        super.onStop();
    }
    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
           // setupSensors();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            removeListener();
        }

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "OnConnected!");
        logthis("OnConnected");
        //now you can make call to the fitness APIs
        setupSensors();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "onConnectionSuspected");
        logthis("OnConnectionSuspected");
    }

    public void setupSensors() {
        logthis("Setup Sensors start");
        mlistener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
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

        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                //.setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)  //works, but never get any data...
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                        //.setDataSourceTypes(DataSource.TYPE_DERIVED)  //works on moto G, but not Nexus 5X
                        // .setDataSourceTypes(DataSource.TYPE_RAW)  //works on Nexus 5X, but not moto G.  wtf?  commented both out and it works.
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                logthis("onResult of data source sert.");
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    logthis("data source is " + dataSource.toDebugString());
                    if (DataType.TYPE_STEP_COUNT_CUMULATIVE.equals(dataSource.getDataType())) {
                        logthis("Found a data source");
                        addListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
                    }
                }
            }
        };

        Fitness.SensorsApi.findDataSources(mGoogleApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
        logthis("Setup Sensors done");
    }
    public void addListener(DataSource dataSource, DataType dataType) {
        //adding listener
        logthis("Adding Listener");

        SensorRequest req = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(1, TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(mGoogleApiClient, req, mlistener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Log.i(TAG, "Result call back: " + status.getStatusMessage());
                        logthis("Result call back: " + status.getStatusMessage());
                    }
                });

    }
    public void removeListener() {
        PendingResult<Status> pendingResult = Fitness.SensorsApi.remove(mGoogleApiClient, mlistener);
        pendingResult.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Log.i(TAG, "Result call back: " + status.getStatusMessage());
            }
        });
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(getActivity(), REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.v(TAG, "onConnectionFailed cause:" + connectionResult.toString());
            logthis("onConnectionFailed cause:" + connectionResult.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_OAUTH) {
            authInProgress = false;
            if (resultCode == Activity.RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.e("GoogleFit", "RESULT_CANCELED");
            }
        } else {
            Log.e("GoogleFit", "requestCode NOT request_oauth");
        }
    }
}
