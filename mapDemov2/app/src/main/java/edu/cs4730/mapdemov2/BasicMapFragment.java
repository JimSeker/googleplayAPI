package edu.cs4730.mapdemov2;


import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 */
public class BasicMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    View myView;
    public BasicMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Because of the maps, we need to have the view inflated only once (viewpager, may call this multiple times
        // so if this is the first time, ie myView is null, then do the setup, otherwise, "reset" the view, by removing it
        // and return the already setup view.
        if (myView == null) {
            myView = inflater.inflate(R.layout.basicmap_fragment, container, false);
        } else  {
            ((ViewGroup) container.getParent()).removeView(myView);
            return myView;
        }
        //in a fragment
         ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        //in an activity
        //((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);


        return myView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        //now that we have the map, add some things.
        Marker kiel = map.addMarker(new MarkerOptions()
                .position(MainActivity.KIEL)
                .title("Kiel")
                //change and use a blue "default" marker, instead of read.
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
        );

        Marker laramie = map.addMarker(new MarkerOptions()
                .position(MainActivity.LARAMIE)
                .title("Laramie")
                .snippet("I'm in Laramie!")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher))
        );

        Marker cheyenne = map.addMarker(new MarkerOptions().position(MainActivity.CHEYENNE)
                .title("Cheyenne"));
        // Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.LARAMIE, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Sets the map type to be "hybrid"
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //normal map
        //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //allow user to use zoom controls (ie the + - buttons on the map.
        map.getUiSettings().setZoomControlsEnabled(true);

        //add a marker click event.
        map.setOnMarkerClickListener( new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(Marker myMarker) {
                Toast.makeText(getActivity().getApplicationContext(), "Clicked the " + myMarker.getTitle() + " Marker", Toast.LENGTH_SHORT).show();

                //return true;  //yes we consumed the event.
                return false; //so the default action is shown as well.
            }

        });


        //add map click listener.
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                Toast.makeText(getActivity().getApplicationContext(), "Lat: " + point.latitude+ " Long:" +point.longitude,  Toast.LENGTH_SHORT).show();
            }

        });

    }



}
