package edu.cs4730.mapdemov2;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;


/**
 * Very little is happening here, except seating up the bottom nav view for the three
 * fragments.   check the basic for simple version, compass that shows which direction hybrid map, and drawmap for how to draw on a map.
 */

public class MainActivity extends AppCompatActivity {

    static final LatLng CHEYENNE = new LatLng(41.1400, -104.8197);  //Note, West is a negative, East is positive
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    static final LatLng LARAMIE = new LatLng(41.312928, -105.587253);

    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(navView, navController);
    }
}