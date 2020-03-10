package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import com.vungle.warren.Vungle;
import com.vungle.warren.AdConfig; // Custom ad configurations
import com.vungle.warren.InitCallback; // Initialization callback
import com.vungle.warren.LoadAdCallback; // Load ad callback
import com.vungle.warren.PlayAdCallback; // Play ad callback
import com.vungle.warren.VungleNativeAd; // MREC ad
import com.vungle.warren.Banners; // Banner ad
import com.vungle.warren.VungleBanner; // Banner ad
import com.vungle.warren.Vungle.Consent; // GDPR consent
import com.vungle.warren.VungleSettings; // Minimum disk space
import com.vungle.warren.error.VungleException; // onError message



public class RnVungleModule extends ReactContextBaseJavaModule {

    

    private final ReactApplicationContext reactContext;

    

    public RnVungleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    VungleSettings vungleSettings = new VungleSettings.Builder()
    .setAndroidIdOptOut(true)
    .build();

    

    private void initVungle(final String appId,final Callback callback) {

        Vungle.init(appId, reactContext.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                callback.invoke("SDK Success");
            }

            @Override
            public void onError(VungleException e) {
                callback.invoke("SDK Error: " + e.getLocalizedMessage());
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
                // Callback to notify when an ad becomes available for the cache optimized
                // placement
                // NOTE: This callback works only for the cache optimized placement. Otherwise,
                // please use
                // LoadAdCallback with loadAd API for loading placements.
            }
        },vungleSettings);
    }

    private void innerLoadAds(final String placementId,final Callback callback) {
        // Load Ad Implementation
        if (Vungle.isInitialized()) {
        Vungle.loadAd(placementId, new LoadAdCallback() {
        @Override
        public void onAdLoad(String placementReferenceId) {
            callback.invoke("Ad loaded");
         }
  
        @Override
        public void onError(String placementReferenceId, VungleException e) {
            callback.invoke("SDK Error: " + e.getLocalizedMessage());
         }
    });
  }
    }

    private void innerShowAds(final String placementId,final String userId,final String appId,final Callback callback) {
        AdConfig adConfig = new AdConfig();
        adConfig.setAdOrientation(AdConfig.AUTO_ROTATE);
        adConfig.setMuted(true);
        adConfig.setBackButtonImmediatelyEnabled(true);
        Vungle.setIncentivizedFields(userId, "RewardedTitle", "RewardedBody", "RewardedKeepWatching", "RewardedClose");
        //adConfig.setIncentivizedUserId(userId);
        if (Vungle.canPlayAd(placementId)) { 
            Vungle.playAd(placementId, adConfig, new PlayAdCallback() { 
              @Override public void onAdStart(String placementReferenceId) { 
                //callback.invoke("Ad started");
              } 
              @Override public void onAdEnd(String placementReferenceId, boolean completed, boolean isCTAClicked) { 
                callback.invoke("Ad ended");
              } 
              @Override public void onError(String placementReferenceId, VungleException e) {
                if (e.getExceptionCode() == VungleException.VUNGLE_NOT_INTIALIZED) {
                    // Re-initialize Vungle SDK
                    initVungle(appId,callback);
                  }
               } 
            });
          }
    }



    @ReactMethod
    public void loadAds(final String placementId,final Callback callback) {
        innerLoadAds(placementId, callback);
    }

    @ReactMethod
    public void showAds(final String placementId,final String userId,final String appId,final Callback callback) {
        
        innerShowAds(placementId,userId,appId,callback);
    }

   

    @ReactMethod
    public void isInitialized(final Callback callback) {
        if (Vungle.isInitialized() == true) {
            callback.invoke("Initialized true");
        } else {
            callback.invoke("Initialized false");
        }

    }

    @ReactMethod
    public void init(final String appid,final Callback callback) {
        initVungle(appid, callback);
    }

    @Override
    public String getName() {
        return "RnVungle";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }
}
