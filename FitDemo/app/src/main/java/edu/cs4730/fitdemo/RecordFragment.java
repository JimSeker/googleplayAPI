package edu.cs4730.fitdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.fitness.result.ListSubscriptionsResult;

/**
 * This fragment shows to add/cancel subscriptions so the phone can update the data.
 *
 * This needs cleaned up and the async tasks need fixed so they can use the UI.  right now everything
 * uses Log.e instead of the standard logger (it works though).
 */
public class RecordFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;

    boolean subscribed = false;
    static final int REQUEST_OAUTH = 2;
    String TAG = "RecordFrag";
    TextView logger;
    Button btn_cancel, btn_show;

    public RecordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity().getApplicationContext())
                .addApi(Fitness.RECORDING_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                        //have it login the user instead of us doing it manually.
                .enableAutoManage(getActivity(), REQUEST_OAUTH, this)
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_record, container, false);
        logger = (TextView) myView.findViewById(R.id.loggerr);
        btn_show = (Button) myView.findViewById(R.id.btn_show);
        btn_show.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fitness.RecordingApi.listSubscriptions(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<ListSubscriptionsResult>() {
                            @Override
                            public void onResult(ListSubscriptionsResult listSubscriptionsResult) {
                                for (Subscription subscription : listSubscriptionsResult.getSubscriptions()) {
                                    DataType dataType = subscription.getDataType();
                                    logthis(dataType.getName());
                                    for (Field field : dataType.getFields()) {
                                        logthis(field.toString());
                                    }
                                }
                            }
                        });
            }
        });
        btn_cancel = (Button) myView.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fitness.RecordingApi.unsubscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                        .setResultCallback(new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                if (status.isSuccess()) {
                                    logthis("Canceled subscriptions!");
                                    subscribed = false;
                                } else {
                                    // Subscription not removed
                                    logthis("Failed to cancel subscriptions");
                                }
                            }
                        });
            }
        });

        return myView;
    }

    public void logthis(String item) {
        Log.i(TAG, item);
        logger.append(item + "\n");
    }

    public void subscribe() {
        //connect to the step count.
        Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode() == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                logthis("Already subscribed to the Recording API");
                            } else {
                                logthis("Subscribed to the Recording API");
                            }
                            subscribed = true;
                        }
                    }
                });
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
        if (mGoogleApiClient.isConnected()) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            subscribe();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        logthis("OnConnected!");
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        logthis("OnConnectionSuspected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        logthis("onConnectionFailed cause:" + connectionResult.toString());
    }

}
