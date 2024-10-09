package edu.cs4730.documentscannerdemo;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import edu.cs4730.documentscannerdemo.databinding.ActivityMainBinding;


/**
 * Simple example of the document scanner.  It doesn't work as well as they say it will on the web pages
 * but it does work.   Press the take picture button and then it wil scan your document and display it here.
 * https://developers.google.com/ml-kit/vision/doc-scanner
 *
 */

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String TAG = "MainActivity";
    GmsDocumentScanner scanner;
    ActivityResultLauncher<IntentSenderRequest> scannerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(1)  //just one page for this example.
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG, GmsDocumentScannerOptions.RESULT_FORMAT_PDF)
            //use ml to clean up the document even removing stains and fingers?
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build();

        scanner = GmsDocumentScanning.getClient(options);
        scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult activityResult) {
                    if (activityResult.getResultCode() == RESULT_OK) {
                        GmsDocumentScanningResult documentScanningResult = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.getData());

                        for (GmsDocumentScanningResult.Page page : documentScanningResult.getPages()) {
                            Uri imageUri = page.getImageUri();
                            binding.imageView.setImageURI(imageUri);
                        }

//                            GmsDocumentScanningResult.Pdf pdf = documentScanningResult.getPdf();
//                            Uri pdfUri = pdf.getUri();
//                            int pageCount = pdf.getPageCount();
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.logger.setText("image failed!");
                            }
                        });
                    }
                }
            });


        binding.takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicture();
            }
        });


    }

    public void getPicture() {

        scanner.getStartScanIntent(this)
            .addOnSuccessListener(intentSender ->
                scannerLauncher.launch(new IntentSenderRequest.Builder(intentSender).build()))
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Task failed with an exception
                    binding.logger.setText("Processor failed!");
                }
            });


    }


}