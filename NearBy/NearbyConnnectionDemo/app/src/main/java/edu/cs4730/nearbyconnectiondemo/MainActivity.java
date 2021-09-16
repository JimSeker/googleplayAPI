package edu.cs4730.nearbyconnectiondemo;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.nearby.connection.Strategy;

/**
 * Nearby connection demo
 *
 * https://developers.google.com/nearby/connections/overview
 *
 */

public class MainActivity extends AppCompatActivity implements HelpFragment.OnFragmentInteractionListener {
    String TAG = "MainActivity";
    public static final String ServiceId = "edu.cs4730.nearbyconnectiondemo";  //need a unique value to identify app.
    public static final int REQUEST_ACCESS_COURSE_LOCATION= 1;
    FragmentManager fragmentManager;

    /**
     * The connection strategy we'll use for Nearby Connections. In this case, we've decided on
     * P2P_STAR, which is a combination of Bluetooth Classic and WiFi Hotspots.  this is 1 to many, so 1 advertise and many discovery.
     * NOTE: in tests, the discovery changed the wifi to a hotspot on most occasions.  on disconnect, it changed back.
     */
    public static final Strategy STRATEGY = Strategy.P2P_STAR;
    //public static final Strategy STRATEGY = Strategy.P2P_CLUSTER;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.frag_container, new HelpFragment()).commit();

    }

    @Override
    public void onFragmentInteraction(int id) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // Replace whatever is in the fragment_container view with this fragment,
        if (id == 2) { //client
            transaction.replace(R.id.frag_container, new DiscoveryFragment());
        } else { //server
            transaction.replace(R.id.frag_container, new AdvertiseFragment());
        }
        // and add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}
