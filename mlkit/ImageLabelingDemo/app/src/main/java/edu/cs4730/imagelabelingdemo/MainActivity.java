package edu.cs4730.imagelabelingdemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import edu.cs4730.imagelabelingdemo.databinding.ActivityMainBinding;

/**
 * very simple imagelabeling example.  take a picture and then process
 * the image and it will draw the text, confidence value, and index (assuming of the 500ish objects it can find)
 */
//https://developers.google.com/ml-kit/vision/image-labeling/android

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    String imagefile;
    Bitmap imagebmp;
    Canvas imageCanvas;
    Paint myColor;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    String TAG = "MainActivity";
    ImageLabeler labeler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.main.getId()), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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


        // To use default options:
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
        // Or, to set the minimum confidence required:
//        ImageLabelerOptions options =
//            new ImageLabelerOptions.Builder()
//                .setConfidenceThreshold(0.7f)
//                .build();
//        labeler = ImageLabeling.getClient(options);


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
            "edu.cs4730.imagelabelingdemo.fileprovider",
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
        Task<List<ImageLabel>> result = labeler.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                @Override
                public void onSuccess(List<ImageLabel> labels) {

                    Paint.FontMetrics fm = myColor.getFontMetrics();
                    float height = fm.bottom - fm.top + fm.leading;
                    float startY = height;
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        float confidence = label.getConfidence();
                        int index = label.getIndex();
                        text += " " + confidence + " " + index;
                        imageCanvas.drawText(text, 0, startY, myColor);
                        Log.w(TAG, text);
                        startY += height + 5f;
                    }
                    // Task completed successfully

                    binding.imageView.setImageBitmap(imagebmp);
                    binding.imageView.invalidate();
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