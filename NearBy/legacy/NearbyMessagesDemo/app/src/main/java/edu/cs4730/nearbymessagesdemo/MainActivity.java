package edu.cs4730.nearbymessagesdemo;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 *  A simple example of using nearby messages.
 * https://developers.google.com/nearby/messages/android/get-started
 * it is working as of api 33. 
 * 
 *
 */

public class MainActivity extends AppCompatActivity implements HelpFragment.OnFragmentInteractionListener{
    String TAG = "MainActivity";
    public static final int REQUEST_ACCESS_FINE_LOCATION= 1;
    FragmentManager fragmentManager;

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
            transaction.replace(R.id.frag_container, new SubscribeFragment());
        } else { //server
            transaction.replace(R.id.frag_container, new PublishFragment());
        }
        // and add the transaction to the back stack so the user can navigate back
        transaction.addToBackStack(null);
        // Commit the transaction
        transaction.commit();
    }
}
