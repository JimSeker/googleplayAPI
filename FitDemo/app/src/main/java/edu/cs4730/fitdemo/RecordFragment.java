package edu.cs4730.fitdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Subscription;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

/**
 * This fragment shows to add/cancel subscriptions so the phone can update the data.
 *
 * This needs cleaned up and the async tasks need fixed so they can use the UI.  right now everything
 * uses Log.e instead of the standard logger (it works though).
 */
public class RecordFragment extends Fragment {

    static final int REQUEST_OAUTH = 2;
    String TAG = "RecordFrag";
    TextView logger;
    Button btn_cancel, btn_show;

    public RecordFragment() {
        // Required empty public constructor
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
                Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .listSubscriptions(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(new OnSuccessListener<List<Subscription>>() {
                        @Override
                        public void onSuccess(List<Subscription> subscriptions) {
                            for (Subscription sc : subscriptions) {
                                DataType dt = sc.getDataType();
                                logthis( "Active subscription for data type: " + dt.getName());
                            }
                        }
                    });

            }
        });
        btn_cancel = (Button) myView.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
                    .unsubscribe(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            logthis("Canceled subscriptions!");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Subscription not removed
                            logthis("Failed to cancel subscriptions");
                        }
                    });
            }
        });


        FitnessOptions fitnessOptions =
            FitnessOptions.builder().addDataType(DataType.TYPE_STEP_COUNT_DELTA).build();

        // Check if the user has permissions to talk to Fitness APIs, otherwise authenticate the
        // user and request required permissions.
        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getLastSignedInAccount(getContext()), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this,
                REQUEST_OAUTH,
                GoogleSignIn.getLastSignedInAccount(getContext()),
                fitnessOptions);
        } else {
            subscribe();
        }

        return myView;
    }

    public void logthis(String item) {
        Log.i(TAG, item);
        logger.append(item + "\n");
    }

    public void subscribe() {
        //connect to the step count.
        Fitness.getRecordingClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getContext()))
            .subscribe(DataType.TYPE_STEP_COUNT_DELTA)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    logthis("Subscribed to the Recording API");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("failed to subscribe to the Recording API");
                }
            });
    }


    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_OAUTH) {
                subscribe();
            }
        }
    }
}
