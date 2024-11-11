package edu.cs4730.admobdemo_kt

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
//import com.google.ads.consent.ConsentInfoUpdateListener
//import com.google.ads.consent.ConsentInformation
//import com.google.ads.consent.ConsentStatus
//import com.google.ads.consent.DebugGeography
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import edu.cs4730.admobdemo_kt.databinding.ActivityMainBinding

/**
 * Ads works.   both the banner ad and interstitial ad work again.
 *
 * had to change the consent manager since the ads one causes duplication class errors.
 * it' had not be updated since 2019.  Using google example code from https://github.com/googleads/googleads-mobile-android-examples/
 * which now uses a different consent manager.
 *
 * a note, I don't seem to be need to give consent even in test mode. so I don't actually know if it works or not.
 * but its' google's code, so hopefully it works?  idk.
 */
class MainActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
    private var TAG: String = "MainActivity"
    private lateinit var adRequest: AdRequest
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    companion object {
        //const val TEST_DEVICE_HASHED_ID = "ABCDEF012345"
        const val TEST_DEVICE_HASHED_ID = "9BCDD15FA3A2C5CDFBB1E0C13599604B"  //pixel4a I think.
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(binding.main.id)
        ) { v: View, insets: WindowInsetsCompat ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            WindowInsetsCompat.CONSUMED
        }

        googleMobileAdsConsentManager = GoogleMobileAdsConsentManager.getInstance(this)
        googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
            if (consentError != null) {
                // Consent not obtained in current session.
                logthis("${consentError.errorCode}: ${consentError.message}")
            }

            if (googleMobileAdsConsentManager.canRequestAds) {
                // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
                MobileAds.initialize(
                    this
                ) { initializationStatus ->
                    //19.7.0+ version.
                    logthis("Initialization is completed. $initializationStatus")
                    runOnUiThread {
                        loadBanner()
                    }
                }
            }
            if (googleMobileAdsConsentManager.isPrivacyOptionsRequired) {
                // Regenerate the options menu to include a privacy setting.
                invalidateOptionsMenu()
            }
        }


        /**
         * now setup and load the interstitial add.  The button will when show the ad, hopefully.
         */
        binding.button.setOnClickListener {

            if (!googleMobileAdsConsentManager.canRequestAds) {
                return@setOnClickListener
            }

            /**
             * Now the interstitialad setup and display if possible.
             */
            InterstitialAd.load(applicationContext,
                resources.getString(R.string.fullscreen_ad_unit_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd
                        logthis("interstitial ad loaded.")
                        /**
                         * This add is loaded, so now we can show it.  You likely want to load the ad elsewhere, so it ready to show.
                         * but this is a simple example.
                         */
                        //now we can setup the full screen pieces.
                        mInterstitialAd!!.fullScreenContentCallback = object :
                            FullScreenContentCallback() {
                            override fun onAdClicked() {
                                // Called when a click is recorded for an ad.
                                logthis("interstitial Ad was clicked.")
                                //should dismiss the add here, but I can't figure out how.
                            }

                            override fun onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                logthis("interstitial Ad dismissed fullscreen content.")
                                mInterstitialAd = null
                            }

                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                // Called when ad fails to show.
                                logthis("interstitial Ad failed to show fullscreen content.")
                                mInterstitialAd = null
                            }

                            override fun onAdImpression() {
                                // Called when an impression is recorded for an ad.
                                logthis("interstitial Ad recorded an impression.")
                            }

                            override fun onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                logthis("interstitial Ad showed fullscreen content.")
                            }
                        }

                        //now show the add.
                        mInterstitialAd!!.show(this@MainActivity)
                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        // Handle the error
                        logthis("interstitial failed $loadAdError")
                        mInterstitialAd = null
                    }
                }
            )
        }
    }

    /*
     * simple helper function to load the banner ad at the bottom of the screen, once we have consent.
     */
    private fun loadBanner() {
        //for the ad at the bottom of the mainactivity.
        adRequest = AdRequest.Builder().build()

        binding.adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                logthis("banner ad has finished loading.")
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                // Code to be executed when an ad request fails.
                logthis("banner ad has failed to load.")
                // Gets the domain from which the error came.
                val errorDomain = error.domain
                // Gets the error code. See
                // https://developers.google.com/android/reference/com/google/android/gms/ads/AdRequest#constant-summary
                // for a list of possible codes.
                val errorCode = error.code
                // Gets an error message.
                // For example "Account not approved yet". See
                // https://support.google.com/admob/answer/9905175 for explanations of
                // common errors.
                val errorMessage = error.message
                // Gets additional response information about the request. See
                // https://developers.google.com/admob/android/response-info for more
                // information.
                val responseInfo = error.responseInfo
                // Gets the cause of the error, if available.
                val cause = error.cause
                // All of this information is available via the error's toString() method.
                logthis(" banner ad error $error")
            }

            override fun onAdOpened() {
                // Code to be executed when the ad is displayed.
                logthis("banner ad is displayed.")
            }

            override fun onAdClosed() {
                // Code to be executed when when the interstitial ad is closed.
                logthis("banner ad has closed, now do something else.")
            }
        }

        binding.adView.loadAd(adRequest)

    }

    /**
     * Called when leaving the activity
     */
    public override fun onPause() {
        binding.adView.pause()
        super.onPause()
    }

    /**
     * Called when returning to the activity
     */
    public override fun onResume() {
        super.onResume()
        binding.adView.resume()
    }

    /**
     * Called before the activity is destroyed
     */
    public override fun onDestroy() {
        binding.adView.destroy()
        super.onDestroy()
    }

    fun logthis(item: String) {
        Log.d(TAG, item)
        binding.logger.append(item + "\n")
    }
}