package edu.cs4730.fbdatabaseauthdemo;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;

import static android.app.Activity.RESULT_OK;


/**
 * This fragment show how to do invites and the custom Analytics
 *
 * Note, custom analytics may take up to 24 to show up in the console.
 *
 * Make sure the Dynamic Links is turned on first.
 */
public class InviteAntFragment extends Fragment {

    //local variables.
    private static String TAG = "InviteAntFragment";
    private TextView logger;

    private FirebaseAnalytics mFirebaseAnalytics;

    public InviteAntFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_invite_ant, container, false);

        // Initialize Firebase Measurement.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

        logger = myView.findViewById(R.id.logger_ant);

        myView.findViewById(R.id.btn_invite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendInvitation();
            }
        });

        myView.findViewById(R.id.btn_cust).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle payload = new Bundle();
               // payload.putString(FirebaseAnalytics.Param.VALUE, "cust_button");
               // mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);
                payload .putString("button_name", "cust");
                payload .putString("full_text", "generate stuff");
                mFirebaseAnalytics.logEvent("share_note",  payload );
                logthis("Cust_button event updated");
            }
        });


        return myView;
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Notes Invite")
            .setMessage("Please join me in the notes app.")
            .setCallToActionText("Call to action")
            .build();
        startActivityForResult(intent, MainActivity.RC_INVITE);
    }

    /**
     * this is a helper function, OnActivityResult from the authentication (used to be only the activity was called!)
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == MainActivity.RC_INVITE) {
            if (resultCode == RESULT_OK) {

                // Use Firebase Measurement to log that invitation was sent.
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");
                //shouldn't we do a logevent here?

                // Check how many invitations were sent and log.
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                logthis("Invitations sent: " + ids.length);
            } else {
                // Use Firebase Measurement to log that invitation was not sent
                Bundle payload = new Bundle();
                payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                // Sending failed or it was canceled, show failure message to the user
                logthis( "Failed to send invitation.");
            }
        }
    }


    void logthis(String item) {
        Log.d(TAG, item);
        logger.append(item + "\n");
    }
}
