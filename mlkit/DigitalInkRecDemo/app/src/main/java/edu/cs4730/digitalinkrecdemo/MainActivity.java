package edu.cs4730.digitalinkrecdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.MlKitException;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizer;
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions;
import com.google.mlkit.vision.digitalink.recognition.Ink;
import com.google.mlkit.vision.digitalink.common.RecognitionResult;

import edu.cs4730.digitalinkrecdemo.databinding.ActivityMainBinding;

/**
 * https://developers.google.com/ml-kit/vision/digital-ink-recognition
 * This an example using the digital ink recognition.  it seems to work pretty well.
 */

public class MainActivity extends AppCompatActivity {

    final int boardsize = 1000;
    Bitmap theboard;
    Canvas theboardc;
    float startx, starty;
    ActivityMainBinding binding;
    Ink.Builder inkBuilder;
    Ink.Stroke.Builder strokeBuilder;
    Paint paint;
    DigitalInkRecognizer recognizer;
    final String TAG = "mainactivity";

    @SuppressLint("ClickableViewAccessibility")
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
        theboard = Bitmap.createBitmap(boardsize, boardsize, Bitmap.Config.ARGB_8888);
        theboardc = new Canvas(theboard);
        theboardc.drawColor(Color.WHITE);  //background color for the board.
        binding.imageView.setImageBitmap(theboard);

        inkBuilder = Ink.builder();
        paint = new Paint();

        binding.imageView.setOnTouchListener(new View.OnTouchListener() {
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
                        binding.imageView.setImageBitmap(theboard);
                        startx = x;
                        starty = y;
                        return true;
                    case MotionEvent.ACTION_UP:
                        strokeBuilder.addPoint(Ink.Point.create(x, y, t));
                        inkBuilder.addStroke(strokeBuilder.build());
                        strokeBuilder = null;
                        theboardc.drawLine(startx, starty, x, y, paint);
                        binding.imageView.setImageBitmap(theboard);
                        return true;
                }
                return false;
            }
        });
        binding.clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inkBuilder = Ink.builder();  //clear the builder.
                theboardc.drawColor(Color.WHITE);  //background color for the board.
                binding.imageView.setImageBitmap(theboard);
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

        binding.recognize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Ink ink = inkBuilder.build();
                if (recognizer != null) {
                    recognizer.recognize(ink)
                        .addOnSuccessListener(new OnSuccessListener<RecognitionResult>() {
                            @Override
                            public void onSuccess(RecognitionResult result) {
                                binding.logger.append("\nresult is " + result.getCandidates().get(0).getText());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                binding.logger.append("\nFailed to recognize.");
                            }
                        });
                }
            }
        });
    }
}