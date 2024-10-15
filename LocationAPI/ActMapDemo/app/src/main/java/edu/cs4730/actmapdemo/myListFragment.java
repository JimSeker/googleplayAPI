package edu.cs4730.actmapdemo;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;


/**
 *  this setups the  recyclerview, the data is handled via the viewmodel.
 */
public class myListFragment extends Fragment {
    String TAG = "ListFragment";
    RecyclerView mRecyclerView;
    myAdapter mAdapter;
    DataViewModel mViewModel;

    public myListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_my_list, container, false);
        //setup the RecyclerView
        mRecyclerView = myView.findViewById(R.id.list);
        mViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        //and default animator
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        //setup the adapter, which is myAdapter, see the code.
        mAdapter = new myAdapter(null, R.layout.rowlayout);
        //add the adapter to the recyclerview
        mRecyclerView.setAdapter(mAdapter);

        mViewModel.getData().observe(getViewLifecycleOwner(), new Observer<ArrayList<objData>>() {
            @Override
            public void onChanged(ArrayList<objData> objData) {
                if (mAdapter != null)
                    mAdapter.updateData(objData);
            }
        });

        return myView;
    }
}
