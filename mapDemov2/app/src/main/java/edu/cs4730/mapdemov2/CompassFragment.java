package edu.cs4730.mapdemov2;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;

import edu.cs4730.mapdemov2.databinding.CompassFragmentBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class CompassFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    CompassFragmentBinding binding;

    public CompassFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = CompassFragmentBinding.inflate(inflater, container, false);
        //in a fragment
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map2)).getMapAsync(this);
        //in an activity
        //map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

        return binding.getRoot();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        // Move the camera instantly to laramie and zoom in .
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(MainActivity.LARAMIE, 15));

        // Zoom in, animating the camera.
        //map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Sets the map type to be "hybrid"
        //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //turn on buildings
        //map.setBuildingsEnabled(true);
    }
}
