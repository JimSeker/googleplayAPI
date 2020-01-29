package edu.cs4730.fitdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Most of the code is in the fragments.
 *
 * But this activity is asking for the new ACTIVITY permission that started in API 29.  Not sure
 * what happens if this is run on a 28 device.
 */

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager;
    SensorFragment sensorFragment;
    RecordFragment recordFragment = null;
    public static final int REQUEST_ACCESS_Activity_Updates = 0;
    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //first instance, so the default is zero.
        sensorFragment = new SensorFragment();
        recordFragment = new RecordFragment();
        fragmentManager.beginTransaction().replace(R.id.container, sensorFragment).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPerm();
    }

    //ask for permissions when we start.
    public void CheckPerm() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                MainActivity.REQUEST_ACCESS_Activity_Updates);

        }
    }
    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.v(TAG, "onRequest result called.");

        if (requestCode == REQUEST_ACCESS_Activity_Updates) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //We have permissions, so ...
                Log.v(TAG, "Activity permission was  granted.");
                Toast.makeText(this, "Activity access granted", Toast.LENGTH_SHORT).show();
            } else {
                // permission denied,    Disable this feature or close the app.
                Log.v(TAG, "Activity permission was NOT granted.");
                Toast.makeText(this, "Activity access NOT granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_sensor) {
            fragmentManager.beginTransaction().replace(R.id.container, sensorFragment).commit();
        } else if (id == R.id.nav_record) {
            if (recordFragment == null) recordFragment = new RecordFragment();
            fragmentManager.beginTransaction().replace(R.id.container, recordFragment).commit();
        } else if (id == R.id.nav_session) {
            fragmentManager.beginTransaction().replace(R.id.container, new SessionFragment()).commit();
        } else if (id == R.id.nav_history) {
            fragmentManager.beginTransaction().replace(R.id.container, new HistoryFragment()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SensorFragment.REQUEST_OAUTH) {
            sensorFragment.onActivityResult(requestCode, resultCode, data);
            return;
        } else if (requestCode == RecordFragment.REQUEST_OAUTH) {
            sensorFragment.onActivityResult(requestCode, resultCode, data);
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
