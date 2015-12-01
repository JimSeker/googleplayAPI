package edu.cs4730.actmapdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class myMapFragment extends Fragment {
    private GoogleMap map;
    View myView;
    String TAG = "mapfrag";
    List<objData> mylist = new ArrayList<objData>();  //just in case.
    objData current = null;

    public myMapFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        //Because of the maps, we need to have the view inflated only once (viewpager, may call this multiple times
        // so if this is the first time, ie myView is null, then do the setup, otherwise, "reset" the view, by removing it
        // and return the already setup view.
        if (myView == null) {
            myView = inflater.inflate(R.layout.fragment_map, container, false);
        } else {
            ((ViewGroup) container.getParent()).removeView(myView);
            return myView;
        }
        //in a fragment
        map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();


        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        // Sets the map type to be "hybrid"
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //normal map
        //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //allow user to use zoom controls (ie the + - buttons on the map.
        map.getUiSettings().setZoomControlsEnabled(true);


        return myView;
    }

    public void setupInitialloc(double lat, double lng) {
        // Move the camera instantly to current location.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 17));
    }

    public void updateMapDraw(objData objDataList) {
        Log.v(TAG, "map Update!");
        if (current == null) {
            map.addMarker(new MarkerOptions()
                            .position(new LatLng(
                                    objDataList.lat,
                                    objDataList.lng))
                            .title("Start")

            );
            Log.v(TAG, "Added init marker");
            current = objDataList;
        } else {
            map.addPolyline(new PolylineOptions()
            .add(new LatLng(current.lat, current.lng), new LatLng(objDataList.lat, objDataList.lng))   //line segment.
            .color(Color.RED)  //make it red.
            //.width(10)   //width of 10
            );
            current = objDataList;
        }
    }

}
