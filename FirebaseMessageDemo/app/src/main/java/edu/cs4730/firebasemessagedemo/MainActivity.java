package edu.cs4730.firebasemessagedemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// using https://www.simplifiedcoding.net/firebase-cloud-messaging-tutorial-android/
//   and https://www.simplifiedcoding.net/firebase-cloud-messaging-android/
//   and https://www.androidtutorialpoint.com/firebase/firebase-android-tutorial-getting-started/
//   and https://www.androidtutorialpoint.com/firebase/firebase-cloud-messaging-tutorial/
//   and https://firebase.google.com/docs/cloud-messaging/


public class MainActivity extends AppCompatActivity {

    private Button buttonDisplayToken;
    private TextView textViewToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //getting views from xml
        textViewToken = (TextView) findViewById(R.id.textViewToken);
        buttonDisplayToken = (Button) findViewById(R.id.buttonDisplayToken);

        //adding listener to view
        buttonDisplayToken.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == buttonDisplayToken) {
                    //getting token from shared preferences
                    String token = SharedPrefManager.getInstance(getApplicationContext()).getDeviceToken();

                    //if token is not null
                    if (token != null) {
                        //displaying the token
                        textViewToken.setText(token);
                    } else {
                        //if token is null that means something wrong
                        textViewToken.setText("Token not generated");
                    }
                }
            }
        });
    }
}
