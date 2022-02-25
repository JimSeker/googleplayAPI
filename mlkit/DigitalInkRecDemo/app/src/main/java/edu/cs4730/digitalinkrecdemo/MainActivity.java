package edu.cs4730.digitalinkrecdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.Ink;
import com.google.mlkit.vision.digitalink.RecognitionResult;

/**
 * https://developers.google.com/ml-kit/vision/digital-ink-recognition 
 */

public class MainActivity extends AppCompatActivity {

    final int boardsize = 1000;
    Bitmap theboard;
    Canvas theboardc;
    float startx, starty;
    Button button, clear;
    ImageView iv;
    TextView logger;
    Ink.Builder inkBuilder;
    Ink.Stroke.Builder strokeBuilder;
    Paint paint;
    DigitalInkRecognizer recognizer;
    final String TAG = "mainactivity";

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        clear = findViewById(R.id.button2);
        iv = findViewById(R.id.imageView);
        logger = findViewById(R.id.logger);


        theboard = Bitmap.createBitmap(boardsize, boardsize, Bitmap.Config.ARGB_8888);
        theboardc = new Canvas(theboard);
        theboardc.drawColor(Color.WHITE);  //background color for the board.
        iv.setImageBitmap(theboard);

        inkBuilder = Ink.builder();
        paint = new Paint();

        iv.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                float x = event.getX();
                float y = event.getY();
                long t = System.currentTimeMillis();

                // If your setup does not provide timing information, you can omit the
                // third parameter (t) in the calls to Ink.Point.create
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        strokeBuilder = Ink.Stroke.builder();
                        strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                        startx = x;
                        starty = y;
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                        theboardc.drawLine(startx, starty, x, y, paint);
                        iv.setImageBitmap(theboard);
                        startx = x;
                        starty = y;
                        return true;
                    case MotionEvent.ACTION_UP:
                        strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                        inkBuilder.addStroke(strokeBuilder.build());
                        strokeBuilder = null;
                        theboardc.drawLine(startx, starty, x, y, paint);
                        iv.setImageBitmap(theboard);
                        return true;
                }
                return false;
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inkBuilder = Ink.builder();  //clear the builder.
                theboardc.drawColor(Color.WHITE);  //background color for the board.
                iv.setImageBitmap(theboard);
            }
        });
        DigitalInkRecognitionModelIdentifier modelIdentifier = null;
        try {
            modelIdentifier = DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US");
        } catch (MlKitException e) {
            // language tag failed to parse, handle error.
        }
        if (modelIdentifier != null) {
            DigitalInkRecognitionModel model = DigitalInkRecognitionModel.builder(modelIdentifier).build();

            RemoteModelManager remoteModelManager = RemoteModelManager.getInstance();
            remoteModelManager
                .download(model, new DownloadConditions.Builder().build())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Model downloaded");
                        recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build());
                    }
                })
                .addOnFailureListener(
                    e -> Log.e(TAG, "Error while downloading a model: " + e));


            // Get a recognizer for the language
           // recognizer = DigitalInkRecognition.getClient(DigitalInkRecognizerOptions.builder(model).build());
        } else {
            recognizer = null;
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ink ink = inkBuilder.build();
                if (recognizer != null) {
                    recognizer.recognize(ink)
                        .addOnSuccessListener(new OnSuccessListener<RecognitionResult>() {
                            @Override
                            public void onSuccess(RecognitionResult result) {
                                logger.append("\nresult is " + result.getCandidates().get(0).getText());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                logger.append("\nFailed to recognize.");
                            }
                        });
                }
            }
        });
    }
}