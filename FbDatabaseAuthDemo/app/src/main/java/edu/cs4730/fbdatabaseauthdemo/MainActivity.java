package edu.cs4730.fbdatabaseauthdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

/**
 * The friendlychat code was use an an example in many places, but it so complex.
 * https://github.com/firebase/friendlychat-android
 * <p>
 * NOTE to test the console notifications (cloud messaging now), close the app.  It has service to
 * caught it when the app is open as well.  But then you won't see the notification.
 */

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    //public variables to use my fragments
    static final int RC_PHOTO_PICKER = 9003;
    static final int RC_INVITE = 9004;

    //local variables.
    private static String TAG = "MainActivity";
    AuthFragment authFragment;
    AuthGoogleApiFragment authGoogleApiFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        authFragment = new AuthFragment();
        authGoogleApiFragment = new AuthGoogleApiFragment();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //put the default first fragment into place.
        getSupportFragmentManager().beginTransaction()
            // .add(R.id.container, authFragment).commit();
            .add(R.id.container, authGoogleApiFragment).commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_auth) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, authFragment).commit();
        } else if (id == R.id.nav_dbsimple) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new DBSimpleFragment()).commit();
        } else if (id == R.id.nav_dblist) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new DBListFragment()).commit();
        } else if (id == R.id.nav_authg) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, authGoogleApiFragment).commit();
        } else if (id == R.id.nav_storage) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new StorageFragment()).commit();
        } else if (id == R.id.nav_rc) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new RCFragment()).commit();
        } else if (id == R.id.nav_invite) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new InviteAntFragment()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
