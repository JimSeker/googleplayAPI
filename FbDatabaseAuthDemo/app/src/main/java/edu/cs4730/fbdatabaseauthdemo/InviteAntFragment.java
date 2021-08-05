package edu.cs4730.fbdatabaseauthdemo;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.analytics.FirebaseAnalytics;


/**
 * This fragment show how to do invites and the custom Analytics
 * Note, custom analytics may take up to 24 to show up in the console.
 * Make sure the Dynamic Links is turned on first.
 */
public class InviteAntFragment extends Fragment {

    //local variables.
    private static String TAG = "InviteAntFragment";
    private TextView logger;

    private FirebaseAnalytics mFirebaseAnalytics;

    ActivityResultLauncher<Intent> myActivityResultLauncher;

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
                payload.putString("button_name", "cust");
                payload.putString("full_text", "generate stuff");
                mFirebaseAnalytics.logEvent("share_note", payload);
                logthis("Cust_button event updated");
            }
        });

        //using the new startActivityForResult method.
        myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Use Firebase Measurement to log that invitation was sent.
                        Bundle payload = new Bundle();
                        payload.putString(FirebaseAnalytics.Param.VALUE, "inv_sent");
                        //shouldn't we do a logevent here?

                        // Check how many invitations were sent and log.
                        String[] ids = AppInviteInvitation.getInvitationIds(result.getResultCode(), data);
                        logthis("Invitations sent: " + ids.length);
                    } else {
                        // Use Firebase Measurement to log that invitation was not sent
                        Bundle payload = new Bundle();
                        payload.putString(FirebaseAnalytics.Param.VALUE, "inv_not_sent");
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, payload);

                        // Sending failed or it was canceled, show failure message to the user
                        logthis("Failed to send invitation.");
                    }
                }
            });
        return myView;
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder("Notes Invite")
            .setMessage("Please join me in the notes app.")
            .setCallToActionText("Call to action")
            .build();
        myActivityResultLauncher.launch(intent);
    }

    void logthis(String item) {
        Log.d(TAG, item);
        logger.append(item + "\n");
    }
}
