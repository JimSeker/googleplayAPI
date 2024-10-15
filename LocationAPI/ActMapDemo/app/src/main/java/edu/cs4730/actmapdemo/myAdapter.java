package edu.cs4730.actmapdemo;

/**
 * Created by Seker on 1/22/2015.
 */

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

import java.util.List;

/**
 * simple adapter to do the list of activities, picture, name, and distance.
 */

public class myAdapter extends RecyclerView.Adapter<myAdapter.ViewHolder> {

    private List<objData> myList;
    private int rowLayout;
   // private Context mContext;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView pos;
        public TextView miles;
        public ImageView Pic;

        public ViewHolder(View itemView) {
            super(itemView);
            pos = itemView.findViewById(R.id.location);
            miles = itemView.findViewById(R.id.distance);
            Pic = itemView.findViewById(R.id.pic);
        }
    }

    //constructor
    myAdapter(List<objData> myList, int rowLayout) {
        this.myList = myList;
        this.rowLayout = rowLayout;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(rowLayout, viewGroup, false);
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int i) {
        objData entry = myList.get(i);
        viewHolder.pos.setText(entry.lat + " " + entry.lng);
        if (entry.distance < 5280.0f)
            viewHolder.miles.setText(String.format("%.2f", entry.distance) + " feet");
        else
            viewHolder.miles.setText(String.format("%.2f", entry.distance / 5280.0f) + " miles");

        viewHolder.Pic.setImageResource(getActivityPic(entry.act));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return myList == null ? 0 : myList.size();
    }

    public void updateData(List<objData> l) {
        myList = l;
        notifyDataSetChanged();
    }

    /**
     * Returns a human readable String corresponding to a detected activity type.
     */

    public static int getActivityPic(int detectedActivityType) {
        switch (detectedActivityType) {
            case DetectedActivity.IN_VEHICLE:
                return R.drawable.car;
            case DetectedActivity.ON_BICYCLE:
                return R.drawable.bike;
            case DetectedActivity.ON_FOOT:
                return R.drawable.walk;
            case DetectedActivity.RUNNING:
                return R.drawable.run;
            case DetectedActivity.STILL:
                return R.drawable.still;
            case DetectedActivity.TILTING:
                return R.drawable.tilt;
            case DetectedActivity.UNKNOWN:
                return R.drawable.unknown;
            case DetectedActivity.WALKING:
                return R.drawable.walk;
            default:
                return R.drawable.unknown;
        }
    }
}
