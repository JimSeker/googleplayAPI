package edu.cs4730.subjectsegmentationdemo;

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

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.ColorInt;
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
import com.google.mlkit.vision.common.PointF3D;
import com.google.mlkit.vision.common.Triangle;
import com.google.mlkit.vision.segmentation.subject.Subject;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import edu.cs4730.subjectsegmentationdemo.databinding.ActivityMainBinding;

/**
 * Very simple use of the segemtation library, uses a picture, then masks the objects in the foreground.
 * based https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart
 * ml kit vision code.  but there is so far to complex for a simple example.
 */


public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String imagefile;
    Bitmap imagebmp;
    Canvas imageCanvas;
    Paint myColor;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    String TAG = "MainActivity";
    SubjectSegmenter segmenter;
    private int imageWidth;
    private int imageHeight;
    List<Subject> subjects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.main.getId()), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return WindowInsetsCompat.CONSUMED;
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

        //The foreground confidence mask lets you distinguish the foreground subject from the background.
//        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
//            .enableForegroundConfidenceMask()
//            .build();
        //Similarly, you can also get a bitmap of the foreground subject.
        //
        //Call enableForegroundBitmap() in the options lets you to later retrieve the foreground bitmap by
        // calling getForegroundBitmap() on the SubjectSegmentationResult object returned after
        // processing the image.
//        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
//            .enableForegroundBitmap()
//            .build();
        //multi subject with mask.
        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
            .enableMultipleSubjects(
                new SubjectSegmenterOptions.SubjectResultOptions.Builder()
                    .enableConfidenceMask()
                    .build())
            .build();
        //multi subject with bitmap.
//        SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
//            .enableMultipleSubjects(
//                new SubjectSegmenterOptions.SubjectResultOptions.Builder()
//                    .enableSubjectBitmap()
//                    .build())
//            .build();

        segmenter = SubjectSegmentation.getClient(options);

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
            "edu.cs4730.subjectsegmentationdemo.fileprovider",
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
        imageWidth = image.getWidth();
        imageHeight = image.getHeight();
        Task<SubjectSegmentationResult> result = segmenter.process(image)
            .addOnSuccessListener(new OnSuccessListener<SubjectSegmentationResult>() {
                @Override
                public void onSuccess(SubjectSegmentationResult result) {
                    // Task completed successfully
                    if (result == null) {
                        binding.logger.setText("No Subjects found.");
                        return;
                    }
                    //to get the list of all the subject .
                    subjects = result.getSubjects();

                    //to get the list of all the subject bitmaps
//                    List<Bitmap> bitmaps = new ArrayList<Bitmap>();
//                    for (Subject subject : subjects) {
//                        bitmaps.add(subject.getBitmap());
//                    }

                    Bitmap bitmap =
                        Bitmap.createBitmap(maskColorsFromFloatBuffer(), imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
                    imageCanvas.drawBitmap(bitmap, 0, 0, myColor);
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
     * Converts FloatBuffer floats from all subjects to ColorInt array that can be used as a mask.
     */
    @ColorInt
    private int[] maskColorsFromFloatBuffer() {
        @ColorInt int[] colors = new int[imageWidth * imageHeight];
        for (int k = 0; k < subjects.size(); k++) {
            Subject subject = subjects.get(k);
            int[] rgb = COLORS[k % COLORS.length];
            int color = Color.argb(128, rgb[0], rgb[1], rgb[2]);
            FloatBuffer mask = subject.getConfidenceMask();
            for (int j = 0; j < subject.getHeight(); j++) {
                for (int i = 0; i < subject.getWidth(); i++) {
                    if (mask.get() > 0.5) {
                        colors[(subject.getStartY() + j) * imageWidth + subject.getStartX() + i] = color;
                    }
                }
            }
            // Reset FloatBuffer pointer to beginning, so that the mask can be redrawn if screen is
            // refreshed
            mask.rewind();
        }
        return colors;
    }

    private static final int[][] COLORS = {
        {255, 0, 255},
        {0, 255, 255},
        {255, 255, 0},
        {255, 0, 0},
        {0, 255, 0},
        {0, 0, 255},
        {128, 0, 128},
        {0, 128, 128},
        {128, 128, 0},
        {128, 0, 0},
        {0, 128, 0},
        {0, 0, 128}
    };

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