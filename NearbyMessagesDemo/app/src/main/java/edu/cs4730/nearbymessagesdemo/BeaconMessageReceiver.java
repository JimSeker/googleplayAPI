package edu.cs4730.nearbymessagesdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;

public class BeaconMessageReceiver extends BroadcastReceiver {
    String TAG = "BeaconMessageReceiver";
    @Override
    public void onReceive(final Context context, Intent intent) {
        Nearby.getMessagesClient(context).handleIntent(intent, new MessageListener() {
            @Override
            public void onFound(Message message) {
                String content = new String(message.getContent());
                Log.i(TAG, "Found message via PendingIntent: " + content);
                Toast.makeText(context, content,Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLost(Message message) {
                Log.i(TAG, "Lost message via PendingIntent: " + message);
            }
        });

    }
}
