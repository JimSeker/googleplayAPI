package edu.cs4730.nearbyconnectiondemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * This is the advertise side of the Nearby API.  (server)
 */
public class AdvertiseFragment extends Fragment {


    String TAG = "AdvertiseFragment";
    String UserNickName = "AdvertiseNearbyDemo"; //idk what this should be.  doc's don't say.
    TextView logger;
    boolean mIsAdvertising = false;

    String ConnectedEndPointId;


    public AdvertiseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_advertise, container, false);
        logger = myView.findViewById(R.id.ad_output);
        myView.findViewById(R.id.start_advertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsAdvertising)
                    stopAdvertising();  //already advertising, turn it off
                else
                    startAdvertising();
            }
        });
        myView.findViewById(R.id.end_advertise).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ConnectedEndPointId != null)
                    if (ConnectedEndPointId.compareTo("") != 0) { //connected to someone
                        Nearby.getConnectionsClient(requireContext()).disconnectFromEndpoint(ConnectedEndPointId);
                        ConnectedEndPointId = "";
                    }
                if (mIsAdvertising) {
                    stopAdvertising();
                }
            }
        });
        return myView;
    }

    /**
     * Callbacks for connections to other devices.  These call backs are used when a connection is initiated
     * and connection, and disconnect.
     * <p>
     * we auto accept any connection.  We with another callback that allows us to read the data.
     */
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
            @Override
            public void onConnectionInitiated(@NonNull String endpointId, ConnectionInfo connectionInfo) {
                logthis("Connection Initiated :" + endpointId + " Name is " + connectionInfo.getEndpointName());
                // Automatically accept the connection on both sides.
                // setups the callbacks to read data from the other connection.
                Nearby.getConnectionsClient(requireContext()).acceptConnection(endpointId, //mPayloadCallback);
                    new PayloadCallback() {
                        @Override
                        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {

                            if (payload.getType() == Payload.Type.BYTES) {
                                String stuff = new String(payload.asBytes());
                                logthis("Received data is " + stuff);
                                if (stuff.startsWith("Hi")) {
                                    send("Good to meet you Discovery");
                                }
                            } else if (payload.getType() == Payload.Type.FILE)
                                logthis("We got a file.  not handled");
                            else if (payload.getType() == Payload.Type.STREAM)
                                //payload.asStream().asInputStream()
                                logthis("We got a stream, not handled");
                        }

                        @Override
                        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate payloadTransferUpdate) {
                            //if stream or file, we need to know when the transfer has finished.  ignoring this right now.
                        }
                    });
            }

            @Override
            public void onConnectionResult(@NonNull String endpointId, ConnectionResolution result) {
                logthis("Connection accept :" + endpointId + " result is " + result.toString());

                switch (result.getStatus().getStatusCode()) {
                    case ConnectionsStatusCodes.STATUS_OK:
                        // We're connected! Can now start sending and receiving data.
                        ConnectedEndPointId = endpointId;
                        //if we don't then more can be added to conversation, when an List<string> of endpointIds to send to, instead a string.
                        // ... .add(endpointId);
                        stopAdvertising();  //and comment this out to allow more then one connection.
                        logthis("Status ok, sending Hi message");
                        send("Hi from Advertiser");
                        break;
                    case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                        logthis("Status rejected.  :(");
                        // The connection was rejected by one or both sides.
                        break;
                    case ConnectionsStatusCodes.STATUS_ERROR:
                        logthis("Status error.");
                        // The connection broke before it was able to be accepted.
                        break;
                }
            }

            @Override
            public void onDisconnected(@NonNull String endpointId) {
                logthis("Connection disconnected :" + endpointId);
                ConnectedEndPointId = "";  //need a remove if using a list.
            }
        };

    /**
     * Start advertising the nearby.  It sets the callback from above with what to once we get a connection
     * request.
     */
    private void startAdvertising() {

        Nearby.getConnectionsClient(requireContext())
            .startAdvertising(
                UserNickName,    //human readable name for the endpoint.
                MainActivity.ServiceId,  //unique identifier for advertise endpoints
                mConnectionLifecycleCallback,  //callback notified when remote endpoints request a connection to this endpoint.
                new AdvertisingOptions.Builder().setStrategy(MainActivity.STRATEGY).build()
            )
            .addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unusedResult) {
                        mIsAdvertising = true;
                        logthis("we're advertising!");
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mIsAdvertising = false;
                        // We were unable to start advertising.
                        logthis("we're failed to advertise");
                        e.printStackTrace();
                    }
                });
    }

    /**
     * turn off advertising.  Note, you can not add success and failure listeners.
     */
    public void stopAdvertising() {
        mIsAdvertising = false;
        Nearby.getConnectionsClient(requireContext()).stopAdvertising();
        logthis("Advertising stopped.");
    }


    /**
     * Sends a {@link Payload} to all currently connected endpoints.
     */
    protected void send(String data) {

        //basic error checking
        if (ConnectedEndPointId.compareTo("") == 0)   //empty string, no connection
            return;

        Payload payload = Payload.fromBytes(data.getBytes());

        // sendPayload (List<String> endpointIds, Payload payload)  if more then one connection allowed.
        Nearby.getConnectionsClient(requireContext()).
            sendPayload(ConnectedEndPointId,  //end point to end to
                payload)   //the actual payload of data to send.
            .addOnSuccessListener(new OnSuccessListener<Void>() {  //don't know if need this one.
                @Override
                public void onSuccess(Void aVoid) {
                    logthis("Message send successfully.");
                }
            })
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    logthis("Message send completed.");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Message send failed.");
                    e.printStackTrace();
                }
            });
    }


    @Override
    public void onStop() {
        super.onStop();
        stopAdvertising();
    }

    /**
     * helper function to log and add to a textview.
     */
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

}
