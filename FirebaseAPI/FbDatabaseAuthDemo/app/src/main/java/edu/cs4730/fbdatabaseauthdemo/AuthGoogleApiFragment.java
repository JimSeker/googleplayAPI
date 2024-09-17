package edu.cs4730.fbdatabaseauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import edu.cs4730.fbdatabaseauthdemo.databinding.FragmentAuthGoogleApiBinding;


/**
 * Uses the GoogleSignIn and GoogleSignInClient to sign into a google account
 * It then signs into firebase with that account.  since google sign in doesn't sign into firebase.
 */
public class AuthGoogleApiFragment extends Fragment {

    //local variables.
    private static final String TAG = "AuthFragment";
    private FragmentAuthGoogleApiBinding binding;
    private static final String ANONYMOUS = "anonymous";
    private String mPhotoUrl;
    private String mUsername;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    public AuthGoogleApiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAuthGoogleApiBinding.inflate(inflater, container, false);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        //get the sign in client.
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
        // google sign in button and pieces.
        binding.gSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        binding.gSignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });


        //using the new startActivityForResult method.
        myActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
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
                        binding.gSignInButton.setEnabled(true);
                    }
                }
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
            binding.gAccName.setText(mUsername);
            binding.gSignInButton.setEnabled(false);
        }
        return binding.getRoot();
    }


    void logthis(String item) {
        Log.wtf(TAG, item);
        binding.authglogger.append(item + "\n");
    }

    //start the signin to google authentication, when we will then sign into firebase (in onactivityresult method below)
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        myActivityResultLauncher.launch(signInIntent);
    }

    private void signOut() {
        //sign out of google
        mGoogleSignInClient.signOut();
        //sign out firebase, don't know if this is necessary, but seems like a good idea.
        mFirebaseAuth.signOut();

        mFirebaseUser = null;
        mUsername = ANONYMOUS;
        mPhotoUrl = null;
        binding.gAccName.setText(mUsername);
        //remove the listener or it will require the user to sign in again.  Not the action I want to here.
        binding.gSignInButton.setEnabled(true);
        logthis("Signed out");
    }

    /**
     * This is a helper function, so with have authentication with google, but we still need to
     * login to firebase.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.wtf(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
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
                    binding.gAccName.setText(mUsername);
                    binding.gSignInButton.setEnabled(false);
                }
            }
        });
    }
}
