package edu.cs4730.fitdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import java.util.Map;

/**
 * Most of the code is in the fragments.
 * <p>
 * But this activity is asking for the new ACTIVITY permission that started in API 29.  Not sure
 * what happens if this is run on a 28 device.
 *
 * ACTIVITY_RECOGNTION and ACCESS_FILE_LOCATION are requrested, since you need them for step count
 *   also could get calories.expended, activity.exercise information.
 *  see https://developers.google.com/fit/android/authorization 
 * Note you need the BODY_SENSOR permission in order to get heart_rate information.
 * this app doesn't not access it, so it is not requested or listed in the mananifest
 * also no attempt for the new API 33 BODY_SENSORS_BACKGROUND is made either.
 */

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    FragmentManager fragmentManager;
    SensorFragment sensorFragment;
    RecordFragment recordFragment = null;
    public static final String TAG = "MainActivity";
    private String[] REQUIRED_PERMISSIONS;
    ActivityResultLauncher<String[]> rpl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            //REQUIRED_PERMISSIONS = new String[]{"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACTIVITY_RECOGNITION"};
            REQUIRED_PERMISSIONS = new String[]{Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.ACCESS_FINE_LOCATION};
            Log.v(TAG, "android 12 activity and fine location");
        } else {
            REQUIRED_PERMISSIONS = new String[]{ Manifest.permission.ACTIVITY_RECOGNITION};
            Log.v(TAG, "below android 12 activity recognition needed.");
        }
        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    boolean granted = true;
                    for (Map.Entry<String, Boolean> x : isGranted.entrySet()) {
                        Log.v(TAG, x.getKey() + " is " + x.getValue());
                        if (!x.getValue()) granted = false;
                    }
                    if (granted) {
                        Log.v(TAG, "All permissions are granted.");
                        //they likely died first without permissions, so just do it again.
                        sensorFragment = new SensorFragment();
                        recordFragment = new RecordFragment();
                        fragmentManager.beginTransaction().replace(R.id.container, sensorFragment).commit();
                        Toast.makeText(getApplicationContext(), "Activity access granted", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.v(TAG, "One of more permissions were NOT granted.");
                        Toast.makeText(getApplicationContext(), "Activity access NOT granted", Toast.LENGTH_SHORT).show();
                        finish();

                    }
                }
            }
        );
        fragmentManager = getSupportFragmentManager();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //first instance, so the default is zero.
        if (allPermissionsGranted()) {
            sensorFragment = new SensorFragment();
            recordFragment = new RecordFragment();
            fragmentManager.beginTransaction().replace(R.id.container, sensorFragment).commit();
        }  //else wait until it comes back.
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!allPermissionsGranted())
            rpl.launch(REQUIRED_PERMISSIONS);
        else
            Log.v(TAG, "All permissions have been granted already.");
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
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
        } else if (id == R.id.nav_signout) {
            sensorFragment.disconnect();
            //reset, so it will login again.
            sensorFragment = new SensorFragment();
            fragmentManager.beginTransaction().replace(R.id.container, sensorFragment).commit();

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
