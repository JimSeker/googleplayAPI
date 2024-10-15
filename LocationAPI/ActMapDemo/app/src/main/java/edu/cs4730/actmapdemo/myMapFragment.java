package edu.cs4730.actmapdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

/**
 * This draws the points on a google map, to see where
 */
public class myMapFragment extends Fragment {
    private GoogleMap map;
    View myView;
    String TAG = "mapfrag";
    objData current = null;
    LatLng INITLOC = null;
    DataViewModel mViewModel;

    public myMapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
        mViewModel.getData().observe(getViewLifecycleOwner(), new Observer<ArrayList<objData>>() {
            @Override
            public void onChanged(ArrayList<objData> objData) {
                if (!objData.isEmpty())
                    updateMapDraw(objData.get(objData.size() - 1));  //update with newest data point, (last one)
            }
        });
        //Because of the maps, we need to have the view inflated only once (viewpager, may call this multiple times
        // so if this is the first time, ie myView is null, then do the setup, otherwise, "reset" the view, by removing it
        // and return the already setup view.
        if (myView == null) {
            myView = inflater.inflate(R.layout.fragment_map, container, false);
        } else {
            ((ViewGroup) container.getParent()).removeView(myView);
            return myView;
        }

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {
                map = googleMap;

                INITLOC = DataViewModel.LARAMIE;
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(INITLOC, 17));

                map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //normal map
                //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                //allow user to use zoom controls (ie the + - buttons on the map.
                map.getUiSettings().setZoomControlsEnabled(true);
                map.getUiSettings().setTiltGesturesEnabled(true);
                map.setBuildingsEnabled(true);

                map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(@NonNull Polyline polyline) {
                        Toast.makeText(getActivity(), DataViewModel.getActivityString(polyline.getColor()), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });


        return myView;
    }

    public void setupInitialloc(double lat, double lng) {
        // Move the camera instantly to current location.
        if (map != null)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17));
        else
            INITLOC = DataViewModel.LARAMIE;
    }

    public void updateMapDraw(objData objDataList) {
        Log.v(TAG, "map Update!");
        if (current == null) {
            map.addMarker(new MarkerOptions()
                .position(objDataList.myLatlng)
                .title("Start")
            );
            Log.v(TAG, "Added init marker");
            current = objDataList;
        } else {
            //add a new line to map
            map.addPolyline(new PolylineOptions()
                    .add(current.myLatlng, objDataList.myLatlng)   //line segment.
                    .color(DataViewModel.getActivityColor(objDataList.act))  //make it red.
                    .clickable(true)  //for the listener.
                //.width(10)   //width of 10
            );
            //move the camera to center it to it.
            map.moveCamera(CameraUpdateFactory.newLatLng(objDataList.myLatlng));
            //and finally make it the current position
            current = objDataList;
        }
    }

    public void mileMarker(objData objDataList, String Title) {
        map.addMarker(new MarkerOptions()
            .position(new LatLng(
                objDataList.lat,
                objDataList.lng))
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .title(Title)
        );
    }

    public void finishMap(objData objDataList) {
        //add a marker for the stop position.
        map.addMarker(new MarkerOptions()
            .position(new LatLng(
                objDataList.lat,
                objDataList.lng))
            .title("End " + objDataList.distance + " Miles")
        );
        //move the camera to center it to it.
        map.moveCamera(CameraUpdateFactory.newLatLng(objDataList.myLatlng));
    }

    public void clearmap() {
        map.clear();
        current = null;  //so the start marker will show up.
    }


}
