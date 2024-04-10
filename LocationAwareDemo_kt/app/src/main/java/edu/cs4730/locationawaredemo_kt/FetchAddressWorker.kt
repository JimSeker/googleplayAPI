package edu.cs4730.locationawaredemo_kt

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.text.TextUtils
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.IOException
import java.util.Locale

/**
 * This takes the lat and long as parameters.
 * using the Geocoder with the lat and lng, it finds the address.
 * returns the address as a string back.
 */
class FetchAddressWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {
    override fun doWork(): Result {
        val mLatitude = inputData.getDouble(Constants.LATITUDE, 0.0)
        val mLongitude = inputData.getDouble(Constants.LONGITUDE, 0.0)
        var returnMessage = ""
        val success = false

        // Errors could still arise from using the Geocoder (for example, if there is no
        // connectivity, or if the Geocoder is given illegal location data). Or, the Geocoder may
        // simply not have an address for a location. In all these cases, we communicate with the
        // receiver using a resultCode indicating failure. If an address is found, we use a
        // resultCode indicating success.

        // The Geocoder used in this sample. The Geocoder's responses are localized for the given
        // Locale, which represents a specific geographical or linguistic region. Locales are used
        // to alter the presentation of information such as numbers or dates to suit the conventions
        // in the region they describe.
        val geocoder = Geocoder(applicationContext, Locale.getDefault())

        // Address found using the Geocoder.
        var addresses: List<Address>? = null
        try {
            // Using getFromLocation() returns an array of Addresses for the area immediately
            // surrounding the given latitude and longitude. The results are a best guess and are
            // not guaranteed to be accurate.
            //https://stackoverflow.com/questions/73456748/geocoder-getfromlocation-deprecated
            //basically this is blocking and google doesn't want that anymore.  of course, the point
            //sevice is so it can block.  This whole example piece can be rewritten now when I want to.
            addresses = geocoder.getFromLocation(
                mLatitude, mLongitude,
                1
            ) // In this sample, we get just a single address.
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            returnMessage = applicationContext.getString(R.string.service_not_available)
            Log.e(TAG, returnMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            returnMessage = applicationContext.getString(R.string.invalid_lat_long_used)
            Log.e(
                TAG, returnMessage + ". " +
                        "Latitude = " + mLatitude +
                        ", Longitude = " + mLongitude, illegalArgumentException
            )
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.isEmpty()) {
            if (returnMessage.isEmpty()) {
                returnMessage = applicationContext.getString(R.string.no_address_found)
                Log.e(TAG, returnMessage)
            }
        } else {
            val address = addresses[0]
            val addressFragments = ArrayList<String?>()

            // Fetch the address lines using {@code getAddressLine},
            // join them, and send them to the thread. The {@link android.location.address}
            // class provides other options for fetching address details that you may prefer
            // to use. Here are some examples:
            // getLocality() ("Mountain View", for example)
            // getAdminArea() ("CA", for example)
            // getPostalCode() ("94043", for example)
            // getCountryCode() ("US", for example)
            // getCountryName() ("United States", for example)
            for (i in 0..address.maxAddressLineIndex) {
                //Log.d(TAG, "line " + i +" is " +address.getAddressLine(i));
                addressFragments.add(address.getAddressLine(i))
            }
            Log.i(TAG, applicationContext.getString(R.string.address_found))
            returnMessage = TextUtils.join(System.getProperty("line.separator"), addressFragments)
        }
        //...set the output, and we're done!
        val output = Data.Builder()
            .putString(Constants.RESULT_DATA_KEY, returnMessage)
            .putInt(Constants.RESULT_CODE, Constants.SUCCESS_RESULT)
            .build()
        return Result.success(output)
    }

    companion object {
        private const val TAG = "FetchAddressWorker"
    }
}
