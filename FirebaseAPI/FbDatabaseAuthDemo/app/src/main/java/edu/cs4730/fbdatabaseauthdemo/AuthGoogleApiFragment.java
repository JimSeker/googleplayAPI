package edu.cs4730.fbdatabaseauthdemo;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;

import android.os.CancellationSignal;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import static com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.concurrent.Executors;

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
    private CredentialManager credentialManager;
    private GoogleSignInClient mGoogleSignInClient;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    public AuthGoogleApiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAuthGoogleApiBinding.inflate(inflater, container, false);

        credentialManager = CredentialManager.create(requireContext());

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


        // Initialize everything else.
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

        // Instantiate a Google sign-in request
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(getString(R.string.default_web_client_id))
            .build();

        // Create the Credential Manager request
        GetCredentialRequest request = new GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build();


        // Launch Credential Manager UI
        credentialManager.getCredentialAsync(
            requireContext(),
            request,
            new CancellationSignal(),
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    // Extract credential from the result returned by Credential Manager
                    logthis("successful login, now firebase.");
                    handleSignIn(result.getCredential());
                }

                @Override
                public void onError(@NonNull GetCredentialException e) {
                    Log.e(TAG, "Couldn't retrieve user's credentials: " + e.getLocalizedMessage());
                    //Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        logthis("signInResult:failed code=" + e.getLocalizedMessage());
                        binding.gSignInButton.setEnabled(true);
                }
            }
        );
    }

    private void signOut() {
        //sign out of google
        // When a user signs out, clear the current user credential state from all credential providers.
        ClearCredentialStateRequest clearRequest = new ClearCredentialStateRequest();
        credentialManager.clearCredentialStateAsync(
            clearRequest,
            new CancellationSignal(),
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<>() {
                @Override
                public void onResult(@NonNull Void result) {
                    logthis("Signed out of google");
                }

                @Override
                public void onError(@NonNull ClearCredentialException e) {
                    Log.e(TAG, "Couldn't clear user credentials: " + e.getLocalizedMessage());
                }
            });
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

    private void handleSignIn(Credential credential) {
        // Check if credential is of type Google ID
        if (credential instanceof CustomCredential customCredential
            && credential.getType().equals(TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) {
            // Create Google ID Token
            Bundle credentialData = customCredential.getData();
            GoogleIdTokenCredential googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credentialData);

            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.getIdToken());
        } else {
            Log.w(TAG, "Credential is not of type Google ID!");
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        Log.wtf(TAG, "firebaseAuthWithGoogle:" + idToken);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
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
                    mUsername = mFirebaseUser.getDisplayName() == null ? "No Display Name" : mFirebaseUser.getDisplayName();
                    logthis("username is" + mUsername);
                    binding.gAccName.setText(mUsername);
                    binding.gSignInButton.setEnabled(false);
                }
            }
        });
    }
}
