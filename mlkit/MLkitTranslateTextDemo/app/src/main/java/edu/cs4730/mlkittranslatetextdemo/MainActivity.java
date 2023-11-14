package edu.cs4730.mlkittranslatetextdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import edu.cs4730.mlkittranslatetextdemo.databinding.ActivityMainBinding;

/**
 * this is a very simple example of getting the translator to work.
 * It's all done in oncreate, no buttons.
 * <p>
 * You can change the example in the code to other languages and
 * other test to translate.
 * <p>
 * https://developers.google.com/ml-kit/language/translation/android
 */


public class MainActivity extends AppCompatActivity {

    String text = "hello world";
    Translator translator;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //create the options pieces and then get a translator for it.
        TranslatorOptions options = new TranslatorOptions.Builder().setSourceLanguage(TranslateLanguage.ENGLISH).setTargetLanguage(TranslateLanguage.GERMAN).build();

        translator = Translation.getClient(options);

        getLifecycle().addObserver(translator); //ensures the translator is closed when not needed.

        logthis("Translating " + text + " to german");
        logthis("Starting, wait for first translation to complete.");
        downloadandtraslate();

        binding.button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!binding.etText.getText().toString().isEmpty()) {
                            text = binding.etText.getText().toString();
                            translate();
                        }

                    }
                }
        );
    }

    void translate() {
        translator.translate(text).addOnSuccessListener(new OnSuccessListener<String>() {
            @Override
            public void onSuccess(String s) {
                logthis("Translate is " + s);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("translate failed " + e.toString());
            }
        });
    }


    void downloadandtraslate() {
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
        logthis("Starting model download as needed.");
        translator.downloadModelIfNeeded(conditions).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    logthis("model downloaded and now translating");
                    translator.translate(text).addOnSuccessListener(new OnSuccessListener<String>() {
                        @Override
                        public void onSuccess(String s) {
                            logthis("Translate is " + s);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
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

        TranslateRemoteModel germanModel = new TranslateRemoteModel.Builder(TranslateLanguage.GERMAN).build();
        modelManager.deleteDownloadedModel(germanModel).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                logthis("German model deleted");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                logthis("Failed to delete german model");
            }
        });

    }


    void logthis(String item) {
        Log.d("Translate", item);
        binding.logger.append("\n");
        binding.logger.append(item);

    }
}