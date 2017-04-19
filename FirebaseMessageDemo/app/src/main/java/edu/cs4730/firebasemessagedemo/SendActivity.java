package edu.cs4730.firebasemessagedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;


import java.util.ArrayList;
import java.util.List;

import java.util.HashMap;
import java.util.Map;


/*
 * this activity is used to send the message to one or all devices.
 * To do this, it must pull a list of all the registered devices off the server and put it into the
 * spinner.
 *
 * This sends a simple text message (in json).  But much more complex information can be send, like
 * images, etc.
 */

public class SendActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, View.OnClickListener {

    private Button buttonSendPush;
    private RadioGroup radioGroup;
    private Spinner spinner;
    private ProgressDialog progressDialog;

    private EditText editTextTitle, editTextMessage;

    private boolean isSendAllChecked ;
    private List<String> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        spinner = (Spinner) findViewById(R.id.spinnerDevices);
        buttonSendPush = (Button) findViewById(R.id.buttonSendPush);

        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        editTextMessage = (EditText) findViewById(R.id.editTextMessage);


        devices = new ArrayList<>();

        radioGroup.setOnCheckedChangeListener(this);
        buttonSendPush.setOnClickListener(this);

        loadRegisteredDevices();
    }

    //method to load all the devices from database on the backend server.
    //see the php code in the directory for more details about the backend.
    private void loadRegisteredDevices() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching Devices...");
        progressDialog.show();

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
                                    devices.add(d.getString("name"));
                                }

                                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                                        SendActivity.this,
                                        android.R.layout.simple_spinner_dropdown_item,
                                        devices);

                                spinner.setAdapter(arrayAdapter);
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

    //this method will send the push
    //from here we will call sendMultiple() or sendSingle() push method
    //depending on the selection
    private void sendPush(){
        if(isSendAllChecked){
            sendMultiplePush();
        }else{
            sendSinglePush();
        }
    }

    //this method will send a message to all registered devices.
    private void sendMultiplePush(){
        final String title = editTextTitle.getText().toString();
        final String message = editTextMessage.getText().toString();

        progressDialog.setMessage("Sending Push");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_MULTIPLE_PUSH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        Toast.makeText(SendActivity.this, response, Toast.LENGTH_LONG).show();
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
    private void sendSinglePush(){
        final String title = editTextTitle.getText().toString();
        final String message = editTextMessage.getText().toString();
        final String name = spinner.getSelectedItem().toString();

        progressDialog.setMessage("Sending Push");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, EndPoints.URL_SEND_SINGLE_PUSH,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();

                        Toast.makeText(SendActivity.this, response, Toast.LENGTH_LONG).show();
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
                spinner.setEnabled(false);
                break;

            case R.id.radioButtonSendOne:
                isSendAllChecked = false;
                spinner.setEnabled(true);
                break;

        }
    }

    @Override
    public void onClick(View view) {
        //calling the method send push on button click
        sendPush();
    }
}
