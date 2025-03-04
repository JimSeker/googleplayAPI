package edu.cs4730.barcodepicdemo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.cs4730.barcodepicdemo.databinding.ActivityMainBinding;

/**
 * This is an example of using the mlkit barcode scanner with a picture.
 * It's pretty simple, but workable example.  It takes picture via an intent
 * and then it can be processed via another button.
 * <p>
 * Note, likely the intent should turn down the image size, it really don't need to be that high of resolution.
 *
 * note the QR codes don't seem to read well, while things the camera app can find them, this app fails on the same
 * qr code.  Note, updated library seems to have fixed the QR code issues.
 */

public class MainActivity extends AppCompatActivity {

   ActivityMainBinding binding;
    String imagefile;
    Bitmap imagebmp;
    Canvas imageCanvas;
    Paint myColor;
    ActivityResultLauncher<Intent> myActivityResultLauncher;
    BarcodeScannerOptions options;
    String TAG = "MainActivity";

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

        //if we only want to recognize a few
        options =
            new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(   //default is all formats.
                   // Barcode.FORMAT_QR_CODE,
                    Barcode.FORMAT_ALL_FORMATS)
                   // Barcode.FORMAT_UPC_A)
                .build();

        //setup the paint object.
        myColor = new Paint();
        myColor.setColor(Color.RED);
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
            "edu.cs4730.barcodepicdemo.fileprovider",
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
        BarcodeScanner scanner = BarcodeScanning.getClient();
        // Or, to specify the formats to recognize:
        // BarcodeScanner scanner = BarcodeScanning.getClient(options);
        Task<List<Barcode>> result = scanner.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                @Override
                public void onSuccess(List<Barcode> barcodes) {
                    // Task completed successfully
                    if (barcodes == null) {
                        binding.logger.setText("No barcodes found.");
                        return;
                    }
                    Log.d(TAG, "We have something " + barcodes.size());
                    for (Barcode barcode : barcodes) {
                        Log.wtf(TAG, barcode.getDisplayValue());
                        binding.logger.setText("Success: " + barcode.getDisplayValue());
                        //lets draw a box around it, since we using the bmp, no scaling is needed either.  a camera image would need to be scaled.
                        Rect rect = barcode.getBoundingBox();
                        imageCanvas.drawRect(rect, myColor);
                        imageCanvas.drawText(barcode.getDisplayValue(), rect.left, rect.bottom, myColor);
                        binding.imageView.setImageBitmap(imagebmp);
                        binding.imageView.invalidate();
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