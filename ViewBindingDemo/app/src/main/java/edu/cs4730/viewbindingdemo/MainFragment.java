package edu.cs4730.viewbindingdemo;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import edu.cs4730.viewbindingdemo.databinding.FragmentMainBinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {
    private FragmentMainBinding binding;

    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false);
        View view = binding.getRoot();
        return view;

    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}
