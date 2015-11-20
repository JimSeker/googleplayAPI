package edu.cs4730.facetrackerdemo2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

/**
 *
 * it will draw the eyes and mouth based on smiling and eye open.
 *
 */
class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float STROKE_WIDTH = 5.0f;

    String TAG = "FaceGraphic";
    int mFaceId;

    private Paint RedPaint;
    private Paint GreenPaint;
    private volatile Face mFace;

    FaceGraphic(GraphicOverlay overlay) {
        super(overlay);

        GreenPaint = new Paint();
        GreenPaint.setColor(Color.GREEN);
        GreenPaint.setStyle(Paint.Style.STROKE);
        GreenPaint.setStrokeWidth(STROKE_WIDTH);

        RedPaint = new Paint();
        RedPaint.setColor(Color.RED);
        RedPaint.setStyle(Paint.Style.STROKE);
        RedPaint.setStrokeWidth(STROKE_WIDTH);

    }

    void setId(int id) {
        mFaceId = id;
    }

    /**
     * Updates the face instance from the detection of the most recent frame.  Invalidates the
     * relevant portions of the overlay to trigger a redraw.
     */
    void updateFace(Face face) {
        mFace = face;
        postInvalidate();
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            return;
        }

        Log.v(TAG, "Left eye is " + face.getIsLeftEyeOpenProbability());
        Log.v(TAG, "Right eye is " + face.getIsRightEyeOpenProbability());
        Log.v(TAG, "simle is " + face.getIsSmilingProbability());
        // Draws a circle at the position of the detected face, with the face's track id below.

        int xl = 10;  //how big the x is.
        //first get all the landmarks, we want the eyes and mounth, but there are more
        //https://developers.google.com/android/reference/com/google/android/gms/vision/face/Landmark
        List<Landmark> myLandmark = mFace.getLandmarks();
        float cx, cy;
        float lx = 0f, ly = 0f, mx = 0, my = 0, rx = 0, ry = 0;
        //loop through the list to find each one.  Note, they may not all be listed either.
        for (Landmark landmark : myLandmark) {

            if (landmark.getType() == Landmark.LEFT_EYE) {
                cx = translateX(landmark.getPosition().x);
                cy = translateY(landmark.getPosition().y);
                if (face.getIsLeftEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            } else if (landmark.getType() == Landmark.RIGHT_EYE) {
                cx = translateX(landmark.getPosition().x);
                cy = translateY(landmark.getPosition().y);
                if (face.getIsRightEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            } else if (landmark.getType() == Landmark.LEFT_MOUTH) {
                lx = translateX(landmark.getPosition().x);
                ly = translateY(landmark.getPosition().y);
                //canvas.drawCircle(lx, ly, 10, paint);
            } else if (landmark.getType() == Landmark.RIGHT_MOUTH) {
                rx = translateX(landmark.getPosition().x);
                ry = translateY(landmark.getPosition().y);
                //canvas.drawCircle(rx, ry, 10, paint);
            } else if (landmark.getType() == Landmark.BOTTOM_MOUTH) {
                mx = translateX(landmark.getPosition().x);
                my = translateY(landmark.getPosition().y);
                //canvas.drawCircle(mx, my, 10, paint);
            }
        }
        //now draw the mouth.   First check if all points exists
        if (lx > 0.0f && rx > 0.0f && mx > 0.0f) {
            //so first if one side of the mouth is higher, use that one.
            Log.v(TAG, "Drawing mouth");
            if (ly < ry)  //left side is higher
                ry = ly;
            else
                ly = ry;
            //okay, the points exist, so lets draw
            if (face.getIsSmilingProbability() > .75) { //smiling draw rec
                canvas.drawRect(lx, ly, rx, my, GreenPaint);
            } else {  //no smiling draw x
                canvas.drawLine(lx, ly, rx, my, RedPaint);
                canvas.drawLine(lx, my, rx, ry, RedPaint);
            }
        }

    }
}
