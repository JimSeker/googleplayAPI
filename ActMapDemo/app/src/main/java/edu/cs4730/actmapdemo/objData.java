package edu.cs4730.actmapdemo;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Seker on 12/1/2015.
 */
public class objData {
    double lat;
    double lng;
    double myTime;
    LatLng myLatlng;
    int act;
    float distance =0.0f;

    objData(double lt, double lg, double t, int ac) {
        lat = lt;
        lng = lg;
        myTime = t;
        myLatlng = new LatLng(lat,lng);
        act = ac;

    }
}
