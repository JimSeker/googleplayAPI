package edu.cs4730.nearbyconnectiondemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

/**
 * this is a simple helper screen.  it will check on the course location permission and bluetooth as well.
 * and has two buttons to launch the advertise or discover fragment.
 */
public class HelpFragment extends Fragment {
    String TAG = "HelpFragment";
    private OnFragmentInteractionListener mListener;
    TextView logger;
    //bluetooth device and code to turn the device on if needed.
    BluetoothAdapter mBluetoothAdapter = null;
    private static final int REQUEST_ENABLE_BT = 2;

    public HelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_help, container, false);
        logger = myView.findViewById(R.id.logger1);


        myView.findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) //don't call if null, duh...
                    mListener.onFragmentInteraction(2);
            }
        });
        myView.findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) //don't call if null, duh...
                    mListener.onFragmentInteraction(1);
            }
        });

       // startbt();  //we don't need to turn on bluetooth, nearby will do it for us.
        return myView;
    }

    //This code will check to see if there is a bluetooth device and
    //turn it on if is it turned off.
    public void startbt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            logthis("This device does not support bluetooth");
            return;
        }
        //make sure bluetooth is enabled.
        if (!mBluetoothAdapter.isEnabled()) {
            logthis("There is bluetooth, but turned off");
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            logthis("The bluetooth is ready to use.");
            //bluetooth is on, so list paired devices from here.
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            //bluetooth result code.
            if (resultCode == Activity.RESULT_OK) {
                logthis("Bluetooth is on.");

            } else {
                logthis("Please turn the bluetooth on.");
            }
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        checkpermissions();
    }

    void checkpermissions() {
        //first check to see if I have permissions (marshmallow) if I don't then ask, otherwise start up the demo.
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //I'm on not explaining why, just asking for permission.
            Log.v(TAG, "asking for permissions");
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                MainActivity.REQUEST_ACCESS_COURSE_LOCATION);
            logthis("We don't have permission to course location");
        }  else {
            logthis("We have permission to course location");
        }
    }

    public void logthis(String msg) {
        logger.append(msg + "\n");
        Log.d(TAG, msg);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int id);
    }
}
