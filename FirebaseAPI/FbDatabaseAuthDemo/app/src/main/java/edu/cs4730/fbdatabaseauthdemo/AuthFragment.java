package edu.cs4730.fbdatabaseauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import edu.cs4730.fbdatabaseauthdemo.databinding.FragmentAuthBinding;


/**
 * This is a simple fragment to show to use Firebase Authentication pieces.
 * This does not use GoogleAPI auth, just Firebase.
 */
public class AuthFragment extends Fragment {
    //local variables.
    private static String TAG = "AuthFragment";
    private FragmentAuthBinding binding;
    public static final String ANONYMOUS = "anonymous";
    private String mPhotoUrl;
    private String mUsername;

    // private SharedPreferences mSharedPreferences;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    ActivityResultLauncher<Intent> myActivityResultLauncher;

    public AuthFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAuthBinding.inflate(inflater, container, false);
        // Assign fields
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        //using the new startActivityForResult method.
        myActivityResultLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
                @Override
                public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Successfully signed in
                        Toast.makeText(getContext(), "Authentication success.", Toast.LENGTH_SHORT).show();
                        logthis("Google Sign In success.");
                        mFirebaseUser = mFirebaseAuth.getCurrentUser();
                        if (mFirebaseUser != null) {
                            logthis("Error: No user");
                        } else {
                           getUsername();
                            logthis("username is" + mUsername);
                            binding.accName.setText(mUsername);
                            binding.signInButton.setEnabled(false);
                        }
                    } else {
                        // Google Sign In failed
                        // Toast.makeText(getContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        logthis("Google Sign In failed.");
                        binding.signInButton.setEnabled(true);
                    }

                }
            }
        );


        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {


                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    getUsername();
                    logthis("Username " + mUsername + " is signed in.");
                    if (mFirebaseUser.getPhotoUrl() != null) {
                        mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
                    }
                    binding.accName.setText(mUsername);
                    binding.signInButton.setEnabled(false);
                } else {
                    // user is not signed in.
                    myActivityResultLauncher.launch(AuthUI.getInstance()  //see firebase UI for documentation.
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.PhoneBuilder().build()
                        )).build());
                }
            }
        };


        mUsername = ANONYMOUS;
        binding.accName.setText(mUsername);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

        if (mFirebaseAuth.getCurrentUser() != null) {
            logthis("currently logged in");
        } else {
            logthis("not logged in");
        }
    }

    private void signIn() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void signOut() {
        mFirebaseAuth.signOut();  //just signs out, but doesn't clear the user if they want to login with same name.
        AuthUI.getInstance().signOut(requireContext());  //clears username and everything from google signin
        mFirebaseUser = null;
        mUsername = ANONYMOUS;
        mPhotoUrl = null;
        binding.accName.setText(mUsername);
        //remove the listener or it will require the user to sign in again.  Not the action I want to here.
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        binding.signInButton.setEnabled(true);
        logthis("Signed out");
    }

    @Override
    public void onPause() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onPause();
    }

    void getUsername() {
        if (mFirebaseUser == null) {
            logthis("Error: No user");
            mUsername = "No User";
        } else {
            logthis("User id is " + mFirebaseUser.getUid());
            logthis("User email is " + mFirebaseUser.getEmail());
            logthis("User name is " + mFirebaseUser.getDisplayName());
            if (mFirebaseUser.getDisplayName() != null) {
                mUsername = mFirebaseUser.getDisplayName();
            } else if (mFirebaseUser.getEmail() != null) {
                mUsername = mFirebaseUser.getEmail();
            } else {
                mUsername = "Unknown User";
            }
        }
    }

    /**
     * this is a helper function
     */
    void logthis(String item) {
        Log.d(TAG, item);
        binding.authlogger.append(item + "\n");
    }
}
