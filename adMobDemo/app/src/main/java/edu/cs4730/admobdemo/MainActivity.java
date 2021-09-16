package edu.cs4730.admobdemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.ads.consent.DebugGeography;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.AdView;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

/**
 * api 19.7.0 is a trans version to API 20 for the ads and many things are depreciated.
 * I'm attempting to use the newer methods.
 */


public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private static String TAG = "MainActivity";
    AdView mAdView;
    AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713

        //MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");  //19.5.0 version.
        MobileAds.initialize(this, new OnInitializationCompleteListener() { //19.7.0+ version.
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.wtf(TAG, "Initialization is completed.");
            }
        });

        //for the ad at the bottom of the mainactivity.
        mAdView = findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.d(TAG, "banner ad has finished loading.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
                // Code to be executed when an ad request fails.
                Log.d(TAG, "banner ad has failed to load.");
                // Gets the domain from which the error came.
                String errorDomain = error.getDomain();
                // Gets the error code. See
                // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                // for a list of possible codes.
                int errorCode = error.getCode();
                // Gets an error message.
                // For example "Account not approved yet". See
                // https://support.google.com/admob/answer/9905175 for explanations of
                // common errors.
                String errorMessage = error.getMessage();
                // Gets additional response information about the request. See
                // https://developers.google.com/admob/android/response-info for more
                // information.
                ResponseInfo responseInfo = error.getResponseInfo();
                // Gets the cause of the error, if available.
                AdError cause = error.getCause();
                // All of this information is available via the error's toString() method.
                Log.d("Ads", error.toString());

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                Log.d(TAG, "banner ad is displayed.");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                Log.d(TAG, "banner ad has closed, now do something else.");
            }
        });

        mAdView.loadAd(adRequest);

        ConsentInformation consentInformation = ConsentInformation.getInstance(this);
        consentInformation.addTestDevice("D1A4B2E34EF63965FDB3E19C432D0D82");
        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        String[] publisherIds = {"pub-0123456789012345"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                Log.wtf(TAG, "consent status successfully updated.");
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
                Log.wtf(TAG, "consent status failed to update.");
            }
        });


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showInterstitialAd();
            }
        });

    }


    void showInterstitialAd() {

        //for the Interstitial ad, which is launched via the button
        //"ca-app-pub-3940256099942544/1033173712"  test add.
        InterstitialAd.load(this,
            String.valueOf(R.string.fullscreen_ad_unit_id),
            adRequest,
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                    String TAG = "InterstitialAdload";
                    // The mInterstitialAd reference will be null until
                    // an ad is loaded.
                    mInterstitialAd = interstitialAd;
                    Toast.makeText(MainActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                    //now we can setup the full screen pieces.
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Called when fullscreen content is dismissed.
                            Log.d("TAG", "The ad was dismissed.");
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            // Called when fullscreen content failed to show.
                            Log.d("TAG", "The ad failed to show.");
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            // Called when fullscreen content is shown.
                            // Make sure to set your reference to null so you don't
                            // show it a second time.
                            mInterstitialAd = null;
                            Log.d("TAG", "The ad was shown.");
                        }
                    });

                    //add is loaded and ready to show.
                    Log.i(TAG, "onAdLoaded");

                    //now show the add.
                    mInterstitialAd.show(MainActivity.this);

                }

                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    // Handle the error
                    String error =
                        String.format(
                            "domain: %s, code: %d, message: %s",
                            loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                    Log.i("InterstitialAd fail", error);
                    mInterstitialAd = null;
                }
            });

    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }
}
