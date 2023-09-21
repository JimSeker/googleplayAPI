package edu.cs4730.googlelogindemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import edu.cs4730.googlelogindemo.databinding.ActivityMainBinding;

/**
 * a demo for logging into google with the google play services
 * https://developers.google.com/identity/sign-in/android/start
 * https://github.com/googlesamples/google-services/tree/master/android/signin/app/src/main/java/com/google/samples/quickstart/signin
 */


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        binding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignInActivity.class));
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignInActivityWithDrive.class));
            }
        });

    }

}
