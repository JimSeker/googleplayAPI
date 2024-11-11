package edu.cs4730.fbdatabaseauthdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import edu.cs4730.fbdatabaseauthdemo.databinding.ActivityMainBinding;

/**
 * The friendlychat code was use an an example in many places, but it so complex.
 * https://github.com/firebase/friendlychat-android
 * <p>
 * NOTE to test the console notifications (cloud messaging now), close the app.  It has service to
 * caught it when the app is open as well.  But then you won't see the notification.
 */

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //local variables.
    private static final String TAG = "MainActivity";

    AuthFragment authFragment;
    AuthGoogleApiFragment authGoogleApiFragment;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
        setSupportActionBar(binding.toolbar);

        authFragment = new AuthFragment();
        authGoogleApiFragment = new AuthGoogleApiFragment();

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        binding.navView.setNavigationItemSelectedListener(this);

        //put the default first fragment into place.
        getSupportFragmentManager().beginTransaction()
                // .add(binding.container.getId(), authFragment).commit();
                .add(binding.container.getId(), authGoogleApiFragment).commit();

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        // Handle navigation view item clicks here.
        int id = menuItem.getItemId();

        if (id == R.id.nav_auth) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), authFragment).commit();
        } else if (id == R.id.nav_dbsimple) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), new DBSimpleFragment()).commit();
        } else if (id == R.id.nav_dblist) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), new DBListFragment()).commit();
        } else if (id == R.id.nav_authg) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), authGoogleApiFragment).commit();
        } else if (id == R.id.nav_storage) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), new StorageFragment()).commit();
        } else if (id == R.id.nav_rc) {
            getSupportFragmentManager().beginTransaction().replace(binding.container.getId(), new RCFragment()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
