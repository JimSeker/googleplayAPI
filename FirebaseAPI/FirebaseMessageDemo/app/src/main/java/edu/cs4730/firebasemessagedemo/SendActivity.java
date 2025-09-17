package edu.cs4730.firebasemessagedemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

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

public class SendActivity extends AppCompatActivity {
    String TAG = "SendActivity";
    private ActivitySendBinding binding;

    private boolean isSendAllChecked;
    private List<String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });

        devices = new ArrayList<>();

        binding.radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull RadioGroup radioGroup, int i) {
                //figure out which radio button is checked and enable/disable the spinner as needed.
                //setting which method to call when the send button is clicked.
                if (radioGroup.getCheckedRadioButtonId() == binding.radioButtonSendAll.getId()) {
                    isSendAllChecked = true;
                    binding.spinnerDevices.setEnabled(false);
                } else if (radioGroup.getCheckedRadioButtonId() == binding.radioButtonSendOne.getId()) {
                    isSendAllChecked = false;
                    binding.spinnerDevices.setEnabled(true);
                }
            }
        });
        binding.buttonSendPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //calling the method send push on button click
                if (isSendAllChecked) {
                    sendMultiplePush();
                } else {
                    sendSinglePush();
                }
            }
        });


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

        StringRequest stringRequest = new StringRequest(Request.Method.GET, EndPoints.URL_FETCH_DEVICES, new Response.Listener<String>() {
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

                        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(SendActivity.this, android.R.layout.simple_spinner_dropdown_item, devices);

                        binding.spinnerDevices.setAdapter(arrayAdapter);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };
        MyVolley.getInstance(this).addToRequestQueue(stringRequest);
    }

    //this method will send a message to all registered devices.
    private void sendMultiplePush() {
        final String title = binding.editTextTitle.getText().toString();
        final String message = binding.editTextMessage.getText().toString();

        logthis("Sending Push");


        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_MULTIPLE_PUSH, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                logthis(response);
            }
        }, new Response.ErrorListener() {
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
        String endpoint = EndPoints.URL_SEND_SINGLE_PUSH + name;
        //StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_SINGLE_PUSH,
        StringRequest stringRequest = new StringRequest(Request.Method.POST, endpoint, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                logthis(response);
            }
        }, new Response.ErrorListener() {
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


}
