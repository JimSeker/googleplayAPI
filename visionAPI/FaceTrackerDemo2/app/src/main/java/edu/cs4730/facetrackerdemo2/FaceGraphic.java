package edu.cs4730.facetrackerdemo2;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.Landmark;

import java.util.List;

/**
 * it will draw the eyes and mouth based on smiling and eye open.
 */
class FaceGraphic extends GraphicOverlay.Graphic {

    private static final float STROKE_WIDTH = 5.0f;
    //handler, since the facetracker is on another thread.
    protected Handler handler;
    String TAG = "FaceGraphic";
    int mFaceId;

    private Paint RedPaint;
    private Paint GreenPaint;
    private volatile Face mFace;

    FaceGraphic(GraphicOverlay overlay, Handler h) {
        super(overlay);

        handler = h;

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

    public void sendmessage(String logthis) {
        Bundle b = new Bundle();
        b.putString("logthis", logthis);
        Message msg = handler.obtainMessage();
        msg.setData(b);
        msg.arg1 = 1;

        msg.what = 1;  //so the empty message is not used!
        // System.out.println("About to Send message"+ logthis);
        handler.sendMessage(msg);
        // System.out.println("Sent message"+ logthis);
    }


    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        Face face = mFace;
        if (face == null) {
            Log.e(TAG, "FACE IS NULL?!");
            return;
        }
        //send it back via a handler
//        Log.v(TAG, "Left eye is " + face.getIsLeftEyeOpenProbability());
//        Log.v(TAG, "Right eye is " + face.getIsRightEyeOpenProbability());
//        Log.v(TAG, "simle is " + face.getIsSmilingProbability());
        sendmessage("Smile: " + face.getIsSmilingProbability() +
            " Left: " + face.getIsLeftEyeOpenProbability() +
            " Right:" + face.getIsRightEyeOpenProbability());
        // Draws a circle at the position of the detected face, with the face's track id below.
        canvas.drawRect(0, 0, 30, 30, RedPaint);
        int xl = 10;  //how big the x is.
        //first get all the landmarks, we want the eyes and mounth, but there are more
        //https://developers.google.com/android/reference/com/google/android/gms/vision/face/Landmark
        List<Landmark> myLandmark = mFace.getLandmarks();
        float cx, cy;
        float lx = 0f, ly = 0f, mx = 0, my = 0, rx = 0, ry = 0;
        //loop through the list to find each one.  Note, they may not all be listed either.
        if (myLandmark.isEmpty()) {
            Log.e(TAG, "Error, no landmarks!");
        }
        for (Landmark landmark : myLandmark) {
            Log.d(TAG, "Found a landmark" + landmark.toString());
            if (landmark.getType() == Landmark.LEFT_EYE) {
                Log.d(TAG, "LEFT EYE");
                cx = translateX(landmark.getPosition().x);
                cy = translateY(landmark.getPosition().y);
                if (face.getIsLeftEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            } else if (landmark.getType() == Landmark.RIGHT_EYE) {
                Log.d(TAG, "RIGHT EYE");
                cx = translateX(landmark.getPosition().x);
                cy = translateY(landmark.getPosition().y);
                if (face.getIsRightEyeOpenProbability() > .75) {  //open, so show circle
                    canvas.drawCircle(cx, cy, 10, GreenPaint);
                } else { //closed, so show x.
                    canvas.drawLine(cx - xl, cy - xl, cx + xl, cy + xl, RedPaint);
                    canvas.drawLine(cx - xl, cy + xl, cx + xl, cy - xl, RedPaint);
                }
            } else if (landmark.getType() == Landmark.LEFT_MOUTH) {
                Log.d(TAG, "LEFT MOUTH");
                lx = translateX(landmark.getPosition().x);
                ly = translateY(landmark.getPosition().y);
                //canvas.drawCircle(lx, ly, 10, paint);
            } else if (landmark.getType() == Landmark.RIGHT_MOUTH) {
                Log.d(TAG, "RIGHT MOUTH");
                rx = translateX(landmark.getPosition().x);
                ry = translateY(landmark.getPosition().y);
                //canvas.drawCircle(rx, ry, 10, paint);
            } else if (landmark.getType() == Landmark.BOTTOM_MOUTH) {
                Log.d(TAG, "BOTTOM MOUTH");
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
