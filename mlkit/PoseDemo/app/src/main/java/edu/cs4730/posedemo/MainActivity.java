package edu.cs4730.posedemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    String TAG = "MainActivity";
    ActivityResultLauncher<String[]> rpl;
    private final String[] REQUIRED_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    PreviewView viewFinder;
    ExecutorService executor = Executors.newSingleThreadExecutor();
    PoseDetector poseDetector;
    GraphicOverlay graphicOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rpl = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> isGranted) {
                    if (allPermissionsGranted()) {
                        startCamera();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            }
        );
        viewFinder = findViewById(R.id.viewFinder);
        graphicOverlay = findViewById(R.id.graphic_overlay);

        PoseDetectorOptions options =
            new PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build();
        // Accurate pose detector on static images, when depending on the pose-detection-accurate sdk
//        AccuratePoseDetectorOptions options =
//            new AccuratePoseDetectorOptions.Builder()
//                .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
//                .build();

        poseDetector = PoseDetection.getClient(options);

        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            rpl.launch(REQUIRED_PERMISSIONS);
        }
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);


        cameraProviderFuture.addListener(
            new Runnable() {
                @Override
                public void run() {
                    try {
                        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                        Preview preview = (new Preview.Builder()).build();
                        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());

                        ImageAnalysis imageAnalysis =
                            new ImageAnalysis.Builder()
                                // enable the following line if RGBA output is needed.
                                //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                                .setTargetResolution(new Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();
                        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
                            @Override
                            @androidx.camera.core.ExperimentalGetImage
                            public void analyze(@NonNull ImageProxy imageProxy) {
                                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                                // insert your code here.
                                Image mediaImage = imageProxy.getImage();
                                if (mediaImage != null) {
                                    InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                                    // Pass image to an ML Kit Vision API
                                    poseDetector.process(image)
                                        .addOnSuccessListener(
                                            new OnSuccessListener<Pose>() {
                                                @Override
                                                public void onSuccess(Pose pose) {
                                                    // Task completed successfully
                                                    List<PoseLandmark> allPoseLandmarks = pose.getAllPoseLandmarks();
                                                    if (!allPoseLandmarks.isEmpty()) {
                                                        logthis("We have landmarks\n");
                                                    }
                                                    graphicOverlay.clear();
                                                    graphicOverlay.add(new PoseGraphic(graphicOverlay,pose,true ));
                                                    graphicOverlay.postInvalidate();
                                                }
                                            })
                                        .addOnFailureListener(
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                   // logthis(" Task failed " + e.getMessage());
                                                }
                                            });


                                }
                                // after done, release the ImageProxy object
                                imageProxy.close();
                            }
                        });

                        CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build();

                        // Unbind use cases before rebinding
                        cameraProvider.unbindAll();

                        // Bind use cases to camera
                        cameraProvider.bindToLifecycle(
                            MainActivity.this, cameraSelector, imageAnalysis, preview);


                    } catch (Exception e) {
                        Log.e(TAG, "Use case binding failed", e);
                    }
                }
            }, ContextCompat.getMainExecutor(this)
        );
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void logthis(String item) {
        Log.d(TAG, item);
    }
}
