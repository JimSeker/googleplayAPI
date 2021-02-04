package edu.cs4730.mlkittranslatetextdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

/**
 * this is a very simple example of getting the translator to work.
 * It's all done in oncreate, no buttons.
 *
 * You can change the example in the code to other languages and
 * other test to translate.
 *
 * https://developers.google.com/ml-kit/language/translation/android
 */


public class MainActivity extends AppCompatActivity {

    String text = "hello world";
    Translator translator;
    TextView logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logger = findViewById(R.id.logger);
        //create the options pieces and then get a translator for it.
        TranslatorOptions options =
            new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.GERMAN)
                .build();

        translator = Translation.getClient(options);
        logthis("Translating " + text + " to german");
        //we need ot make sure it has been downloaded (not about 30MB is the size too.
        //so use WIFI!!!
//        DownloadConditions conditions = new DownloadConditions.Builder()
//            .requireWifi()
//            .build();
//        translator.downloadModelIfNeeded(conditions)
//            .addOnSuccessListener(new OnSuccessListener() {
//                @Override
//                public void onSuccess(Object o) {
//                    //successfully download the model needed.
//                    logthis("Successfully download model");
//                }
//
//            })
//            .addOnFailureListener(new OnFailureListener() {
//                @Override
//                public void onFailure(@NonNull Exception e) {
//                    logthis("failed download model");
//                }
//            });
//        translate();
//
        logthis("Starting");
        downloadandtraslate();

    }

    void translate() {
        translator.translate(text)
            .addOnSuccessListener(
                new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        logthis("Translate is " + s);
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        logthis("translate failed " + e.toString());
                    }
                });
    }


    void downloadandtraslate() {
        DownloadConditions conditions = new DownloadConditions.Builder()
            .requireWifi()
            .build();
        logthis("Starting model download as needed.");
        translator.downloadModelIfNeeded(conditions)
            .addOnCompleteListener(
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            logthis("model downloaded and now translating");
                            translator.translate(text)
                                .addOnSuccessListener(
                                    new OnSuccessListener<String>() {
                                        @Override
                                        public void onSuccess(String s) {
                                            logthis("Translate is " + s);
                                        }
                                    })
                                .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            logthis("translate failed " + e.toString());
                                        }
                                    });
                        } else {
                            logthis("Failed to download the model");
                        }
                    }
                });
    }

    void deletemodel() {

        RemoteModelManager modelManager = RemoteModelManager.getInstance();


        TranslateRemoteModel germanModel =
            new TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build();
        modelManager.deleteDownloadedModel(germanModel)
            .addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    logthis("German model deleted");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    logthis("Failed to delete german model");
                }
            });

    }


    void logthis(String item) {
        Log.d("Translate", item);
        if (logger != null) {
            logger.append("\n");
            logger.append(item);
        }
    }
}