package edu.cs4730.locationawaredemo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressWorker extends Worker {
    private static final String TAG = "FetchAddressWorker";
    public FetchAddressWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    //depreciated 
    @NonNull
    @Override
    public Result doWork() {
        double mLatitude =getInputData().getDouble(Constants.LATITUDE, 0D);
        double mLongitude = getInputData().getDouble(Constants.LONGITUDE, 0D);
        String returnMessage = "";
        boolean success = false;

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            addresses = geocoder.getFromLocation(
                  mLatitude,
                  mLongitude,
                    // In this sample, we get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            returnMessage = getApplicationContext().getString(R.string.service_not_available);
            Log.e(TAG, returnMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            returnMessage =  getApplicationContext().getString(R.string.invalid_lat_long_used);
            Log.e(TAG, returnMessage + ". " +
                    "Latitude = " + mLatitude +
                    ", Longitude = " + mLongitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {
            if (returnMessage.isEmpty()) {
                returnMessage = getApplicationContext().getString(R.string.no_address_found);
                Log.e(TAG, returnMessage);
            }

            success = false; //already set, but just to ensure.
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)

            for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                //Log.d(TAG, "line " + i +" is " +address.getAddressLine(i));
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, getApplicationContext().getString(R.string.address_found));
            success = true;
            returnMessage =  TextUtils.join(System.getProperty("line.separator"), addressFragments);
        }
        //...set the output, and we're done!
        Data output;
        if (success)
                output = new Data.Builder()
                .putString(Constants.RESULT_DATA_KEY, returnMessage)
                        .putInt(Constants.RESULT_CODE, Constants.SUCCESS_RESULT)
                .build();
        else
            output = new Data.Builder()
                    .putString(Constants.RESULT_DATA_KEY, returnMessage)
                    .putInt(Constants.RESULT_CODE, Constants.FAILURE_RESULT)
                    .build();

        return Result.success(output);
    }
}
