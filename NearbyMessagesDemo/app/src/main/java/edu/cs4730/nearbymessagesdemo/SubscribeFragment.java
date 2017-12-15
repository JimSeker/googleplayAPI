package edu.cs4730.nearbymessagesdemo;


import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.BleSignal;
import com.google.android.gms.nearby.messages.Distance;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageFilter;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeOptions;


/**
 * This class is the subscribe side of the code.  It will read the messages found.
 * It has foreground for when the activity is activity and a background that uses the receiver.
 *
 * The foreground has has code to figure the distance of the beacon if possible.
 *
 */
public class SubscribeFragment extends Fragment {

    String TAG = "SubscribeFragment";
    TextView logger;
    boolean mIsSubscribing = false;
    boolean mIsBGSubscribing = false;

    public SubscribeFragment() {
        // Required empty public constructor
    }

    /**
     * This is the listener for foreground messages.
     */
    MessageListener mMessageListener = new MessageListener() {
        @Override
        public void onFound(Message message) {
            logthis("Found message: " + new String(message.getContent()));
        }

        @Override
        public void onLost(Message message) {
            logthis("Lost sight of message: " + new String(message.getContent()));
        }


        /**
         *
         * Note the following two methods are not required and used for distance information.
         *   one problem, we are not publishing a beacon message, so these are not called.
         *   https://developers.google.com/beacons/proximity/attachments
         *   https://developers.google.com/beacons/proximity/guides
         *
         * Called when the Bluetooth Low Energy (BLE) signal associated with a message changes.
         *
         * This is currently only called for BLE beacon messages.
         *
         * For example, this is called when we see the first BLE advertisement
         * frame associated with a message; or when we see subsequent frames with
         * significantly different received signal strength indicator (RSSI)
         * readings.
         *
         * For more information, see the MessageListener Javadocs.
         */
        @Override
        public void onBleSignalChanged(final Message message, final BleSignal bleSignal) {
            logthis( "Message: " + message + " has new BLE signal information: " + bleSignal);
        }

        /**
         * Called when Nearby's estimate of the distance to a message changes.
         * This is currently only called for BLE beacon messages.
         * For more information, see the MessageListener Javadocs.
         */
        @Override
        public void onDistanceChanged(final Message message, final Distance distance) {
            logthis( "Distance changed, message: " + message + ", new distance: " + distance);
        }

    };

    // Subscribe to receive messages.
    private void subscribe() {
        //foreground subscription.
        logthis("Subscribing.");
         SubscribeOptions options = new SubscribeOptions.Builder()
            .setStrategy(Strategy.BLE_ONLY)
            .build();
        Nearby.getMessagesClient(getActivity()).subscribe(mMessageListener, options);
        mIsSubscribing = true;
    }

    // Subscribe to receive messages.
    private void unsubscribe() {
        logthis("Unsubscribe.");
        Nearby.getMessagesClient(getActivity()).unsubscribe(mMessageListener);
        mIsSubscribing = false;
    }

    // Subscribe to messages in the background.
    private void subscribeBG() {
        logthis( "Subscribing for background updates.");
        SubscribeOptions options = new SubscribeOptions.Builder()
            .setStrategy(Strategy.BLE_ONLY)
            .build();
        Nearby.getMessagesClient(getActivity()).subscribe(getPendingIntent(), options);
        mIsBGSubscribing = true;
    }

    private PendingIntent getPendingIntent() {
        return PendingIntent.getBroadcast(getContext(), 0, new Intent(getContext(), BeaconMessageReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // Subscribe to receive messages.
    private void unsubscribeBG() {
        logthis("Unsubscribe  for background updates..");
        Nearby.getMessagesClient(getActivity()).unsubscribe(getPendingIntent());
        mIsBGSubscribing = false;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_subscribe, container, false);
        logger = myView.findViewById(R.id.sub_output);

        myView.findViewById(R.id.fg_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSubscribing)
                    unsubscribe();
                else
                    subscribe();

            }
        });
        myView.findViewById(R.id.bg_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsBGSubscribing)
                    unsubscribeBG();
                else
                    subscribeBG();
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
