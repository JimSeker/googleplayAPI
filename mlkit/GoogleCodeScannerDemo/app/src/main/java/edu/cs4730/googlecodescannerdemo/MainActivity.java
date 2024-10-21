package edu.cs4730.googlecodescannerdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.view.View;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner;
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions;
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning;

import edu.cs4730.googlecodescannerdemo.databinding.ActivityMainBinding;

/**
 * this is a very quick and cool "bar code" scanner.  almost no code and it does all the work.
 * no permissions seem to be needed either.
 */
public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    GmsBarcodeScanner scanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(binding.main.getId()), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scan();
            }
        });

        //setup the scanner pieces
        GmsBarcodeScannerOptions options = new GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE, Barcode.FORMAT_AZTEC).build();
        scanner = GmsBarcodeScanning.getClient(this);
        // Or with a configured options
        // GmsBarcodeScanner scanner = GmsBarcodeScanning.getClient(context, options);
    }

    //this could be done in the click listener, but this will be easier to read.
    public void scan() {
        scanner.startScan().addOnSuccessListener(new OnSuccessListener<Barcode>() {
            @Override
            public void onSuccess(Barcode barcode) {
                String rawValue = barcode.getRawValue();
                binding.logger.append(rawValue + "\n");
            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {
                binding.logger.append("Scanning Canceled\n");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                binding.logger.append("Scanning failed.\n");
            }
        });
    }


}