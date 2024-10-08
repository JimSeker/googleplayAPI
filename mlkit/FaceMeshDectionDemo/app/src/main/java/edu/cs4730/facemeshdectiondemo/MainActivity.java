package edu.cs4730.facemeshdectiondemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.common.Triangle;
import com.google.mlkit.vision.facemesh.FaceMesh;
import com.google.mlkit.vision.facemesh.FaceMeshDetection;
import com.google.mlkit.vision.facemesh.FaceMeshDetector;
import com.google.mlkit.vision.facemesh.FaceMeshDetectorOptions;
import com.google.mlkit.vision.facemesh.FaceMeshPoint;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.cs4730.facemeshdectiondemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String imagefile;
    Bitmap imagebmp;
    Canvas imageCanvas;
    Paint myColor;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    String TAG = "MainActivity";
    FaceMeshDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });
        binding.process.setEnabled(false);
        binding.process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                procesor();
            }
        });

        //if we only want to recognize a few

        //FACE_MESH (default option): Provides a bounding box and additional face mesh info (468 3D points
        // and triangle info). When compared to the BOUNDING_BOX_ONLY use case, latency increases by ~15%,
        // as measured on Pixel 3.
        FaceMeshDetectorOptions.Builder optionsBuilder = new FaceMeshDetectorOptions.Builder();
        detector = FaceMeshDetection.getClient(optionsBuilder.build());
        //BOUNDING_BOX_ONLY: Only provides a bounding box for a detected face mesh. This is the fastest face
        // detector, but has with range limitation(faces must be within ~2 meters or ~7 feet of the camera).
        /*
        detector = FaceMeshDetection.getClient(
            new FaceMeshDetectorOptions.Builder()
                .setUseCase(FaceMeshDetectorOptions.BOUNDING_BOX_ONLY)
                .build()
        );
        */
        //setup the paint object.
        myColor = new Paint();
        myColor.setColor(Color.WHITE);
        myColor.setStyle(Paint.Style.STROKE);
        myColor.setStrokeWidth(10);
        myColor.setTextSize(myColor.getTextSize() * 10);
        myActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Log.wtf("CAPTURE FILE", "we got a file?");
                        imagebmp = loadAndRotateImage(imagefile);
                        if (imagebmp != null) {
                            imageCanvas = new Canvas(imagebmp);
                            binding.imageView.setImageBitmap(imagebmp);
                            binding.process.setEnabled(true);
                            binding.logger.setText("Image should have loaded correctly");
                        } else {
                            binding.logger.setText("Image failed to load or was canceled.");
                        }
                    }
                }
            });
    }

    public void getPicture() {
        //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        //  File mediaFile = new File(storageDir.getPath() +File.separator + "IMG_" + timeStamp+ ".jpg");
        File mediaFile = new File(storageDir.getPath() + File.separator + "IMG_working.jpg");
        Uri photoURI = FileProvider.getUriForFile(this,
            "edu.cs4730.facemeshdectiondemo.fileprovider",
            mediaFile);

        imagefile = mediaFile.getAbsolutePath();
        Log.wtf("File", imagefile);
        // Uri photoURI = getUriForFile(this, "edu.cs4730.piccapture3.fileprovider",mediaFile);
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        myActivityResultLauncher.launch(intent);
    }

    public void procesor() {
        if (imagebmp == null) return;
        // InputImage image = InputImage.fromBitmap(bitmap, rotationDegree);
        InputImage image = InputImage.fromBitmap(imagebmp, 0);
        Task<List<FaceMesh>> result = detector.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<FaceMesh>>() {
                @Override
                public void onSuccess(List<FaceMesh> faceMeshs) {
                    // Task completed successfully
                    if (faceMeshs == null) {
                        binding.logger.setText("No faces found.");
                        return;
                    }

                    for (FaceMesh faceMesh : faceMeshs) {
                        Rect bounds = faceMesh.getBoundingBox();
                        imageCanvas.drawRect(bounds, myColor);
                        // Gets all points
//                        List<FaceMeshPoint> faceMeshpoints = faceMesh.getAllPoints();
//                        for (FaceMeshPoint faceMeshpoint : faceMeshpoints) {
//                            int index = faceMeshpoint.getIndex();
//                            PointF3D position = faceMeshpoint.getPosition();
//                        }
                        Log.d(TAG, "We have something " + faceMeshs.size());
                        // Gets triangle info
                        List<Triangle<FaceMeshPoint>> triangles = faceMesh.getAllTriangles();
                        for (Triangle<FaceMeshPoint> triangle : triangles) {
                            // 3 Points connecting to each other and representing a triangle area.
                            List<FaceMeshPoint> faceMeshPoints = triangle.getAllPoints();
                            PointF3D point1 = faceMeshPoints.get(0).getPosition();
                            PointF3D point2 = faceMeshPoints.get(1).getPosition();
                            PointF3D point3 = faceMeshPoints.get(2).getPosition();

                            imageCanvas.drawLine(point1.getX(), point1.getY(), point2.getX(), point2.getY(), myColor);
                            imageCanvas.drawLine(point2.getX(), point2.getY(), point3.getX(), point3.getY(), myColor);
                            imageCanvas.drawLine(point3.getX(), point3.getY(), point1.getX(), point1.getY(), myColor);
                            binding.imageView.setImageBitmap(imagebmp);
                            binding.imageView.invalidate();
                        }
                    }
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    binding.logger.setText("Processor failed!");
                }
            });
    }


    /**
     * loads and rotates a file as needed, based on the orientation found in the file
     */

    public Bitmap loadAndRotateImage(String path) {
        int rotate = 0;
        ExifInterface exif;

        Bitmap bitmap = BitmapFactory.decodeFile(path);
        try {
            exif = new ExifInterface(path);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
            bitmap.getHeight(), matrix, true);
    }


}