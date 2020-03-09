package edu.cs4730.viewbindingdemo;

import androidx.appcompat.app.AppCompatActivity;
import edu.cs4730.viewbindingdemo.databinding.ActivityMainBinding;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        setContentView(view);
    }
}
