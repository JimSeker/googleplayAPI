package edu.cs4730.sleepapidemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.SleepClassifyEvent;
import com.google.android.gms.location.SleepSegmentEvent;

import java.util.List;

public class SleepReceiver extends BroadcastReceiver {

    public static final String TAG = "SleepReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (SleepSegmentEvent.hasEvents(intent)) {
            List<SleepSegmentEvent> sleepSegmentEvents = SleepSegmentEvent.extractEvents(intent);
            for (SleepSegmentEvent event : sleepSegmentEvents) {
                Log.e(TAG, "SleepSeg " + event.toString());
            }
        } else if (SleepClassifyEvent.hasEvents(intent)) {
            List<SleepClassifyEvent> sleepClassifyEvents = SleepClassifyEvent.extractEvents(intent);
            for (SleepClassifyEvent event : sleepClassifyEvents) {
              Log.e(TAG, "SleepClass " + event.toString());
            }
        } else {
            Log.wtf(TAG, "sleep receiver called, but intent is not a sleep.");
        }

    }
}