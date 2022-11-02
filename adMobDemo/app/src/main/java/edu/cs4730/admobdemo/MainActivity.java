package edu.cs4730.admobdemo;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
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
 * Ads works.  but currently it can't be API31, because underneath there is a pendingintent that is
 * not correct.
 */


public class MainActivity extends AppCompatActivity {
    private InterstitialAd mInterstitialAd;
    private static String TAG = "MainActivity";
    AdView mAdView;
    AdRequest adRequest;
    TextView logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logger = findViewById(R.id.logger);

        //I'm seeing real adds, if I don't add this.  and sometimes I still see real ads.  be careful not to click them.
        ConsentInformation.getInstance(getApplicationContext()).addTestDevice("9BCDD15FA3A2C5CDFBB1E0C13599604B");

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        MobileAds.initialize(this, new OnInitializationCompleteListener() { //19.7.0+ version.
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                logthis("Initialization is completed. " + initializationStatus.toString());
            }
        });

        //for the ad at the bottom of the mainactivity.
        mAdView = findViewById(R.id.adView);
        adRequest = new AdRequest.Builder().build();

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                logthis("banner ad has finished loading.");
            }

            @Override
            public void onAdFailedToLoad(LoadAdError error) {
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

        mAdView.loadAd(adRequest);

        ConsentInformation consentInformation = ConsentInformation.getInstance(this);
        consentInformation.addTestDevice("D1A4B2E34EF63965FDB3E19C432D0D82");

        consentInformation.setDebugGeography(DebugGeography.DEBUG_GEOGRAPHY_EEA);
        String[] publisherIds = {"pub-0123456789012345"};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                // User's consent status successfully updated.
                logthis("consent status successfully updated.");
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
                logthis("consent status failed to update.");
            }
        });

        /**
         * now setup and load the interstitial add.  The button will when show the ad, I think.
         *
         */


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * Now the interstitialad setup and display if possible.
                 */

                InterstitialAd.load(getApplicationContext(), "ca-app-pub-3940256099942544/1033173712"
                    , adRequest,
                    new InterstitialAdLoadCallback() {
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
                                public void onAdFailedToShowFullScreenContent(AdError adError) {
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

    public void logthis(String item) {
        Log.d(TAG, item);
        logger.append(item + "\n");
    }
}
