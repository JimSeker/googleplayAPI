package edu.cs4730.mlfacetrackerdemo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import edu.cs4730.mlfacetrackerdemo.common.CameraImageGraphic;
import edu.cs4730.mlfacetrackerdemo.common.FrameMetadata;
import edu.cs4730.mlfacetrackerdemo.common.GraphicOverlay;


/**
 * Face Contour Demo.
 */
public class FaceDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";
    //handler, since the facetracker is on another thread.
    protected Handler handler;
    private final FirebaseVisionFaceDetector detector;

    public FaceDetectorProcessor(Handler h) {
        handler = h;
        FirebaseVisionFaceDetectorOptions options =
            new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
               // .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)  //contours of "prominent face
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)  //eyes open pieces.
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)//landmarks
                .enableTracking()  //don't enable with contour, breaks it.
                .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
        @Nullable Bitmap originalCameraImage,
        @NonNull List<FirebaseVisionFace> faces,
        @NonNull FrameMetadata frameMetadata,
        @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            FaceGraphic faceGraphic = new FaceGraphic(graphicOverlay, face, handler);
            graphicOverlay.add(faceGraphic);
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}