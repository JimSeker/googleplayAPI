package edu.cs4730.firebasemessagedemo;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;

import edu.cs4730.firebasemessagedemo.databinding.ActivitySendBinding;


/**
 * this activity is used to send the message to one or all devices.
 * To do this, it must pull a list of all the registered devices off the server and put it into the
 * spinner.
 * <p>
 * This sends a simple text message (in json).  But much more complex information can be send, like
 * images, etc.
 */

public class SendActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    String TAG = "SendActivity";
    private ActivitySendBinding binding;
    //private RadioGroup radioGroup;
    //private Spinner spinner;
    //private ProgressDialog progressDialog;

    //private EditText editTextTitle, editTextMessage;

    private boolean isSendAllChecked;
    private List<String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        radioGroup = findViewById(R.id.radioGroup);
//        spinner = findViewById(R.id.spinnerDevices);
//        buttonSendPush = findViewById(R.id.buttonSendPush);
//
//        editTextTitle = findViewById(R.id.editTextTitle);
//        editTextMessage = findViewById(R.id.editTextMessage);


        devices = new ArrayList<>();

        binding.radioGroup.setOnCheckedChangeListener(this);
        binding.buttonSendPush.setOnClickListener(this);

        loadRegisteredDevices();
    }
    //helper method
    void logthis(String item) {
        Log.d(TAG, item);
        binding.logger.append("\n");
        binding.logger.append(item);
    }

    /**
     * method to load all the devices from database on the backend server.
     * see the php code in the directory for more details about the backend.
     */
    private void loadRegisteredDevices() {
        logthis("Fetching Devices...");

        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoints.URL_FETCH_DEVICES,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject obj = null;
                    try {
                        obj = new JSONObject(response);
                        if (!obj.getBoolean("error")) {
                            JSONArray jsonDevices = obj.getJSONArray("devices");

                            for (int i = 0; i < jsonDevices.length(); i++) {
                                JSONObject d = jsonDevices.getJSONObject(i);
                                devices.add(d.getString("name"));
                            }

                            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                SendActivity.this,
                                android.R.layout.simple_spinner_dropdown_item,
                                devices);

                            binding.spinnerDevices.setAdapter(arrayAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {

        };
        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    /**
     * this method will send the push
     * from here we will call sendMultiple() or sendSingle() push method
     * depending on the selection
     */
    private void sendPush() {
        if (isSendAllChecked) {
            sendMultiplePush();
        } else {
            sendSinglePush();
        }
    }

    //this method will send a message to all registered devices.
    private void sendMultiplePush() {
        final String title = binding.editTextTitle.getText().toString();
        final String message = binding.editTextMessage.getText().toString();

        logthis("Sending Push");


        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_MULTIPLE_PUSH,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    logthis( response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("message", message);

                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    //this will send a message to only the device/token listed.
    private void sendSinglePush() {
        final String title = binding.editTextTitle.getText().toString();
        final String message = binding.editTextMessage.getText().toString();
        final String name = binding.spinnerDevices.getSelectedItem().toString();

        logthis("Sending Push");

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_SINGLE_PUSH,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    logthis( response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("title", title);
                params.put("message", message);
                params.put("name", name);
                return params;
            }
        };

        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.radioButtonSendAll:
                isSendAllChecked = true;
                binding.spinnerDevices.setEnabled(false);
                break;

            case R.id.radioButtonSendOne:
                isSendAllChecked = false;
                binding.spinnerDevices.setEnabled(true);
                break;

        }
    }

    @Override
    public void onClick(View view) {
        //calling the method send push on button click
        sendPush();
    }
}
