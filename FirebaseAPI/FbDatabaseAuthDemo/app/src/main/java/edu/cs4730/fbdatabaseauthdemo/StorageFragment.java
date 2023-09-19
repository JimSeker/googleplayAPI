package edu.cs4730.fbdatabaseauthdemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

import edu.cs4730.fbdatabaseauthdemo.databinding.FragmentStorageBinding;


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
    private final String TAG = "StorageFragment";
    DatabaseReference mFirebaseDatabaseReference;
    DatabaseReference myRef;
    ValueEventListener myValueEventlistener;
    FragmentStorageBinding binding;
    boolean havepic = false;
    String imageurl = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentStorageBinding.inflate(inflater, container, false);

        //auth, so I can get the username.
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) mUsername = mFirebaseUser.getDisplayName();
        else mUsername = "anonymous";
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //init storage methods.
        mFirebaseStorage = FirebaseStorage.getInstance();
        mPhotosStorageReference = mFirebaseStorage.getReference().child("photos");

        // ImagePickerButton shows an image picker to upload a image for a message
        binding.btnPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                // intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                picActivityResultLauncher.launch(intent);
                //startActivityForResult(Intent.createChooser(intent, "Complete action using"), MainActivity.RC_PHOTO_PICKER);
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
        return binding.getRoot();
    }

    void DownloadImage(String imageUrl) {
        // Download directly from StorageReference using Glide (See MyAppGlideModule for Loader registration)
        Glide.with(requireContext()).load(imageUrl).into(binding.imagePic);
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

    ActivityResultLauncher<Intent> picActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == Activity.RESULT_OK) {
                // There are no request codes
                Intent data = result.getData();
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
                            Log.wtf(TAG, "The task failed to get download url and add to db.");
                        }
                    }
                });

            } else {
                Toast.makeText(requireContext(), "Image picker was canceled", Toast.LENGTH_SHORT).show();
            }
        }
    });

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
