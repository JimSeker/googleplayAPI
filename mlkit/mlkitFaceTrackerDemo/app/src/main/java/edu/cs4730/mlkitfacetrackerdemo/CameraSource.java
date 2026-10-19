/*
 * Copyright 2020 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cs4730.mlkitfacetrackerdemo;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.common.MlKitException;

import java.util.concurrent.ExecutionException;

/**
 * Manages the CameraX camera lifecycle, preview, and image analysis pipeline for ML Kit.
 */
public class CameraSource {
    public static final int CAMERA_FACING_BACK = CameraSelector.LENS_FACING_BACK;
    public static final int CAMERA_FACING_FRONT = CameraSelector.LENS_FACING_FRONT;

    private static final String TAG = "CameraSource";

    private final Context context;
    private final GraphicOverlay graphicOverlay;

    private int facing = CAMERA_FACING_BACK;

    @Nullable private ProcessCameraProvider cameraProvider;
    @Nullable private Camera camera;
    @Nullable private Preview previewUseCase;
    @Nullable private ImageAnalysis analysisUseCase;
    @Nullable private VisionImageProcessor frameProcessor;

    @Nullable private LifecycleOwner currentLifecycleOwner;
    @Nullable private PreviewView currentPreviewView;

    public CameraSource(Context context, GraphicOverlay overlay) {
        this.context = context;
        this.graphicOverlay = overlay;
        this.graphicOverlay.clear();
    }

    public void setFacing(int facing) {
        if (facing != CAMERA_FACING_BACK && facing != CAMERA_FACING_FRONT) {
            throw new IllegalArgumentException("Invalid camera facing: " + facing);
        }
        this.facing = facing;
    }

    public int getCameraFacing() {
        return facing;
    }

    public void setMachineLearningFrameProcessor(VisionImageProcessor processor) {
        if (frameProcessor != null) {
            frameProcessor.stop();
        }
        this.frameProcessor = processor;
    }

    /**
     * Starts or rebinds the CameraX camera preview and analysis use cases to the lifecycle owner.
     */
    public void start(LifecycleOwner lifecycleOwner, PreviewView previewView) {
        this.currentLifecycleOwner = lifecycleOwner;
        this.currentPreviewView = previewView;

        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(context);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Unhandled exception binding camera use cases", e);
            }
        }, ContextCompat.getMainExecutor(context));
    }

    private void bindCameraUseCases() {
        if (cameraProvider == null || currentLifecycleOwner == null || currentPreviewView == null) {
            return;
        }

        cameraProvider.unbindAll();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(facing)
                .build();

        previewUseCase = new Preview.Builder().build();
        previewUseCase.setSurfaceProvider(currentPreviewView.getSurfaceProvider());

        boolean isLensFacingFront = (facing == CAMERA_FACING_FRONT);

        analysisUseCase = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        analysisUseCase.setAnalyzer(
                ContextCompat.getMainExecutor(context),
                imageProxy -> {
                    if (frameProcessor != null) {
                        try {
                            frameProcessor.processImageProxy(imageProxy, graphicOverlay, isLensFacingFront);
                        } catch (MlKitException e) {
                            Log.e(TAG, "Failed to process image: " + e.getLocalizedMessage(), e);
                            imageProxy.close();
                        }
                    } else {
                        imageProxy.close();
                    }
                }
        );

        try {
            camera = cameraProvider.bindToLifecycle(
                    currentLifecycleOwner,
                    cameraSelector,
                    previewUseCase,
                    analysisUseCase
            );
        } catch (Exception e) {
            Log.e(TAG, "Use case binding failed", e);
        }
    }

    public void stop() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    public void release() {
        stop();
        if (frameProcessor != null) {
            frameProcessor.stop();
            frameProcessor = null;
        }
    }
}
