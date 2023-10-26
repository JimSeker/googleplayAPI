package edu.cs4730.nearbymessagesdemo;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessagesClient;
import com.google.android.gms.nearby.messages.MessagesOptions;
import com.google.android.gms.nearby.messages.NearbyPermissions;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;


/**
 * This is a simple example of how to publish a message with nearby.
 *
 * This example will publish a message for three message and turn it off.
 *   if you want longer (change the time) or unlimited, remove the options.
 *
 */
public class PublishFragment extends Fragment {

    String TAG = "PublishFragment";
    TextView logger;
    boolean mIsPublish = false;

    Message mActiveMessage;


    /**
     * Sets the time in seconds for a published message or a subscription to live. Set to three
     * minutes in this sample.
     */
    private static final int TTL_IN_SECONDS = 3 * 60; // Three minutes.
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
        .setTtlSeconds(TTL_IN_SECONDS).build();


    public PublishFragment() {
        // Required empty public constructor
    }

    /**
     * this publishes a message for a period of 3 minutes and then times out.
     *   remove options to make it unlimited.
     * Note: you can only have one message at a time.
     */

    private void publish() {
        logthis("Publishing message: " + "Hi there");
       // mActiveMessage = new Message("Hello World".getBytes());
        mActiveMessage = new Message("Hi there".getBytes());

        PublishOptions options = new PublishOptions.Builder()
            .setStrategy(PUB_SUB_STRATEGY)
            .setCallback(new PublishCallback() {
                @Override
                public void onExpired() {
                    super.onExpired();
                    logthis("No longer publishing");
                    mIsPublish = false;
                    mActiveMessage = null;
                }
            }).build();
                //note this must be an activity, not context.
        MessagesClient mMessagesClient = Nearby.getMessagesClient(requireActivity(), new MessagesOptions.Builder()
            .setPermissions(NearbyPermissions.BLE)
            .build());
        Nearby.getMessagesClient(requireActivity()).publish(mActiveMessage,options)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to publish");
                    e.printStackTrace();
                }
            })
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    logthis("Successfully published.");
                }
            });
        mIsPublish = true;
    }

    /**
     * this will unpublish a message.
     */
    private void unpublish() {
        logthis("Unpublishing.");
        if (mActiveMessage != null) {
            //note this must be an activity, not context.
            Nearby.getMessagesClient(requireActivity()).unpublish(mActiveMessage);
            mActiveMessage = null;
            mIsPublish = false;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_publish, container, false);
        logger = myView.findViewById(R.id.pub_output);

        myView.findViewById(R.id.start_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mIsPublish)
                    publish();

            }
        });
        myView.findViewById(R.id.end_publish).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsPublish)
                    unpublish();

            }
        });
        return myView;
    }

    //helper function to log and added to textview.
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }
}
