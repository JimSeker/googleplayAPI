package edu.cs4730.firebasefirestoredemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.cs4730.firebasefirestoredemo.databinding.ActivityMainBinding;

/**
 * This is a simple example using the default data example from google.
 * It does require a google login in order to work (both read and write)
 * <p>
 * This example will have you login first and you can read/write.  If you logout, then
 * you will not be to read/write the data anymore.
 * Note to self, under the firebaseMLKit project.  check for analytics pieces.
 */
public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    private ActivityMainBinding binding;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    //firestore and auth pieces.
    FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private String mUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //setup the auth listener for onresume to login.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    logthis("username: " + mFirebaseUser.getDisplayName());
                    binding.signInButton.setEnabled(false);
                } else {
                    // user is not signed in.
                    myActivityResultLauncher.launch(AuthUI.getInstance()  //see firebase UI for documentation.
                        .createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .setAvailableProviders(Arrays.asList(
                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.PhoneBuilder().build()
                        )).build());
                }
            }
        };

        //using the new startActivityForResult method.
        myActivityResultLauncher = registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
            new ActivityResultCallback<FirebaseAuthUIAuthenticationResult>() {
            @Override
            public void onActivityResult(FirebaseAuthUIAuthenticationResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    // Successfully signed in
                    Toast.makeText(MainActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                    logthis("Google Sign In success.");
                    if (mFirebaseUser.getDisplayName() != null) {
                        mUsername = mFirebaseUser.getDisplayName();
                    } else if (mFirebaseUser.getEmail() != null) {
                        mUsername = mFirebaseUser.getEmail();
                    } else {
                        mUsername = "Unknown User";
                    }
                    mFirebaseUser = mFirebaseAuth.getCurrentUser();
                    logthis("username is" + mUsername);
                    binding.signInButton.setEnabled(false);
                } else {
                    // Google Sign In failed
                    logthis("Google Sign In failed.");
                    binding.signInButton.setEnabled(true);
                }
            }
        });
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
        binding.addData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adddata();
            }
        });
        binding.getdata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readdata();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    public void onPause() {
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        super.onPause();
    }

    private void signIn() {
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    private void signOut() {
        mFirebaseAuth.signOut();  //just signs out, but doesn't clear the user if they want to login with same name.
        AuthUI.getInstance().signOut(this);  //clears username and everything from google signin
        mFirebaseUser = null;
        //remove the listener or it will require the user to sign in again.  Not the action I want to here.
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        binding.signInButton.setEnabled(true);
        logthis("Signed out");
    }


    /**
     * This is literally using the code that the firestore tab (in studio) shows you.
     */
    void adddata() {
        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("first", "Ada");
        user.put("last", "Lovelace");
        user.put("born", 1815);

        // Add a new document with a generated ID
        db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                logthis("DocumentSnapshot added with ID: " + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("Error adding document" + e);
            }
        });
        // Create a second user with a first, middle, and last name
        user = new HashMap<>();
        user.put("first", "Alan");
        user.put("middle", "Mathison");
        user.put("last", "Turing");
        user.put("born", 1912);

        // Add a new document with a generated ID
        db.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                logthis("DocumentSnapshot added with ID: " + documentReference.getId());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("Error adding document" + e);
            }
        });
    }

    /**
     * This is the code that the tab shows you to list the data in the firestore database.
     */
    void readdata() {
        db.collection("users").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        logthis(document.getId() + " => " + document.getData());
                    }
                } else {
                    logthis("Error getting documents." + task.getException());
                }
            }
        });
    }

    //helper function to log and added to textview.
    public void logthis(String msg) {
        binding.logger.append(msg + "\n");
        Log.d(TAG, msg);
    }


}
