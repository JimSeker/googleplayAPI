package edu.cs4730.cloudmessagedemo;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import edu.cs4730.cloudmessagedemo.backend.registration.Registration;

public class MainActivity extends AppCompatActivity {
    //set to use an emulator.
    public static String SERVER_ADDR = "http://10.0.2.2:8080";

    String TAG = "MainActivity";
    TextView logger;
    Button SendMesg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = (TextView) findViewById(R.id.logger);

        //setup the button to send a message.
        SendMesg = (Button) findViewById(R.id.send);
        SendMesg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AsyncTask<Void, Void, String>() {

                    @Override
                    // Get history and upload it to the server.
                    protected String doInBackground(Void... arg0) {


                        // Upload the history of all entries using upload().
                        String uploadState="";
                        try {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("message", "Testing anyone out there!?");


                            ServerUtilities.post(SERVER_ADDR+"/add.do", params);
                        } catch (IOException e1) {
                            uploadState = "Sync failed: " + e1.getCause();
                            Log.e(TAG, "data posting error " + e1);
                        }

                        return uploadState;
                    }

                    @Override
                    protected void onPostExecute(String errString) {
                        String resultString;
                        if(errString.equals("")) {
                            resultString =  " entry uploaded.";
                        } else {
                            resultString = errString;
                        }

                        Toast.makeText(getApplicationContext(), resultString,
                                Toast.LENGTH_SHORT).show();

                    }

                }.execute();
            }
        });
        //registar the with google.
        new GcmRegistrationAsyncTask(this).execute();
    }


    class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
        private Registration regService = null;
        private GoogleCloudMessaging gcm;
        private Context context;

        // TODO: change to your own sender ID to Google Developers Console project number, as per instructions above
        private static final String SENDER_ID = "370173421652";

        public GcmRegistrationAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(),
                        new AndroidJsonFactory(), null)
                        // Need setRootUrl and setGoogleClientRequestInitializer only for local testing,
                        // otherwise they can be skipped
                        .setRootUrl(SERVER_ADDR+"/_ah/api/")
                        .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                            @Override
                            public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                    throws IOException {
                                abstractGoogleClientRequest.setDisableGZipContent(true);
                            }
                        });
                // end of optional local run code

                regService = builder.build();
            }

            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                String regId = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + regId;

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                regService.register(regId).execute();

            } catch (IOException ex) {
                ex.printStackTrace();
                msg = "Error: " + ex.getMessage();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            logger.append(msg + "\n");
        }
    }
}
