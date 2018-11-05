package edu.cs4730.fbdatabaseauthdemo;

import android.content.DialogInterface;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class ListActivity extends AppCompatActivity {


    final static String TAG = "ListActivity";

    DatabaseReference mFirebaseDatabaseReference;
    DatabaseReference myChildRef;


    private FirebaseAuth mFirebaseAuth;


    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Note, MessageViewHolder> mFirebaseAdapter;


    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tv_title;
        TextView tv_note;
        View myitemView;

        public MessageViewHolder(View v) {
            super(v);
            tv_title = itemView.findViewById(R.id.title);
            tv_note = itemView.findViewById(R.id.note);
            myitemView = itemView;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("Add");
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();

        mRecyclerView = findViewById(R.id.list);
        mLinearLayoutManager = new LinearLayoutManager(this);
        // mLinearLayoutManager.setStackFromEnd(true);

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Following android doc and example.  It was not commented and I'm not sure what is going on here.
        SnapshotParser<Note> parser = new SnapshotParser<Note>() {
            @Override
            public Note parseSnapshot(DataSnapshot dataSnapshot) {
                Note note = dataSnapshot.getValue(Note.class);
                if (note != null) {
                    note.setId(dataSnapshot.getKey());
                }
                return note;
            }
        };

        myChildRef = mFirebaseDatabaseReference.child("messages");


        //Following android doc and example.  It was not commented and I'm not sure what is going on here.
        FirebaseRecyclerOptions<Note> options =
            new FirebaseRecyclerOptions.Builder<Note>()
                .setQuery(myChildRef, parser)
                .build();

        /**
         *  This is the adapter for the recyclerview.  but it's the firebase recycler adapter.
         */
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Note, MessageViewHolder>(options) {

            @Override
            public MessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new MessageViewHolder(inflater.inflate(R.layout.note_row, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(final MessageViewHolder viewHolder, int position, Note note) {
                viewHolder.tv_title.setText(note.getTitle());
                viewHolder.tv_title.setTag(note);  //since it's small.  larger, just use the id, which is the key.
                viewHolder.tv_note.setText(note.getNote());
                viewHolder.myitemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDialog((Note) viewHolder.tv_title.getTag());
                    }
                });
            }
        };
        //This must be the spot that gets the updates from the realtime database.
        //Again, I'm not really sure what is going on here.
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int noteCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the user is at the bottom of the list, scroll
                // to the bottom of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (noteCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

        //setup left/right swipes on the cardviews so I can delete data.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //likely allows to for animations?  or moving items in the view I think.
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                //called when it has been animated off the screen.  So item is no longer showing.
                //use ItemtouchHelper.X to find the correct one.
                if (direction == ItemTouchHelper.RIGHT) {
                    Note mynote = (Note) ((MessageViewHolder) viewHolder).tv_title.getTag();
                    //  this is the delete.
                    //mFirebaseDatabaseReference.child("messages").child(mynote.getId()).removeValue();
                    //or use this, since it already declared.
                    myChildRef.child(mynote.getId()).removeValue();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }


    @Override
    public void onPause() {

        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    /**
     * This creates a dialog to update the note.  It is passed to this method.
     */
    void updateDialog(final Note note) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textenter = inflater.inflate(R.layout.fragment_my_dialog, null);
        final EditText et_note = textenter.findViewById(R.id.et_note);
        et_note.setText(note.getNote());
        final EditText et_title = textenter.findViewById(R.id.et_title);
        et_title.setText(note.getTitle());
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Dialog));
        builder.setView(textenter).setTitle("Add");
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                logthis("update title is " + et_title.getText().toString());
                logthis("update note is " + et_note.getText().toString());
                //myChildRef.push().setValue(new Note(et_title.getText().toString(), et_note.getText().toString()));
                Note mynote = new Note(et_title.getText().toString(), et_note.getText().toString());

                // mFirebaseDatabaseReference.child("messages").child(note.getId()).setValue(mynote);
                //OR since it's partially there already, use myChildRef
                myChildRef.child(note.getId()).setValue(mynote);
                //Toast.makeText(getBaseContext(), userinput.getText().toString(), Toast.LENGTH_LONG).show();

            }
        })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    logthis("dialog canceled");
                    dialog.cancel();

                }
            });
        //you can create the dialog or just use the now method in the builder.
        //AlertDialog dialog = builder.create();
        //dialog.show();
        builder.show();
    }

    /**
     * Add dialog, with blanks.  then adds the data into the database.
     */
    void showDialog(String title) {
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textenter = inflater.inflate(R.layout.fragment_my_dialog, null);
        final EditText et_note = textenter.findViewById(R.id.et_note);
        final EditText et_title = textenter.findViewById(R.id.et_title);
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.ThemeOverlay_AppCompat_Dialog));
        builder.setView(textenter).setTitle(title);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int id) {

                logthis("title is " + et_title.getText().toString());
                logthis("note is " + et_note.getText().toString());
                //Note push is used only when added new data for child "notes".  ie needes a unique id.
                myChildRef.push().setValue(new Note(et_title.getText().toString(), et_note.getText().toString()));
                //Toast.makeText(getBaseContext(), userinput.getText().toString(), Toast.LENGTH_LONG).show();
            }
        })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    logthis("dialog canceled");
                    dialog.cancel();

                }
            });
        //you can create the dialog or just use the now method in the builder.
        //AlertDialog dialog = builder.create();
        //dialog.show();
        builder.show();
    }


    void logthis(String item) {
        Log.d(TAG, "Value is: " + item);
    }
}
