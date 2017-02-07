package edu.cs4730.actmapdemo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.

 */
public class myListFragment extends Fragment {
    String TAG = "ListFragment";
    List<objData> values;
    RecyclerView mRecyclerView;
    myAdapter mAdapter;

    public myListFragment() {
        // Required empty public constructor
       // values = new String[]{"Menu -> Start Nav"};  //just case.
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_my_list, container, false);
               //setup the RecyclerView
        mRecyclerView = (RecyclerView)myView.findViewById(R.id.list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //and default animator
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.
        mAdapter = new myAdapter(values, R.layout.rowlayout, getContext());
        //add the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        return myView;
    }

    public void updateAdatper(List<objData> newValues) {
        //Log.v(TAG, "update");
        values = newValues;
        mAdapter.updateData(values);

    }


}
