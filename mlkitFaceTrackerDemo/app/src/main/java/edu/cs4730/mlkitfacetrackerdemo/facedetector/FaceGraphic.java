package edu.cs4730.mlkitfacetrackerdemo.facedetector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

import edu.cs4730.mlkitfacetrackerdemo.GraphicOverlay;


/**
 * Graphic instance for rendering face contours graphic overlay view.
 */
public class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 80.0f;
    private static final float ID_X_OFFSET = -70.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private Paint RedPaint;
    private Paint GreenPaint;
    private static final float STROKE_WIDTH = 5.0f;
    static final String TAG = "FaceGraphic";
    private volatile Face face;

   // protected Handler handler;


    public FaceGraphic(GraphicOverlay overlay, Face face) {
        super(overlay);

        this.face = face;
        final int selectedColor = Color.WHITE;
      //  handler = h;
        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);

        GreenPaint = new Paint();
        GreenPaint.setColor(Color.GREEN);
        GreenPaint.setStyle(Paint.Style.STROKE);
        GreenPaint.setStrokeWidth(STROKE_WIDTH);

        RedPaint = new Paint();
        RedPaint.setColor(Color.RED);
        RedPaint.setStyle(Paint.Style.STROKE);
        RedPaint.setStrokeWidth(STROKE_WIDTH);

    }


    public void sendmessage(String logthis) {
        Log.wtf("graphics", logthis        );
        /*  until I get the main code working again.

        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;

        msg.what = 1;  //so the empty message is not used!
        // System.out.println("About to Send message"+ logthis);
        handler.sendMessage(msg);
        // System.out.println("Sent message"+ logthis);


         */
    }
    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = this.face;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
        canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);

        // Draws a bounding box around the face.
        float xOffset = scale(face.getBoundingBox().width() / 2.0f);
        float yOffset = scale(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);

        /** contuor code here.
         FirebaseVisionFaceContour contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS);
         for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
         float px = translateX(point.getX());
         float py = translateY(point.getY());
         canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
         }
         */

        sendmessage("Smile: " + face.getSmilingProbability() +
            " Left: " + face.getLeftEyeOpenProbability() +
            " Right:" + face.getRightEyeOpenProbability());
/*
    if (face.getSmilingProbability() >= 0) {
      canvas.drawText(
          "happiness: " + String.format("%.2f", face.getSmilingProbability()),
          x + ID_X_OFFSET * 3,
          y - ID_Y_OFFSET,
          idPaint);
    }

    if (face.getRightEyeOpenProbability() >= 0) {
      canvas.drawText(
          "right1 eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
          x - ID_X_OFFSET,
          y,
          idPaint);
    }
    if (face.getLeftEyeOpenProbability() >= 0) {
      canvas.drawText(
          "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
          x + ID_X_OFFSET * 6,
          y,
          idPaint);
    }

 */


        float cx, cy;
        float lx = 0f, ly = 0f, mx = 0, my = 0, rx = 0, ry = 0;
        int xl = 10;  //how big the x is.

        FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
        if (leftEye != null && leftEye.getPosition() != null) {
            Log.d(TAG, "LEFT EYE");
            cx = translateX(leftEye.getPosition().x);
            cy = translateY(leftEye.getPosition().y);
            if (face.getLeftEyeOpenProbability() != null) {
                if (face.getLeftEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            }
        }

        FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
        if (rightEye != null && rightEye.getPosition() != null) {
            Log.d(TAG, "RIGHT EYE");
            cx = translateX(rightEye.getPosition().x);
            cy = translateY(rightEye.getPosition().y);
            if (face.getRightEyeOpenProbability() != null) {
                if (face.getRightEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            }
        }

        FaceLandmark leftmouth = face.getLandmark(FaceLandmark.MOUTH_LEFT);
        FaceLandmark rightmouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
        FaceLandmark bottommouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
        if (leftmouth != null && rightmouth != null && bottommouth != null &&
            leftmouth.getPosition() != null && rightmouth.getPosition() != null && bottommouth.getPosition() != null) {
            Log.d(TAG, "LEFT MOUTH");
            lx = translateX(leftmouth.getPosition().x);
            ly = translateY(leftmouth.getPosition().y);

            Log.d(TAG, "RIGHT MOUTH");
            rx = translateX(rightmouth.getPosition().x);
            ry = translateY(rightmouth.getPosition().y);

            Log.d(TAG, "BOTTOM MOUTH");
            mx = translateX(bottommouth.getPosition().x );
            my = translateY(bottommouth.getPosition().y);


            //now draw the mouth.   First check if all points exists
            if (lx > 0.0f && rx > 0.0f && mx > 0.0f) {
                //so first if one side of the mouth is higher, use that one.
                Log.v(TAG, "Drawing mouth");
                if (ly < ry)  //left side is higher
                    ry = ly;
                else
                    ly = ry;
                //okay, the points exist, so lets draw
                if (face.getSmilingProbability()  != null) {
                    if (face.getSmilingProbability() > .75) { //smiling draw rec
                        canvas.drawRect(lx, ly, rx, my, GreenPaint);
                    } else {  //no smiling draw x
                        canvas.drawLine(lx, ly, rx, my, RedPaint);
                        canvas.drawLine(lx, my, rx, ry, RedPaint);
                    }
                }
            }
        }


        FaceLandmark rightear = face.getLandmark(FaceLandmark.RIGHT_EAR);
        if (rightear != null && rightear.getPosition() != null) {
            canvas.drawCircle(
                translateX(rightear.getPosition().x),
                translateY(rightear.getPosition().y),
                FACE_POSITION_RADIUS,
                facePositionPaint);
        }
        FaceLandmark leftear = face.getLandmark(FaceLandmark.LEFT_EAR);
        if (leftear != null && leftear.getPosition() != null) {
            canvas.drawCircle(
                translateX(leftear.getPosition().x),
                translateY(leftear.getPosition().y),
                FACE_POSITION_RADIUS,
                facePositionPaint);
        }

        FaceLandmark leftCheek = face.getLandmark(FaceLandmark.LEFT_CHEEK);
        if (leftCheek != null && leftCheek.getPosition() != null) {
            canvas.drawCircle(
                translateX(leftCheek.getPosition().x),
                translateY(leftCheek.getPosition().y),
                FACE_POSITION_RADIUS,
                facePositionPaint);
        }
        FaceLandmark rightCheek =
            face.getLandmark(FaceLandmark.RIGHT_CHEEK);
        if (rightCheek != null && rightCheek.getPosition() != null) {
            canvas.drawCircle(
                translateX(rightCheek.getPosition().x),
                translateY(rightCheek.getPosition().y),
                FACE_POSITION_RADIUS,
                facePositionPaint);
        }
    }
}
