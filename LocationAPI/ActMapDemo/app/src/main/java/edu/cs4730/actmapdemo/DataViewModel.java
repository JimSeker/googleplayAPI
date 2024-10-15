package edu.cs4730.actmapdemo;

import android.app.Application;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;

import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.function.ObjDoubleConsumer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class DataViewModel extends AndroidViewModel {

   private MutableLiveData<ArrayList<objData>> objDataList;
   public int currentActivity;

   public DataViewModel(@NonNull Application application) {
      super(application);
      objDataList = new MutableLiveData<ArrayList<objData>>(new ArrayList<objData>());
      currentActivity = DetectedActivity.UNKNOWN;
   }

   public LiveData<ArrayList<objData>> getData() {
      return objDataList;
   }

   public void add (objData data) {
      ArrayList<objData> l = objDataList.getValue();
      if (l == null)
         l = new ArrayList<objData>();
      l.add(data);
      objDataList.postValue(l);
   }

   public void clear() {
      ArrayList<objData> l = objDataList.getValue();
      if (l == null)
         l = new ArrayList<objData>();
      l.clear();
      objDataList.postValue(l);
   }

   public float getDistance(LatLng newPoint) {
      //figure distance info.
      ArrayList<objData> l = objDataList.getValue();
      if (l.isEmpty()) {
          return 0.0f;  //first point, so no distance yet.
      } else {
         objData last = getLast();
         float tmp = distanceBetween(last.myLatlng, newPoint) * 3.28f; //converted to feet
         tmp += last.distance;  //previous distance, to ge the total.
         return tmp;
      }
   }

   public objData getLast() {
      ArrayList<objData> l = objDataList.getValue();
      if (l.isEmpty()) {
         //return null;  //is this the right return value?  or just an empty one?
         return new objData(0,0,0,0);  //better to return an empty one?
      } else {
         return l.get(l.size() - 1);
      }

   }

   public static float distanceBetween(LatLng latLng1, LatLng latLng2) {

      Location loc1 = new Location(LocationManager.GPS_PROVIDER);
      Location loc2 = new Location(LocationManager.GPS_PROVIDER);

      loc1.setLatitude(latLng1.latitude);
      loc1.setLongitude(latLng1.longitude);

      loc2.setLatitude(latLng2.latitude);
      loc2.setLongitude(latLng2.longitude);

      return loc1.distanceTo(loc2);
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

   static final LatLng LARAMIE = new LatLng(41.312928, -105.587253);
}
