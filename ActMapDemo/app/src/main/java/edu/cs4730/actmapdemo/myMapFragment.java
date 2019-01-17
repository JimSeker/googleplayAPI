package edu.cs4730.actmapdemo;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
//import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
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
     LatLng INITLOC = null;
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
        //in a fragment  This method is deprecated and can return a null map.
        //map = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMap();
        //The new method here, will provide a non-null map.

        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;

                if (INITLOC != null)
                  map.moveCamera(CameraUpdateFactory.newLatLngZoom(INITLOC, 17));

                // Zoom in, animating the camera.
               // map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

                // Sets the map type to be "hybrid"
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL); //normal map
                //map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

                //allow user to use zoom controls (ie the + - buttons on the map.
                map.getUiSettings().setZoomControlsEnabled(true);

                map.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
                    @Override
                    public void onPolylineClick(Polyline polyline) {
                        Toast.makeText(getActivity(), getActivityString(polyline.getColor()), Toast.LENGTH_LONG).show();
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
            INITLOC = new LatLng(41.312928, -105.587253);

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
                    .color(getActivityColor(objDataList.act))  //make it red.
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

    /**
     * Returns color  corresponding to a detected activity type to draw on the map.
     */

    public static int getActivityColor(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                //return "In a Vehicle";
                return Color.BLUE;
            case DetectedActivity.ON_BICYCLE:
                //return "On a bicycle";
                return Color.BLACK;
            case DetectedActivity.ON_FOOT:
                //return "On Foot";
                return Color.CYAN;
            case DetectedActivity.RUNNING:
                //return "Running";
                return Color.GRAY;
            case DetectedActivity.STILL:
                //return "Still (not moving)";
                return Color.GREEN;
            case DetectedActivity.TILTING:
                //return "Tilting";
                return Color.MAGENTA;
            case DetectedActivity.UNKNOWN:
                //return "Unknown Activity";
                return Color.RED;
            case DetectedActivity.WALKING:
                //return "Walking";
                return Color.YELLOW;
            default:
                //return "Unknown Type";
                return Color.WHITE;
        }
    }

    /**
     * Returns a human readable String corresponding to a map color
     */

    public static String getActivityString(int color) {
        switch (color) {
            case Color.BLUE:
                return "In a Vehicle";
            case Color.BLACK:
                return "On a bicycle";
            case Color.CYAN:
                return "On Foot";
            case Color.GRAY:
                return "Running";
            case Color.GREEN:
                return "Still (not moving)";
            case Color.MAGENTA:
                return "Tilting";
            case Color.RED:
                return "Unknown Activity";
            case Color.YELLOW:
                return "Walking";
            default:
                return "Unknown Type";
        }
    }
}
