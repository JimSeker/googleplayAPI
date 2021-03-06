package edu.cs4730.firebasefirestoredemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
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

/**
 * This is a simple example using the default data example from google.
 * It does require a google login in order to work (both read and write)
 *
 * This example will have you login first and you can read/write.  If you logout, then
 * you will not be to read/write the data anymore.
 *
 */
public class MainActivity extends AppCompatActivity {

    final static String TAG = "MainActivity";
    static final int RC_SIGN_IN = 9001;
    static final int RC_G_SIGN_IN = 9002;

    private TextView logger;
    private SignInButton mSignInButton;
    Button SignOutButton, addData_btn, listData_btn;

    //firestore and auth pieces.
    FirebaseFirestore db;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();

        //setup the auth listener for onresume to login.
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    logthis("username: " + mFirebaseUser.getDisplayName());
                    mSignInButton.setEnabled(false);
                } else {
                    // user is not signed in.
                    startActivityForResult(AuthUI.getInstance()  //see firebase UI for documentation.
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                )
                            )
                            .build(),
                        MainActivity.RC_SIGN_IN);
                }
            }
        };


        logger = findViewById(R.id.logger);
        // Assign fields
        mSignInButton = findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        SignOutButton = findViewById(R.id.sign_out_button);
        SignOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        addData_btn = findViewById(R.id.add_data);
        addData_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adddata();
            }
        });
        listData_btn = findViewById(R.id.getdata);
        listData_btn.setOnClickListener(new View.OnClickListener() {
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
        mSignInButton.setEnabled(true);
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
        db.collection("users")
            .add(user)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    logthis("DocumentSnapshot added with ID: " + documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis( "Error adding document"+ e);
                }
            });


        // Create a second user with a first, middle, and last name
        user = new HashMap<>();
        user.put("first", "Alan");
        user.put("middle", "Mathison");
        user.put("last", "Turing");
        user.put("born", 1912);

        // Add a new document with a generated ID
        db.collection("users")
            .add(user)
            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                   logthis("DocumentSnapshot added with ID: " + documentReference.getId());
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis( "Error adding document" + e);
                }
            });
    }

    /**
    * This is the code that the tab shows you to list the data in the firestore database.
    */
    void readdata() {
        db.collection("users")
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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


    /**
     * this is a helper function, OnActivityResult from the authentication (used to be only the activity was called!)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == MainActivity.RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {

                Toast.makeText(this, "Authentication success.", Toast.LENGTH_SHORT).show();
                logthis("Google Sign In success.");
                mFirebaseUser = mFirebaseAuth.getCurrentUser();
                logthis("username is" + mFirebaseUser.getDisplayName());
                mSignInButton.setEnabled(false);
            } else {
                // Google Sign In failed
                logthis("Google Sign In failed.");
                mSignInButton.setEnabled(true);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //helper function to log and added to textview.
    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }


}
