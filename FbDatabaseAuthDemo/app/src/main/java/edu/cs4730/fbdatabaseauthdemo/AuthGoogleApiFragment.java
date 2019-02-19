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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


/**
 * Uses the GoogleSignIn and GoogleSignInClient to sign into a google account
 * It then signs into firebase with that account.  since google sign in doesn't sign into firebase.
 */
public class AuthGoogleApiFragment extends Fragment {

    //local variables.
    private static String TAG = "AuthFragment";
    private TextView logger;

    private static final String ANONYMOUS = "anonymous";
    private String mPhotoUrl;
    private String mUsername;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private GoogleSignInClient mGoogleSignInClient;

    private SignInButton mSignInButton;
    private TextView mSignInTV;

    public AuthGoogleApiFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_auth_google_api, container, false);

        //logger
        logger = myView.findViewById(R.id.authglogger);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        //get the sign in client.
        mGoogleSignInClient = GoogleSignIn.getClient(getContext(), gso);

        // google sign in button and pieces.
        mSignInButton = myView.findViewById(R.id.g_sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });


        mSignInTV = myView.findViewById(R.id.g_acc_name);
        myView.findViewById(R.id.g_sign_out_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in,
            // normally you would launch the Sign In, if it's required.

        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
            mSignInTV.setText(mUsername);
            mSignInButton.setEnabled(false);
        }

        return myView;
    }


    void logthis(String item) {
        Log.wtf(TAG, item);
        logger.append(item + "\n");
    }

    //start the signin to google authentication, when we will then sign into firebase (in onactivityresult method below)
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, MainActivity.RC_G_SIGN_IN);
    }

    private void signOut() {
        //sign out of google
        mGoogleSignInClient.signOut();
        //sign out firebase, don't know if this is necessary, but seems like a good idea.
        mFirebaseAuth.signOut();

        mFirebaseUser = null;
        mUsername = ANONYMOUS;
        mPhotoUrl = null;
        mSignInTV.setText(mUsername);
        //remove the listener or it will require the user to sign in again.  Not the action I want to here.
        mSignInButton.setEnabled(true);
        logthis("Signed out");
    }

    /**
     * So this where the result will come back to from the sign in call.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == MainActivity.RC_G_SIGN_IN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                logthis("successful login, now firebase.");
                // Google Sign In was successful, authenticate with Firebase
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // The ApiException status code indicates the detailed failure reason.
                // Please refer to the GoogleSignInStatusCodes class reference for more information.
                Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                logthis("signInResult:failed code=" + e.getStatusCode());
                mSignInButton.setEnabled(true);
            }
        }
    }

    /**
     * This is a helper function, so with have authentication with google, but we still need to
     * login to firebase.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.wtf(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        logthis("signinWithCredntial failed");
                        Log.w(TAG, "signInWithCredential", task.getException());
                    } else {
                        logthis("SignIn Success");
                        mFirebaseUser = mFirebaseAuth.getCurrentUser();
                        mUsername = mFirebaseUser.getDisplayName();
                        logthis("username is" + mUsername);
                        mSignInTV.setText(mUsername);
                        mSignInButton.setEnabled(false);
                    }
                }
            });
    }

}
