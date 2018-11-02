package edu.cs4730.fbdatabaseauthdemo;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SimpleActivity extends AppCompatActivity {

    FirebaseDatabase database;
    final static String TAG = "SimpleActivity";
    TextView logger;
    DatabaseReference myRef;
    DatabaseReference myChildRef;
    ChildEventListener myChildeventlistener;
    ValueEventListener myValueEventlistener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);

        logger = findViewById(R.id.logger);

        database = FirebaseDatabase.getInstance();
        myChildRef = database.getReference().child("messages");

        //use a childevent listener, since with are getting children of messages.
        myChildeventlistener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Note myNote = dataSnapshot.getValue(Note.class);
                logthis("title: " + myNote.getTitle());
                logthis("note: " + myNote.getNote());
                logthis("id? " + s + "\n");
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Note myNote = dataSnapshot.getValue(Note.class);
                logthis("change title: " + myNote.getTitle());
                logthis("change note: " + myNote.getNote());
                logthis("changed id " + s + "\n");
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Note myNote = dataSnapshot.getValue(Note.class);
                logthis("removed title: " + myNote.getTitle());
                logthis("removed note: " + myNote.getNote());

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Note myNote = dataSnapshot.getValue(Note.class);
                logthis("moved title: " + myNote.getTitle());
                logthis("moved note: " + myNote.getNote());
                logthis("moved id: " + s + "\n");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read message values.", error.toException());
            }
        };

        //simple reference data.
        myRef = database.getReference("simple");
        // Read from the database, uses a valueEventlistener, not child.
        myValueEventlistener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                logthis("Simple is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read simple value.", error.toException());
            }
        };


        findViewById(R.id.add_object).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myChildRef.push().setValue(new Note("Test", "message test"));

            }
        });


        findViewById(R.id.add_simple).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                myRef.setValue("Hello, World!");   //don't use push, app crashes and corrupts the data.
            }
        });
    }


    void logthis(String item) {
        Log.d(TAG, "Value is: " + item);
        logger.append(item + "\n");
    }

    @Override
    public void onPause() {
        myRef.removeEventListener(myValueEventlistener);
        myChildRef.removeEventListener(myChildeventlistener);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        myRef.addValueEventListener(myValueEventlistener);
        myChildRef.addChildEventListener(myChildeventlistener);
    }
}
