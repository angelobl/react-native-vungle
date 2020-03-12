package com.reactlibrary;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;

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

    VungleSettings vungleSettings = new VungleSettings.Builder().setAndroidIdOptOut(true).build();

    private void sendEvent(ReactApplicationContext context, String eventName, final WritableMap params) {
        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    private void initVungle(final String appId) {
        final WritableMap params1 = Arguments.createMap();
        final WritableMap params2 = Arguments.createMap();
        Vungle.init(appId, reactContext.getApplicationContext(), new InitCallback() {
            @Override
            public void onSuccess() {
                params1.putBoolean("sdkLoaded", true);
                sendEvent(reactContext, "Event", params1);
            }

            @Override
            public void onError(VungleException e) {
                params2.putBoolean("sdkError", true);
                sendEvent(reactContext, "Event", params1);
            }

            @Override
            public void onAutoCacheAdAvailable(String placementId) {
                // Callback to notify when an ad becomes available for the cache optimized
                // placement
                // NOTE: This callback works only for the cache optimized placement. Otherwise,
                // please use
                // LoadAdCallback with loadAd API for loading placements.
            }
        }, vungleSettings);
    }

    private void innerLoadAds(final String placementId) {
        final WritableMap params1 = Arguments.createMap();
        final WritableMap params2 = Arguments.createMap();
        // Load Ad Implementation
        if (Vungle.isInitialized()) {
            Vungle.loadAd(placementId, new LoadAdCallback() {
                @Override
                public void onAdLoad(String placementReferenceId) {
                    params1.putBoolean("adLoaded", true);
                    sendEvent(reactContext, "Event", params1);
                }

                @Override
                public void onError(String placementReferenceId, VungleException e) {
                    params2.putBoolean("adLoadError", true);
                    sendEvent(reactContext, "Event", params1);
                }
            });
        }
    }

    private void innerShowAds(final String placementId, final String userId, final String appId) {
        final WritableMap params1 = Arguments.createMap();
        final WritableMap params2 = Arguments.createMap();
        final WritableMap params3 = Arguments.createMap();
        AdConfig adConfig = new AdConfig();
        adConfig.setAdOrientation(AdConfig.AUTO_ROTATE);
        adConfig.setMuted(true);
        Vungle.setIncentivizedFields(userId, "RewardedTitle", "RewardedBody", "RewardedKeepWatching", "RewardedClose");

        if (Vungle.canPlayAd(placementId)) {

            Vungle.playAd(placementId, adConfig, new PlayAdCallback() {
                @Override
                public void onAdStart(String placementReferenceId) {
                    params1.putBoolean("adStarted", true);
                    sendEvent(reactContext, "Event", params1);
                }

                @Override
                public void onAdEnd(String placementReferenceId, boolean completed, boolean isCTAClicked) {
                    params2.putBoolean("adEnded", true);
                    sendEvent(reactContext, "Event", params2);
                }

                @Override
                public void onError(String placementReferenceId, VungleException e) {
                    if (e.getExceptionCode() == VungleException.VUNGLE_NOT_INTIALIZED) {
                        // Re-initialize Vungle SDK
                        initVungle(appId);
                    }
                }
            });
        } else {
            params3.putBoolean("showAdsError", true);
            sendEvent(reactContext, "Event", params1);
        }

    }

    @ReactMethod
    public void loadAds(final String placementId) {
        innerLoadAds(placementId);
    }

    @ReactMethod
    public void showAds(final String placementId, final String userId, final String appId) {

        innerShowAds(placementId, userId, appId);

    }

    @ReactMethod
    public void isInitialized(Promise promise) {
        final WritableMap params = Arguments.createMap();
        try {
            if (Vungle.isInitialized() == true) {
                promise.resolve(true);
            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            promise.reject("Error", e);
        }

    }

    @ReactMethod
    public void init(final String appid) {
        initVungle(appid);
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
