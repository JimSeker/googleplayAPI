package edu.cs4730.mapdemov2;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
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

import edu.cs4730.mapdemov2.databinding.BasicmapFragmentBinding;


/**
 * Shows a simple map.
 * <p>
 * This one, with the same code as compassFragment won't draw in bottomnavview the second time it
 * is clicked.  but the others work just fine?!  I'm seeing this in other apps too, so something
 * about how the map and bottomnavview interact with each other.  this was org using a viewpager,
 * which still works just fine.
 */
public class BasicMapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    BasicmapFragmentBinding binding;
    String TAG = "BasicMapFragment";

    public BasicMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = BasicmapFragmentBinding.inflate(inflater, container, false);
        //in a fragment
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        //in an activity
        //((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        //now that we have the map, add some things.
        Marker kiel = map.addMarker(new MarkerOptions().position(MainActivity.KIEL).title("Kiel")
                //change and use a blue "default" marker, instead of read.
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        Marker laramie = map.addMarker(new MarkerOptions().position(MainActivity.LARAMIE).title("Laramie")
                .snippet("I'm in Laramie!").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
        Marker cheyenne = map.addMarker(new MarkerOptions().position(MainActivity.CHEYENNE).title("Cheyenne"));
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
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

            @Override
            public boolean onMarkerClick(@NonNull Marker myMarker) {
                Toast.makeText(requireContext(), "Clicked the " + myMarker.getTitle() + " Marker", Toast.LENGTH_SHORT).show();
                //return true;  //yes we consumed the event.
                return false; //so the default action is shown as well, when you click icons and stuff.
            }
        });

        //add map click listener.
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng point) {
                Toast.makeText(requireContext(), "Lat: " + point.latitude + " Long:" + point.longitude, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
