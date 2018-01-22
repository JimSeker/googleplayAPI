package edu.cs4730.nearbyconstreamdemo;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is designed to only have the methods needed to capture a picture
 * The camera2Preview class deals with surface.
 */
public class Camera2CapturePic {
    //where all the camrea info is located.
    Camera2Preview camera2Preview;
    Context context;
    String TAG = "Camera2Capture";

    //variables needed to take a picture

    //needed for take picture
    private Size[] jpegSizes;
    int width = 480;  //240
    int height = 640; //320
    CameraCharacteristics characteristics;
    ImageReader reader;
    Handler backgroudHandler;
    CaptureRequest.Builder captureBuilder;
    List<Surface> outputSurfaces;
    File file;

    Thread myThread = null;



    public Camera2CapturePic(Context context, Camera2Preview camera2Preview) {
        this.camera2Preview = camera2Preview;
        this.context = context;

        if (camera2Preview.mCameraDevice == null) {  //camera must be setup first!
            Log.e(TAG, "mCameraDevice is null!!");
            return;
        }

        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        //setup for taking the picture here, so we only do it once, instead at "take picture" time.
        try {
            characteristics = manager.getCameraCharacteristics(camera2Preview.cameraId);
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);

            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            width = 240;
            height = 320;
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());


            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            //is the setup to take the picture, now the mCameraDevice is initialized.
            //configure the catureBuilder, which is built in listener later on.
            captureBuilder = camera2Preview.mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            // Orientation
            //int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
            int deviceorientation = context.getResources().getConfiguration().orientation;
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, getJpegOrientation(characteristics, deviceorientation));


        } catch (CameraAccessException e) {
            Log.e(TAG, "Error in Camera2CapturePic");
            e.printStackTrace();
        }
    }


    public void setThread(Thread t) {
        myThread = t;
    }

    /*
    ** This is the one to call to take a picture.
    */
    public void TakePicture(File filename) {
        file = filename;
        try {
            camera2Preview.mCameraDevice.createCaptureSession(outputSurfaces, mCaptureStateCallback, backgroudHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, "Error in Take Picture.");
            e.printStackTrace();
        }
    }

    //helper method so set the picture orientation correctly.  This doesn't set the header in jpeg
    // instead it just makes sure the picture is the same way as the phone is when it was taken.
    private int getJpegOrientation(CameraCharacteristics c, int deviceOrientation) {
        if (deviceOrientation == android.view.OrientationEventListener.ORIENTATION_UNKNOWN)
            return 0;
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);

        // Round device orientation to a multiple of 90
        deviceOrientation = (deviceOrientation + 45) / 90 * 90;

        // Reverse device orientation for front-facing cameras
        boolean facingFront = c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT;
        if (facingFront) deviceOrientation = -deviceOrientation;

        // Calculate desired JPEG orientation relative to camera orientation to make
        // the image upright relative to the device orientation
        int jpegOrientation = (sensorOrientation + deviceOrientation + 360) % 360;

        return jpegOrientation;
    }

    //setup for actually taking the picture.

    ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {

        @Override
        public void onImageAvailable(ImageReader reader) {

            Image image = null;
            try {
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                save(bytes);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Error in readerListener filenotfound");
                e.printStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "Error in readerListener IOE");
                e.printStackTrace();
            } finally {
                if (image != null) {
                    image.close();
                }
            }
        }

        private void save(byte[] bytes) throws IOException {
            OutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
                output.flush();
            } finally {
                if (null != output) {
                    output.close();
                }
            }
        }

    };

    CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(CameraCaptureSession session,
                                       CaptureRequest request, TotalCaptureResult result) {

            super.onCaptureCompleted(session, request, result);
            //new tell the system that the file exists , so it will show up in gallery/photos/whatever
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.MediaColumns.DATA, file.toString());
            context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

           // Toast.makeText(context, "Saved:" + file, Toast.LENGTH_SHORT).show();
            Log.v(TAG, "Saved:" + file);

            if (myThread != null) {
                synchronized(myThread) {
                    Log.d(TAG, "waiting up thread to take next pic.");
                    myThread.notify(); //in theory, this should wakeup the thread.  OR myThread.notifyAll()
                }
            }

            camera2Preview.startPreview();
        }

    };

    CameraCaptureSession.StateCallback mCaptureStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(CameraCaptureSession session) {

            try {
                session.capture(captureBuilder.build(), captureListener, backgroudHandler);
            } catch (CameraAccessException e) {
                Log.e(TAG, "Error in mCaptureStatCallback");
                e.printStackTrace();
            }
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {

        }
    };

}
