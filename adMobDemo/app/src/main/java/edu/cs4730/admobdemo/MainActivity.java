package edu.cs4730.admobdemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;

import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.ResponseInfo;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import edu.cs4730.admobdemo.databinding.ActivityMainBinding;

/**
 * Ads works.   both the banner ad and interstitial ad work again.
 *  had to change the consent manager since the ads one causes duplication class errors.
 *  it' had not be updated since 2019.  Using google example code from https://github.com/googleads/googleads-mobile-android-examples/
 *  which now uses a different consent manager.
 *
 *  a note, I don't seem to be need to give consent even in test mode. so I don't actually know if it works or not.
 *  but its' google's code, so hopefully it works?  idk.
 */


public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private static String TAG = "MainActivity";
    AdRequest adRequest;
    ActivityMainBinding binding;
    //public static final String TEST_DEVICE_HASHED_ID = "ABCDEF012345";
    public static final String TEST_DEVICE_HASHED_ID = "9BCDD15FA3A2C5CDFBB1E0C13599604B";
    private GoogleMobileAdsConsentManager googleMobileAdsConsentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(getApplicationContext());
        googleMobileAdsConsentManager.gatherConsent(this,
            consentError -> {
                if (consentError != null) {
                    // Consent not obtained in current session.
                   logthis(String.format("%s: %s", consentError.getErrorCode(), consentError.getMessage()));
                }
                if (googleMobileAdsConsentManager.canRequestAds()) {
                    // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
                    MobileAds.initialize(this, new OnInitializationCompleteListener() { //19.7.0+ version.
                        @Override
                        public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                            logthis("Initialization is completed. " + initializationStatus.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loadBanner();
                                }
                            });
                        }
                    });
                }

                if (googleMobileAdsConsentManager.isPrivacyOptionsRequired()) {
                    // Regenerate the options menu to include a privacy setting.
                    invalidateOptionsMenu();
                }
            });

        /**
         * now setup and load the interstitial add.  The button will when show the ad, hopefully.
         */
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!googleMobileAdsConsentManager.canRequestAds()) {
                    return;
                }
                /**
                 * Now the interstitialad setup and display if possible.
                 */
                InterstitialAd.load(getApplicationContext(), getResources().getString(R.string.fullscreen_ad_unit_id), adRequest, new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        logthis("interstitial ad loaded.");
                        /**
                         * This add is loaded, so now we can show it.  You likely want to load the ad elsewhere, so it ready to show.
                         * but this is a simple example.
                         */
                        //now we can setup the full screen pieces.
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdClicked() {
                                // Called when a click is recorded for an ad.
                                logthis("interstitial Ad was clicked.");
                                //should dismiss the add here, but I can't figure out how.
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                logthis("interstitial Ad dismissed fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                // Called when ad fails to show.
                                logthis("interstitial Ad failed to show fullscreen content.");
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                logthis("interstitial Ad recorded an impression.");
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                logthis("interstitial Ad showed fullscreen content.");
                            }

                        });

                        //now show the add.
                        mInterstitialAd.show(MainActivity.this);
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        logthis("interstitial failed " + loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });

            }
        });

    }

    /*
     * simple helper function to load the banner ad at the bottom of the screen, once we have consent.
     */
    void loadBanner() {
        //for the ad at the bottom of the mainactivity.

        adRequest = new AdRequest.Builder().build();

        binding.adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                logthis("banner ad has finished loading.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                // Code to be executed when an ad request fails.
                logthis("banner ad has failed to load.");
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
                logthis(" banner ad error " + error.toString());

            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
                logthis("banner ad is displayed.");
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                logthis("banner ad has closed, now do something else.");
            }
        });

        binding.adView.loadAd(adRequest);

    }

    /**
     * Called when leaving the activity
     */
    @Override
    public void onPause() {
        binding.adView.pause();
        super.onPause();
    }

    /**
     * Called when returning to the activity
     */
    @Override
    public void onResume() {
        super.onResume();
        binding.adView.resume();
    }

    /**
     * Called before the activity is destroyed
     */
    @Override
    public void onDestroy() {
        binding.adView.destroy();
        super.onDestroy();
    }

    public void logthis(String item) {
        Log.d(TAG, item);
        binding.logger.append(item + "\n");
    }
}
