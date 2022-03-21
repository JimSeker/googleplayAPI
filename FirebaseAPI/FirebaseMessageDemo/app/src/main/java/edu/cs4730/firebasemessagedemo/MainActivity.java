package edu.cs4730.firebasemessagedemo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

// These sites were used in creating this example code
//       https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/
//   and https://www.simplifiedcoding.net/firebase-cloud-messaging-android/
//   and https://www.androidtutorialpoint.com/firebase/firebase-android-tutorial-getting-started/
//   and https://www.androidtutorialpoint.com/firebase/firebase-cloud-messaging-tutorial/
//   and https://firebase.google.com/docs/cloud-messaging/

/**
 * The mainactivity only registers the device with the back end server.  It only needs to be done
 * once (unless the token changes, which I don't think it does).  The user then can push the button
 * to move to the sendactivity.
 * <p>
 * honesty this code needs rewritten to use two fragments instead of two activities.
 */


public class MainActivity extends AppCompatActivity {

    private Button buttonSendPush;
    private Button buttonRegister;
    private EditText editTextName;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get the views
        editTextName = findViewById(R.id.editTextName);
        buttonRegister = findViewById(R.id.buttonRegister);
        buttonSendPush = findViewById(R.id.buttonSendNotification);

        //adding listener to view
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTokenToServer();

            }
        });
        buttonSendPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SendActivity.class));

            }
        });
        //create channels for the notifications.
        createchannel();
    }

    /**
     * This will send the token to the backend server (which is mysql/php/apache.  see
     * the php directory in this project for the source code.
     */
    private void sendTokenToServer() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering Device...");
        progressDialog.show();

        final String token = SharedPrefManager.getInstance(this).getDeviceToken();
        final String name = editTextName.getText().toString();

        if (token == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Token not generated", Toast.LENGTH_LONG).show();
            return;
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_REGISTER_DEVICE,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    try {
                        JSONObject obj = new JSONObject(response);

                        if (obj.getString("message").compareTo("Device already registered") == 0) {
                            //Toast.makeText(MainActivity.this, "warning, device name already registered.", Toast.LENGTH_LONG).show();
                            verifyRegistration(name, token);
                        } else {
                            Toast.makeText(MainActivity.this, obj.getString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("token", token);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    /**
     * this is called when the device is already registered to verify that the name and token match.
     */
    private void verifyRegistration(String name, String token) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Verifying name and Token ...");
        progressDialog.show();
        final String thisName = name;
        final String thisToken = token;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoints.URL_FETCH_DEVICES,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressDialog.dismiss();
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONArray jsonDevices = obj.getJSONArray("devices");

                            for (int i = 0; i < jsonDevices.length(); i++) {
                                JSONObject d = jsonDevices.getJSONObject(i);
                                if (d.getString("name").compareTo(thisName) == 0) {
                                    if (d.getString("token").compareTo(thisToken) == 0) {
                                        Toast.makeText(MainActivity.this, "Device already registered", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "Name registered doesn't make our Token.", Toast.LENGTH_LONG).show();
                                    }
                                    return;
                                }
                            }
                            Toast.makeText(MainActivity.this, "Our Name is not registered", Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
                }
            }) {
        };
        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    /**
     * for API 26+ create notification channels
     */
    private void createchannel() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel mChannel = new NotificationChannel(getString(R.string.default_notification_channel_id),
            getString(R.string.channel_name),  //name of the channel
            NotificationManager.IMPORTANCE_DEFAULT);   //importance level
        //important level: default is is high on the phone.  high is urgent on the phone.  low is medium, so none is low?
        // Configure the notification channel.
        mChannel.setDescription(getString(R.string.channel_description));
        mChannel.enableLights(true);
        //Sets the notification light color for notifications posted to this channel, if the device supports this feature.
        mChannel.setLightColor(Color.RED);
        mChannel.enableVibration(true);
        mChannel.setShowBadge(true);
        mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        nm.createNotificationChannel(mChannel);
    }
}
