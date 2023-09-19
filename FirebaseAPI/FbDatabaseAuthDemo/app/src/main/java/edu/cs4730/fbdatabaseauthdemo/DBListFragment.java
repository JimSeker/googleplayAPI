package edu.cs4730.fbdatabaseauthdemo;


import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.cs4730.fbdatabaseauthdemo.databinding.FragmentDblistBinding;
import edu.cs4730.fbdatabaseauthdemo.databinding.FragmentMyDialogBinding;
import edu.cs4730.fbdatabaseauthdemo.databinding.NoteRowBinding;


/**
 * A simple {@link Fragment} subclass.
 */
public class DBListFragment extends Fragment {

    final static String TAG = "DBListFragment";
    DatabaseReference mFirebaseDatabaseReference;
    DatabaseReference myChildRef;
    private FirebaseAuth mFirebaseAuth;
    private FragmentDblistBinding binding;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<Note, NoteViewHolder> mFirebaseAdapter;


    public static class NoteViewHolder extends RecyclerView.ViewHolder {
//        TextView tv_title;
//        TextView tv_note;
//        View myitemView;
        NoteRowBinding viewBinding;
        public NoteViewHolder(NoteRowBinding viewBinding) {
            super(viewBinding.getRoot());
            this.viewBinding = viewBinding;
//            tv_title = itemView.findViewById(R.id.title);
//            tv_note = itemView.findViewById(R.id.note);
//            myitemView = itemView;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentDblistBinding.inflate(inflater, container, false);
        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog("Add");
            }
        });

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        //Following android doc and example.  It was not commented and I'm not sure what is going on here.
        SnapshotParser<Note> parser = new SnapshotParser<Note>() {
            @NonNull
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
        FirebaseRecyclerOptions<Note> options = new FirebaseRecyclerOptions.Builder<Note>().setQuery(myChildRef, parser).build();

        /**
         *  This is the adapter for the recyclerview.  but it's the firebase recycler adapter.
         */
        //needed for the firebase recyclerview adapter.
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        // mLinearLayoutManager.setStackFromEnd(true);  //causes the list to align at the bottom, instead of the top.
        //we are extending a firebase recyclerview addapter here, using the viewholder above.
        mFirebaseAdapter = new FirebaseRecyclerAdapter<Note, NoteViewHolder>(options) {
            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                return new NoteViewHolder(NoteRowBinding.inflate(inflater, viewGroup, false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final NoteViewHolder viewHolder, int position, @NonNull Note note) {
                viewHolder.viewBinding.title.setText(note.getTitle());
                viewHolder.viewBinding.title.setTag(note);  //since it's small.  larger, just use the id, which is the key.
                viewHolder.viewBinding.note.setText(note.getNote());
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateDialog((Note) viewHolder.viewBinding.title.getTag());
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
                if (lastVisiblePosition == -1 || (positionStart >= (noteCount - 1) && lastVisiblePosition == (positionStart - 1))) {
                    binding.list.scrollToPosition(positionStart);
                }
            }
        });

        binding.list.setLayoutManager(mLinearLayoutManager);
        binding.list.setAdapter(mFirebaseAdapter);

        //setup left/right swipes on the cardviews so I can delete data.
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                //likely allows to for animations?  or moving items in the view I think.
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                //called when it has been animated off the screen.  So item is no longer showing.
                //use ItemtouchHelper.X to find the correct one.
                if (direction == ItemTouchHelper.RIGHT) {
                    Note mynote = (Note) ((NoteViewHolder) viewHolder).viewBinding.title.getTag();
                    //  this is the delete.
                    //mFirebaseDatabaseReference.child("messages").child(mynote.getId()).removeValue();
                    //or use this, since it already declared.
                    myChildRef.child(mynote.getId()).removeValue();
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(binding.list);
        return binding.getRoot();
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
        FragmentMyDialogBinding binding = FragmentMyDialogBinding.inflate(LayoutInflater.from(requireContext()));
        binding.etNote.setText(note.getNote());
        binding.etTitle.setText(note.getTitle());
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_AppCompat_Dialog));
        builder.setView(binding.getRoot()).setTitle("Update Note");
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                logthis("update title is " + binding.etTitle.getText().toString());
                logthis("update note is " + binding.etNote.getText().toString());
                Note mynote = new Note(binding.etTitle.getText().toString(), binding.etNote.getText().toString());

                // mFirebaseDatabaseReference.child("messages").child(note.getId()).setValue(mynote);
                //OR since it's partially there already, use myChildRef
                myChildRef.child(note.getId()).setValue(mynote);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        FragmentMyDialogBinding binding = FragmentMyDialogBinding.inflate(LayoutInflater.from(requireContext()));
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(requireContext(), R.style.ThemeOverlay_AppCompat_Dialog));
        builder.setView(binding.getRoot()).setTitle(title);
        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                logthis("title is " + binding.etTitle.getText().toString());
                logthis("note is " + binding.etNote.getText().toString());
                //Note push is used only when added new data for child "notes".  ie needs a unique id.
                myChildRef.push().setValue(new Note(binding.etTitle.getText().toString(), binding.etNote.getText().toString()));
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
