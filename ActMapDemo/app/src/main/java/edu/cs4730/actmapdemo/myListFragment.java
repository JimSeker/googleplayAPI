package edu.cs4730.actmapdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;


/**
 * A simple {@link Fragment} subclass.

 */
public class myListFragment extends Fragment {
    String TAG = "ListFragment";
    String[] values;
    ListView myList;
    ArrayAdapter<String> adapter;

    public myListFragment() {
        // Required empty public constructor
        values = new String[]{"Menu -> Start Nav"};  //just case.
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_my_list, container, false);
        myList = (ListView) myView.findViewById(R.id.mylist);
        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, values);

        myList.setAdapter(adapter);
        return myView;
    }

    public void updateAdatper(String[] newValues) {
        //Log.v(TAG, "update");
        values = newValues;
        adapter = new ArrayAdapter<String>(getActivity(),
              //  R.layout.rowlayout,R.id.label, values);
                android.R.layout.simple_list_item_1, values);

        myList.setAdapter(adapter);
       // Log.v(TAG, "data lenght is " + values.length);

        //adapter.notifyDataSetChanged();
    }

}
