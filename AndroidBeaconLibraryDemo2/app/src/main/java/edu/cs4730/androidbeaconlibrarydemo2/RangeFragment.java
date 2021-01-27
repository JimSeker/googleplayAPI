package edu.cs4730.androidbeaconlibrarydemo2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * This needs to have a recycler view associated with it and display the data that way, otherwise,
 * it's impossible to follow.
 */

public class RangeFragment extends Fragment {
    TextView logger;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_range, container, false);
        logger = root.findViewById(R.id.text_notifications);

        return root;
    }
    public void logthis(String item) {
        if (logger != null)
        logger.append(item + "\n");
    }

}