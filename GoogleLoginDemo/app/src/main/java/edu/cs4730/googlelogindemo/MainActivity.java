package edu.cs4730.googlelogindemo;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.ClearCredentialStateRequest;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.ClearCredentialException;
import androidx.credentials.exceptions.GetCredentialException;

import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;

import edu.cs4730.googlelogindemo.databinding.ActivityMainBinding;

/**
 * a demo for logging into google with the google play services
 * https://developer.android.com/identity/sign-in/credential-manager-siwg
 * https://developers.google.com/identity/authorization/android
 *
 * note, currently I can't figure out how cancel/remove Authorization Request process programmatically.
 */


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    GetCredentialRequest request, request2;
    GetSignInWithGoogleOption signInWithGoogleOption;
    GetGoogleIdOption googleIdOption;
    String WEB_CLIENT_ID = "106312478965-2hob0i3m66dsseghi7uvqjfveihlt2c5.apps.googleusercontent.com";
    CredentialManager credentialManager;
    GoogleIdTokenCredential googleIdTokenCredential;
    int REQUEST_AUTHORIZE = 12;
    boolean failedtologin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        //signing in using onResume, use these.
        googleIdOption = new GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(WEB_CLIENT_ID)
            .setAutoSelectEnabled(true)

            .setNonce("This is a test string that needs be at least 16 characters.")
            .build();

        request = new GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build();
        credentialManager = CredentialManager.create(getApplicationContext());
        //Signing in with Button use these.
        signInWithGoogleOption = new GetSignInWithGoogleOption.Builder(WEB_CLIENT_ID)
            //.setServerClientId(WEB_CLIENT_ID)
            .setNonce("This is a test string that needs be at least 16 characters.")
            .build();
        request2 = new GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build();

        binding.signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ButtonsignIn();
            }
        });

        binding.signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authorize();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (googleIdTokenCredential == null && !failedtologin)
            signIn();
    }

    private void ButtonsignIn() {
        logthis("Starting Button Sign in");
        credentialManager.getCredentialAsync(
            this,
            request2,
            null,
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    logthis("Successful login");
                    handleSignIn(result);
                }

                @Override
                public void onError(GetCredentialException e) {
                    logthis("Failed to login " + e.getMessage());
                }
            });
    }

    private void signIn() {
        logthis("Starting onResume Sign in");
        credentialManager.getCredentialAsync(
            this,
            request,
            null,
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                @Override
                public void onResult(GetCredentialResponse result) {
                    logthis("Successful login");
                    handleSignIn(result);
                }

                @Override
                public void onError(GetCredentialException e) {
                    logthis("Failed to login " + e.getMessage());
                    failedtologin = true;
                }
            });
    }

    private void signOut() {
        credentialManager.clearCredentialStateAsync(
            new ClearCredentialStateRequest(),
            null,
            Executors.newSingleThreadExecutor(),
            new CredentialManagerCallback<Void, ClearCredentialException>() {
                @Override
                public void onResult(Void unused) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logthis("Successful logout");
                            googleIdTokenCredential = null;
                            updateUI(googleIdTokenCredential);
                        }
                    });
                }

                @Override
                public void onError(@NonNull ClearCredentialException e) {
                    logthis("Failed to log out " + e.getMessage());
                }
            }
        );
    }


    public void handleSignIn(GetCredentialResponse result) {
        Credential credential = result.getCredential();

        if (credential instanceof CustomCredential) {
            if (GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credential.getType())) {
                googleIdTokenCredential = GoogleIdTokenCredential.createFrom(((CustomCredential) credential).getData());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(googleIdTokenCredential);
                    }
                });

            }
        }
    }


    private void updateUI(@Nullable GoogleIdTokenCredential account) {
        if (account != null) {
            binding.signIn.setEnabled(false);
            binding.signOut.setEnabled(true);
            binding.button2.setEnabled(true);
            logthis("User: " + account.getDisplayName());

        } else {
            binding.signIn.setEnabled(true);
            binding.signOut.setEnabled(false);
            binding.button2.setEnabled(false);
            logthis("Signed Out");

        }
    }

    void logthis(String item) {
        runOnUiThread(new Runnable() {
                          @Override
                          public void run() {
                              binding.logger.append(item + "\n");
                          }
                      }

        );
    }


    void authorize() {
        List<Scope> requestedScopes = Arrays.asList(new Scope(Scopes.DRIVE_APPFOLDER));
        AuthorizationRequest authorizationRequest = AuthorizationRequest.builder().setRequestedScopes(requestedScopes).build();
        Identity.getAuthorizationClient(this)
            .authorize(authorizationRequest)
            .addOnSuccessListener(
                authorizationResult -> {
                    if (authorizationResult.hasResolution()) {
                        // Access needs to be granted by the user
                        PendingIntent pendingIntent = authorizationResult.getPendingIntent();
                        try {
                            startIntentSenderForResult(pendingIntent.getIntentSender(),
                                REQUEST_AUTHORIZE, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                           logthis("Error: "+ e.getMessage());
                        };

                    } else {
                        // Access already granted, continue with user action
                      logthis("already Authorized to access google drive.");
                    }
                })
            .addOnFailureListener(e -> logthis( "Failed to authorize"+ e.getMessage()));

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_AUTHORIZE) {
            try {
                AuthorizationResult authorizationResult = Identity.getAuthorizationClient(this).getAuthorizationResultFromIntent(data);
                for(String scope :  authorizationResult.getGrantedScopes()) {
                    logthis("Authorized: " + scope);
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

        }
    }


}
