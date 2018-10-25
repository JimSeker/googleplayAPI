package edu.cs4730.mapdemov2;


import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.model.LatLng;


//note, very little happens in here.  see the other activities.

//Second note, this example needs to be updated to use fragments (a view pager?) instead of 3 activities.

public class MainActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerlayout;
    private NavigationView mNavigationView;
    FragmentManager fragmentManager;

    static final LatLng CHEYENNE = new LatLng(41.1400, -104.8197);  //Note, West is a negative, East is positive
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    static final LatLng LARAMIE = new LatLng(41.312928, -105.587253);

//    BasicMapFragment BasicMapFrag;
//    CompassFragment CompassFrag;
//    DrawMapFragment DrawMapFrag;
//        BasicMapFrag = new BasicMapFragment();
//        CompassFrag = new CompassFragment();
//        DrawMapFrag = new DrawMapFragment();

    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //use the v7.toolbar instead of the default one.
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //standard navigation drawer setup.
        mDrawerlayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerToggle = new ActionBarDrawerToggle(this,  // host activity
                mDrawerlayout,  //drawerlayout object
                toolbar,  //toolbar
                R.string.drawer_open,  //open drawer description  required!
                R.string.drawer_close) {  //closed drawer description

            //called once the drawer has closed.
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Categories");
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            //called when the drawer is now open.
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(R.string.app_name);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        //To disable the icon for the drawer, change this to false
        //mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerlayout.addDrawerListener(mDrawerToggle);

        //this ia the support Navigation view.
        mNavigationView = (NavigationView) findViewById(R.id.navview);
        //setup a listener, which acts very similar to how menus are handled.
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //we could just as easily call onOptionsItemSelected(menuItem) and how it deal with it.
                //Log.v(TAG, "We got someting?");
                int id = menuItem.getItemId();
                if (id == R.id.navigation_item_1) {
                    //load fragment
                    if (!menuItem.isChecked()) {  //only need to do this if fragment is already loaded.
                        menuItem.setChecked(true);  //make sure to check/highlight the item.
                        fragmentManager.beginTransaction().replace(R.id.container, new BasicMapFragment()).commit();
                    }
                    mDrawerlayout.closeDrawers();  //close the drawer, since the user has selected it.
                    return true;
                } else if (id == R.id.navigation_item_2) {
                    //Log.v(TAG, "item 2?");
                    //load fragment
                    if (!menuItem.isChecked()) {  //only need to do this if fragment is already loaded.
                        menuItem.setChecked(true); //make sure the item is checked/highlighted
                        Log.v(TAG, "fab fragment?");
                        fragmentManager.beginTransaction().replace(R.id.container, new CompassFragment()).commit();
                    }
                    //now close the nav drawer.
                    mDrawerlayout.closeDrawers();
                    return true;
                } else if (id == R.id.navigation_item_3) {
                    //load fragment
                    //if (!menuItem.isChecked()) {  //only need to do this if fragment is already loaded.
                    menuItem.setChecked(true);
                    fragmentManager.beginTransaction().replace(R.id.container, new DrawMapFragment()).commit();
                    //}
                    mDrawerlayout.closeDrawers();
                    return true;
                }
                return false;
            }
        });

        //finally deal with the fragments.
        fragmentManager = getSupportFragmentManager();
        //first instance, so the default is zero.
        fragmentManager.beginTransaction().replace(R.id.container, new BasicMapFragment()).commit();
    }


    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}