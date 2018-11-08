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
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private FirebaseStorage mFirebaseStorage;
    private StorageReference mPhotosStorageReference;
    private Button  mPhotoPickerButton;

    private final String TAG = "StorageFragment";
    FirebaseDatabase database;
    TextView logger;
    DatabaseReference myRef;
    ValueEventListener myValueEventlistener;
    public StorageFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myView =  inflater.inflate(R.layout.fragment_storage, container, false);

        mFirebaseAuth = FirebaseAuth.getInstance();
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


        return myView;
    }

    void DownloadImage(String imageUrl) {

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MainActivity.RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();

            String filename = selectedImageUri.getLastPathSegment();  //now only get numbers?  why???
            //other way, still getting a number not file name... I don't know why...
            //List<String> filenames =  selectedImageUri.getPathSegments();
            //String filename = filenames.get(filenames.size()-1);
            Log.wtf(TAG, "ORG filename is "+ filename);
            //sometimes it get more of the filepath and I don't know why, so this should fix the name.
            if (filename.contains("/")) {
                String[] list = filename.split("/");
                filename = list[list.length];
            }
            Log.wtf(TAG, "NEW filename is "+ filename);
            // Get a reference to store file at chat_photos/<FILENAME>
            //fix this so it's just the file name...
            final StorageReference photoRef = mPhotosStorageReference.child(selectedImageUri.getLastPathSegment());

            // Upload file to Firebase Storage
            UploadTask uploadTask = photoRef.putFile(selectedImageUri);
            //now we need the download url to add the db, but have do it with second task.
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return photoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        Log.wtf(TAG, "url is" + downloadUri.toString());
                        myRef = database.getReference("simple");
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            });


        }
    }

}
