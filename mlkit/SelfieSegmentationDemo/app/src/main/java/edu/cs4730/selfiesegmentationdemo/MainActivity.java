package edu.cs4730.selfiesegmentationdemo;

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
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;
import com.google.mlkit.vision.segmentation.Segmentation;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import com.google.mlkit.vision.segmentation.Segmenter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import edu.cs4730.selfiesegmentationdemo.databinding.ActivityMainBinding;


/**
 * Very simple use of the selfie library, uses a picture, then masks out the background in pink leaving
 * the "selfie" clear.   based https://github.com/googlesamples/mlkit/tree/master/android/vision-quickstart
 *  ml kit vision code.  but there is so far to complex for a simple example.
 */

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    String imagefile;
    Bitmap imagebmp;
    Canvas imageCanvas;
    Paint myColor;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    String TAG = "MainActivity";
    Segmenter segmenter;
    private int imageWidth;
    private int imageHeight;
    private int maskWidth;
    private int maskHeight;

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


        SelfieSegmenterOptions options =
            new SelfieSegmenterOptions.Builder()
                //.setDetectorMode(SelfieSegmenterOptions.STREAM_MODE)  //for stream mode with a camera.
                .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
                // .enableRawSizeMask()  //normally smaller then image size, I've turned it off.
                .build();

        segmenter = Segmentation.getClient(options);

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
            "edu.cs4730.selfiesegmentationdemo.fileprovider",
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
        Task<SegmentationMask> result = segmenter.process(image)
            .addOnSuccessListener(new OnSuccessListener<SegmentationMask>() {
                @Override
                public void onSuccess(SegmentationMask result) {
                    // Task completed successfully
                    if (result == null) {
                        binding.logger.setText("No Subject found.");
                        return;
                    }
                    ByteBuffer mask = result.getBuffer();
                    maskWidth = result.getWidth();
                    maskHeight = result.getHeight();
                    Bitmap bitmap =
                        Bitmap.createBitmap(maskColorsFromByteBuffer(mask), maskWidth, maskHeight, Bitmap.Config.ARGB_8888);
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
     * Converts byteBuffer floats to ColorInt array that can be used as a mask.
     */
    @ColorInt
    private int[] maskColorsFromByteBuffer(ByteBuffer byteBuffer) {
        @ColorInt int[] colors = new int[maskWidth * maskHeight];
        for (int i = 0; i < maskWidth * maskHeight; i++) {
            float backgroundLikelihood = 1 - byteBuffer.getFloat();
            if (backgroundLikelihood > 0.9) {
                colors[i] = Color.argb(128, 255, 0, 255);
            } else if (backgroundLikelihood > 0.2) {
                // Linear interpolation to make sure when backgroundLikelihood is 0.2, the alpha is 0 and
                // when backgroundLikelihood is 0.9, the alpha is 128.
                // +0.5 to round the float value to the nearest int.
                int alpha = (int) (182.9 * backgroundLikelihood - 36.6 + 0.5);
                colors[i] = Color.argb(alpha, 255, 0, 255);
            }
        }
        return colors;
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