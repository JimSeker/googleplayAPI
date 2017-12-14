package edu.cs4730.nearbymessagesdemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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


/**
 * this is a simple helper screen.  it will check on the course location permission and bluetooth as well.
 * and has two buttons to launch the publish or subscribe fragment.
 */
public class HelpFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    String TAG = "HelpFragment";
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
        View myView = inflater.inflate(R.layout.fragment_help, container, false);

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

    @Override
    public void onResume() {
        super.onResume();
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
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(int id);
    }
}
