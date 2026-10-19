package edu.cs4730.mlkitfacevideoxdemo;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

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
    GraphicOverlay graphicOverlay;
    int lensFacing = CameraSelector.LENS_FACING_BACK;
    FaceDetector faceDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

        //create the faceDectector
        //most accurate one
        FaceDetectorOptions highAccuracyOpts =
            new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)  //PERFORMANCE_MODE_FAST
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking()//smiling and eyes open
                .build();
        //Fastest one
        FaceDetectorOptions fastOpts =
            new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL) //eyes, ears, nose, cheeks, mouth, etc.
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)  //smiling and eyes open
                .enableTracking()
                .build();
        faceDetector = FaceDetection.getClient(fastOpts);

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

                        // 1. Define the preferred size and fallback rules
                        ResolutionStrategy resolutionStrategy = new ResolutionStrategy(
                            //new Size(480, 360), // min resolution to work.
                            new Size(640, 480), // fast resolution hopefully?
                            //new Size(1280, 720), // Target size
                            ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER // Fallback rule if target is unavailable
                        );

                        // 2. Wrap it inside a ResolutionSelector
                        ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
                            .setResolutionStrategy(resolutionStrategy)
                            .build();
                        ImageAnalysis imageAnalysis =
                            new ImageAnalysis.Builder()
                                .setResolutionSelector(resolutionSelector)
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build();
                        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
                            @Override
                            @androidx.camera.core.ExperimentalGetImage
                            public void analyze(@NonNull ImageProxy imageProxy) {
                                int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                                boolean isImageFlipped = lensFacing == CameraSelector.LENS_FACING_FRONT;
                                if (rotationDegrees == 0 || rotationDegrees == 180) {
                                    graphicOverlay.setImageSourceInfo(
                                        imageProxy.getWidth(), imageProxy.getHeight(), isImageFlipped);
                                } else {
                                    graphicOverlay.setImageSourceInfo(
                                        imageProxy.getHeight(), imageProxy.getWidth(), isImageFlipped);
                                }

                                Image mediaImage = imageProxy.getImage();
                                if (mediaImage != null) {
                                    InputImage image = InputImage.fromMediaImage(mediaImage, rotationDegrees);
                                    faceDetector.process(image)
                                        .addOnSuccessListener(executor,
                                            new OnSuccessListener<List<Face>>() {
                                                @Override
                                                public void onSuccess(List<Face> faces) {
                                                    graphicOverlay.clear();
                                                    for (Face face : faces) {
                                                        graphicOverlay.add(new FaceGraphic(graphicOverlay, face));
                                                    }
                                                    graphicOverlay.postInvalidate();
                                                }
                                            })
                                        .addOnFailureListener(executor,
                                            new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.e(TAG, "Face detection failed: " + e.getMessage());
                                                }
                                            })
                                        .addOnCompleteListener(executor,
                                            task -> imageProxy.close());
                                } else {
                                    imageProxy.close();
                                }
                            }
                        });

                        CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
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