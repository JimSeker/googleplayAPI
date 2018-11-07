package edu.cs4730.fbdatabaseauthdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import static android.app.Activity.RESULT_OK;


/**
 * This is a simple fragment to show to use Firebase Authentication pieces.
 * This does not use GoogleAPI auth, just Firebase.
 */
public class AuthFragment extends Fragment {
    //local variables.
    private static String TAG = "AuthFragment";
    private TextView logger;

    public static final String ANONYMOUS = "anonymous";
    private String mPhotoUrl;
    private String mUsername;

    // private SharedPreferences mSharedPreferences;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    private SignInButton mSignInButton;
    private TextView mSignInTV;


    public AuthFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_auth, container, false);
        logger = myView.findViewById(R.id.authlogger);
        // Assign fields
        mSignInButton = myView.findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mSignInTV = myView.findViewById(R.id.acc_name);
        myView.findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    mUsername = mFirebaseUser.getDisplayName();
                    if (mFirebaseUser.getPhotoUrl() != null) {
                        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                    }
                    mSignInTV.setText(mUsername);
                    mSignInButton.setEnabled(false);
                } else {
                    // user is not signed in.
                    startActivityForResult(AuthUI.getInstance()  //see firebase UI for documentation.
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().build(),
                                new AuthUI.IdpConfig.GoogleBuilder().build(),
                                new AuthUI.IdpConfig.PhoneBuilder().build()
                                )
                            )
                            .build(),
                        MainActivity.RC_SIGN_IN);
                }
            }
        };


        // mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUsername = ANONYMOUS;
        mSignInTV.setText(mUsername);


        return myView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void signIn() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void signOut() {
        mFirebaseAuth.signOut();  //just signs out, but doesn't clear the user if they want to login with same name.
        AuthUI.getInstance().signOut(getContext());  //clears username and everything from google signin
        mFirebaseUser = null;
        mUsername = ANONYMOUS;
        mPhotoUrl = null;
        mSignInTV.setText(mUsername);
        //remove the listener or it will require the user to sign in again.  Not the action I want to here.
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        mSignInButton.setEnabled(true);
        logthis("Signed out");
    }

    @Override
    public void onPause() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onPause();
    }


    /**
     * this is a helper function, that is called byt eh OnActivityResult function from MainActivity
     * since it isn't called to the fragments.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == MainActivity.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                Toast.makeText(getContext(), "Authentication success.", Toast.LENGTH_SHORT).show();
                logthis("Google Sign In success.");
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                mUsername = mFirebaseUser.getDisplayName();
                logthis ("username is" + mUsername);
                mSignInTV.setText(mUsername);
                mSignInButton.setEnabled(false);
            } else {
                // Google Sign In failed
                Toast.makeText(getContext(), "Authentication failed.",
                    Toast.LENGTH_SHORT).show();
                logthis("Google Sign In failed.");
                mSignInButton.setEnabled(true);
            }
        }
    }

    void logthis(String item) {
        Log.d(TAG,  item);
        logger.append(item + "\n");
    }
}
