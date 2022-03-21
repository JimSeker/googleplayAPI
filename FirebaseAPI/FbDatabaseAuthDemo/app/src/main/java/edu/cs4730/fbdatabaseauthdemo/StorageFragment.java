package edu.cs4730.fbdatabaseauthdemo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executor;

import static android.app.Activity.RESULT_OK;


/**
 * This fragment shows how use the storage parts.
 * It's simple and just uploads a picture and shows it in a list (all the pictures).
 * It uses the realtime database as well, under the picture heading.
 */
public class StorageFragment extends Fragment {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private String mUsername;

    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private Button mPhotoPickerButton;

    private final String TAG = "StorageFragment";
    DatabaseReference mFirebaseDatabaseReference;
    TextView logger;
    DatabaseReference myRef;
    ValueEventListener myValueEventlistener;

    ImageView myPic;

    boolean havepic = false;
    String imageurl = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView = inflater.inflate(R.layout.fragment_storage, container, false);

        myPic = myView.findViewById(R.id.imagePic);

        //auth, so I can get the username.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null)
            mUsername = mFirebaseUser.getDisplayName();
        else
            mUsername = "anonymous";
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();


        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotosStorageReference = mFirebaseStorage.getReference().child("photos");

        mPhotoPickerButton = myView.findViewById(R.id.btn_pic);
        // ImagePickerButton shows an image picker to upload a image for a message
        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                // intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), MainActivity.RC_PHOTO_PICKER);
            }
        });


        myRef = FirebaseDatabase.getInstance().getReference().child("photos").child(mUsername);
        myValueEventlistener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                havepic = true;
                imageurl = dataSnapshot.getValue(String.class);
                Log.w(TAG, "have a url which is " + imageurl);
                //call the download.
                DownloadImage(imageurl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read simple value.", error.toException());
                havepic = false;
            }
        };
        return myView;
    }

    void DownloadImage(String imageUrl) {

        // Download directly from StorageReference using Glide
// (See MyAppGlideModule for Loader registration)
        Glide.with(this /* context */)
            .load(imageUrl)
            .into(myPic);
    }

    void deleteImage(String imageUrl) {
        StorageReference delRef;
        try {
            delRef = mFirebaseStorage.getReferenceFromUrl(imageUrl);
            Log.w(TAG, "ref is " + delRef.toString());
        } catch (Exception E) {
            Log.w(TAG, "Attempted to delete something that is not there.");
            return;
        }
        delRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // File deleted successfully
                Log.w(TAG, "file deleted.");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Uh-oh, an error occurred!
                Log.w(TAG, "file DID NOT delete.");
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            if (havepic) {
                //delete the old pic first
                deleteImage(imageurl);
            }
            String filename = selectedImageUri.getLastPathSegment();  //now only get numbers?  why???
            //other way, still getting a number not file name... I don't know why...
            //List<String> filenames =  selectedImageUri.getPathSegments();
            //String filename = filenames.get(filenames.size()-1);
            Log.wtf(TAG, "ORG filename is " + filename);
            //sometimes it get more of the filepath and I don't know why, so this should fix the name.
            if (filename.contains("/")) {
                String[] list = filename.split("/");
                filename = list[list.length];
            }
            Log.wtf(TAG, "NEW filename is " + filename);
            // Get a reference to store file at chat_photos/<FILENAME>
            //fix this so it's just the file name...
            final StorageReference photoRef = mPhotosStorageReference.child(filename);

            // Upload file to Firebase Storage
            UploadTask uploadTask = photoRef.putFile(selectedImageUri);
            //now we need the download url to add the db, but have do it with second task.
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    Log.wtf(TAG, photoRef.toString());
                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();

                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.wtf(TAG, "url is" + downloadUri.toString());
                        mFirebaseDatabaseReference.child("photos").child(mUsername).setValue(downloadUri.toString());

                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });


        }
    }

    @Override
    public void onPause() {
        myRef.removeEventListener(myValueEventlistener);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        myRef.addValueEventListener(myValueEventlistener);
    }
}
