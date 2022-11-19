package edu.cs4730.googlecodescannerdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.service.autofill.TextValueSanitizer;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

/**
 * this is a very quick and cool "bar code" scanner.  almost no code and it does all the work.
 *  no permissions seem to be needed either.
 *
 */
public class MainActivity extends AppCompatActivity {

    TextView logger;
    GmsBarcodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = findViewById(R.id.logger);
        findViewById(R.id.floatingActionButton)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scan();
                    }
                });

        //setup the scanner pieces
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder()
                .setBarcodeFormats(
                        Barcode.FORMAT_QR_CODE,
                        Barcode.FORMAT_AZTEC)
                .build();
        scanner = GmsBarcodeScanning.getClient(this);
        // Or with a configured options
        // GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(context, options);
    }

    //this could be done in the click listener, but this will be easier to read.
    public void scan() {
        scanner.startScan()
                .addOnSuccessListener(new OnSuccessListener<Barcode>() {
                    @Override
                    public void onSuccess(Barcode barcode) {
                        String rawValue = barcode.getRawValue();
                        logger.append(rawValue + "\n");
                    }
                })
                .addOnCanceledListener(new OnCanceledListener() {
                    @Override
                    public void onCanceled() {
                        logger.append("Scanning Canceled\n");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        logger.append("Scanning failed.\n");
                    }
                });
    }


}