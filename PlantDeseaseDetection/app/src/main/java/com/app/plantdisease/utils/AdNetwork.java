package com.app.plantdisease.utils;

import static com.app.plantdisease.utils.Constant.ADMOB;
import static com.app.plantdisease.utils.Constant.AD_STATUS_ON;
import static com.app.plantdisease.utils.Constant.APPLOVIN;
import static com.app.plantdisease.utils.Constant.FAN;
import static com.app.plantdisease.utils.Constant.STARTAPP;
import static com.app.plantdisease.utils.Constant.UNITY;
import static com.app.plantdisease.utils.Constant.UNITY_ADS_BANNER_HEIGHT;
import static com.app.plantdisease.utils.Constant.UNITY_ADS_BANNER_WIDTH;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.app.plantdisease.R;
import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.MaxAdListener;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.sdk.AppLovinSdkUtils;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.startapp.sdk.ads.banner.Banner;
import com.startapp.sdk.ads.banner.BannerListener;
import com.startapp.sdk.ads.nativead.NativeAdDetails;
import com.startapp.sdk.ads.nativead.NativeAdPreferences;
import com.startapp.sdk.ads.nativead.StartAppNativeAd;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.adlisteners.AdEventListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdNetwork {

    private static final String TAG = "AdNetwork";
    private final Activity context;
    SharedPref sharedPref;
    AdsPref adsPref;

    //Banner
    private FrameLayout adContainerView;
    private AdView adView;
    RelativeLayout startAppAdView;
    com.facebook.ads.AdView fanAdView;

    //Interstitial
    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    private StartAppAd startAppAd;
    private MaxInterstitialAd maxInterstitialAd;
    private int retryAttempt;
    private int counter = 1;

    //Native
    MediaView mediaView;
    TemplateView admob_native_ad;
    LinearLayout admob_native_background;
    private NativeAd nativeAd;
    private NativeAdLayout fan_native_ad;
    private LinearLayout nativeAdView;
    View startapp_native_ad;
    ImageView startapp_native_image;
    TextView startapp_native_title;
    TextView startapp_native_description;
    Button startapp_native_button;
    LinearLayout startapp_native_background;

    public AdNetwork(Activity context) {
        this.context = context;
        this.sharedPref = new SharedPref(context);
        this.adsPref = new AdsPref(context);
    }

    public void loadBannerAdNetwork(int ad_placement) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && ad_placement != 0) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    adContainerView = context.findViewById(R.id.admob_banner_view_container);
                    adContainerView.post(() -> {
                        adView = new AdView(context);
                        adView.setAdUnitId(adsPref.getAdMobBannerId());
                        adContainerView.removeAllViews();
                        adContainerView.addView(adView);
                        adView.setAdSize(Tools.getAdSize(context));
                        adView.loadAd(Tools.getAdRequest(context));
                        adView.setAdListener(new AdListener() {
                            @Override
                            public void onAdLoaded() {
                                // Code to be executed when an ad finishes loading.
                                adContainerView.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                // Code to be executed when an ad request fails.
                                adContainerView.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                            }

                            @Override
                            public void onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                            }

                            @Override
                            public void onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                            }
                        });
                    });
                    break;
                case FAN:
                    fanAdView = new com.facebook.ads.AdView(context, adsPref.getFanBannerUnitId(), AdSize.BANNER_HEIGHT_50);
                    LinearLayout adContainer = context.findViewById(R.id.fan_banner_view_container);
                    // Add the ad view to your activity layout
                    adContainer.addView(fanAdView);
                    com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
                        @Override
                        public void onError(Ad ad, AdError adError) {
                            adContainer.setVisibility(View.GONE);
                            Log.d(TAG, "Failed to load Audience Network : " + adError.getErrorMessage() + " "  + adError.getErrorCode());
                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            adContainer.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    };
                    com.facebook.ads.AdView.AdViewLoadConfig loadAdConfig = fanAdView.buildLoadAdConfig().withAdListener(adListener).build();
                    fanAdView.loadAd(loadAdConfig);
                    break;
                case STARTAPP:
                    startAppAdView = context.findViewById(R.id.startapp_banner_view_container);
                    Banner banner = new Banner(context, new BannerListener() {
                        @Override
                        public void onReceiveAd(View banner) {
                            startAppAdView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onFailedToReceiveAd(View banner) {
                            startAppAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onImpression(View view) {

                        }

                        @Override
                        public void onClick(View banner) {
                        }
                    });
                    startAppAdView.addView(banner);
                    break;
                case UNITY:
                    RelativeLayout unityAdView = context.findViewById(R.id.unity_banner_view_container);
                    BannerView bottomBanner = new BannerView(context, adsPref.getUnityBannerPlacementId(), new UnityBannerSize(UNITY_ADS_BANNER_WIDTH, UNITY_ADS_BANNER_HEIGHT));
                    bottomBanner.setListener(new BannerView.IListener() {
                        @Override
                        public void onBannerLoaded(BannerView bannerView) {
                            unityAdView.setVisibility(View.VISIBLE);
                            Log.d("Unity_banner", "ready");
                        }

                        @Override
                        public void onBannerClick(BannerView bannerView) {

                        }

                        @Override
                        public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo bannerErrorInfo) {
                            Log.d("SupportTest", "Banner Error" + bannerErrorInfo);
                            unityAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onBannerLeftApplication(BannerView bannerView) {

                        }
                    });
                    unityAdView.addView(bottomBanner);
                    bottomBanner.load();
                    break;
                case APPLOVIN:
                    RelativeLayout appLovinAdView = context.findViewById(R.id.applovin_banner_view_container);
                    MaxAdView maxAdView = new MaxAdView(adsPref.getAppLovinBannerAdUnitId(), context);
                    maxAdView.setListener(new MaxAdViewAdListener() {
                        @Override
                        public void onAdExpanded(MaxAd ad) {

                        }

                        @Override
                        public void onAdCollapsed(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            appLovinAdView.setVisibility(View.VISIBLE);
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {

                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {

                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            appLovinAdView.setVisibility(View.GONE);
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                        }
                    });

                    int width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int heightPx = context.getResources().getDimensionPixelSize(R.dimen.applovin_banner_height);
                    maxAdView.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
                    if (sharedPref.getIsDarkTheme()) {
                        maxAdView.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundDark));
                    } else {
                        maxAdView.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundLight));
                    }
                    appLovinAdView.addView(maxAdView);
                    maxAdView.loadAd();
                    break;
            }
        }
    }

    public void loadInterstitialAdNetwork(int ad_placement) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && ad_placement != 0) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    InterstitialAd.load(context, adsPref.getAdMobInterstitialId(), Tools.getAdRequest(context), new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            adMobInterstitialAd = interstitialAd;
                            adMobInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                                @Override
                                public void onAdDismissedFullScreenContent() {
                                    loadInterstitialAdNetwork(ad_placement);
                                }

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull com.google.android.gms.ads.AdError adError) {
                                    Log.d(TAG, "The ad failed to show.");
                                }

                                @Override
                                public void onAdShowedFullScreenContent() {
                                    adMobInterstitialAd = null;
                                    Log.d(TAG, "The ad was shown.");
                                }
                            });
                            Log.i(TAG, "onAdLoaded");
                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            Log.i(TAG, loadAdError.getMessage());
                            adMobInterstitialAd = null;
                            Log.d(TAG, "Failed load AdMob Interstitial Ad");
                        }
                    });

                    break;
                case FAN:
                    fanInterstitialAd = new com.facebook.ads.InterstitialAd(context, adsPref.getFanInterstitialUnitId());
                    InterstitialAdListener adListener = new InterstitialAdListener() {
                        @Override
                        public void onError(Ad ad, AdError adError) {

                        }

                        @Override
                        public void onAdLoaded(Ad ad) {
                            Log.d(TAG, "FAN Interstitial Ad loaded...");
                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {
                            fanInterstitialAd.loadAd();
                        }
                    };

                    com.facebook.ads.InterstitialAd.InterstitialLoadAdConfig loadAdConfig = fanInterstitialAd.buildLoadAdConfig().withAdListener(adListener).build();
                    fanInterstitialAd.loadAd(loadAdConfig);

                    break;
                case STARTAPP:
                    startAppAd = new StartAppAd(context);

                    break;
                case APPLOVIN:
                    maxInterstitialAd = new MaxInterstitialAd(adsPref.getAppLovinInterstitialAdUnitId(), context);
                    maxInterstitialAd.setListener(new MaxAdListener() {
                        @Override
                        public void onAdLoaded(MaxAd ad) {
                            retryAttempt = 0;
                            Log.d(TAG, "AppLovin Interstitial Ad loaded...");
                        }

                        @Override
                        public void onAdDisplayed(MaxAd ad) {
                        }

                        @Override
                        public void onAdHidden(MaxAd ad) {
                            maxInterstitialAd.loadAd();
                        }

                        @Override
                        public void onAdClicked(MaxAd ad) {

                        }

                        @Override
                        public void onAdLoadFailed(String adUnitId, MaxError error) {
                            retryAttempt++;
                            long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));
                            new Handler().postDelayed(() -> maxInterstitialAd.loadAd(), delayMillis);
                            Log.d(TAG, "failed to load AppLovin Interstitial");
                        }

                        @Override
                        public void onAdDisplayFailed(MaxAd ad, MaxError error) {
                            maxInterstitialAd.loadAd();
                        }
                    });

                    // Load the first ad
                    maxInterstitialAd.loadAd();
                    break;
            }
        }
    }

    public void showInterstitialAdNetwork(int ad_placement, int interval) {
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && ad_placement != 0) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    if (adMobInterstitialAd != null) {
                        if (counter == interval) {
                            adMobInterstitialAd.show(context);
                            counter = 1;
                        } else {
                            counter++;
                        }
                    }
                    break;
                case FAN:
                    if (fanInterstitialAd != null && fanInterstitialAd.isAdLoaded()) {
                        if (counter == interval) {
                            fanInterstitialAd.show();
                            counter = 1;
                        } else {
                            counter++;
                        }
                    }

                    break;
                case STARTAPP:
                    if (counter == interval) {
                        startAppAd.showAd();
                        counter = 1;
                    } else {
                        counter++;
                    }
                    break;
                case UNITY:
                    if (UnityAds.isReady(adsPref.getUnityInterstitialPlacementId())) {
                        if (counter == interval) {
                            UnityAds.show(context, adsPref.getUnityInterstitialPlacementId(), new IUnityAdsShowListener() {
                                @Override
                                public void onUnityAdsShowFailure(String s, UnityAds.UnityAdsShowError unityAdsShowError, String s1) {

                                }

                                @Override
                                public void onUnityAdsShowStart(String s) {

                                }

                                @Override
                                public void onUnityAdsShowClick(String s) {

                                }

                                @Override
                                public void onUnityAdsShowComplete(String s, UnityAds.UnityAdsShowCompletionState unityAdsShowCompletionState) {

                                }
                            });
                            counter = 1;
                        } else {
                            counter++;
                        }
                    }
                    break;
                case APPLOVIN:
                    Log.d(TAG, "selected");
                    if (maxInterstitialAd.isReady()) {
                        Log.d(TAG, "ready : " + counter);
                        if (counter == interval) {
                            maxInterstitialAd.showAd();
                            counter = 1;
                            Log.d(TAG, "show ad");
                        } else {
                            counter++;
                        }
                    }
                    break;
            }
        }
    }

    public void loadNativeAdNetwork(int ad_placement) {

        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && ad_placement != 0) {

            admob_native_ad = context.findViewById(R.id.admob_native_ad_container);
            mediaView = context.findViewById(R.id.media_view);
            admob_native_background = context.findViewById(R.id.background);
            fan_native_ad = context.findViewById(R.id.fan_native_ad_container);
            startapp_native_ad = context.findViewById(R.id.startapp_native_ad_container);
            startapp_native_image = context.findViewById(R.id.startapp_native_image);
            startapp_native_title = context.findViewById(R.id.startapp_native_title);
            startapp_native_description = context.findViewById(R.id.startapp_native_description);
            startapp_native_button = context.findViewById(R.id.startapp_native_button);
            startapp_native_button.setOnClickListener(v1 -> startapp_native_ad.performClick());
            startapp_native_background = context.findViewById(R.id.startapp_native_background);
            View applovin_mrec_ad = context.findViewById(R.id.applovin_mrec_ad_container);
            RelativeLayout applovin_mrec_ad_view = context.findViewById(R.id.applovin_mrec_ad_view);

            switch (adsPref.getAdType()) {
                case ADMOB:
                    if (admob_native_ad.getVisibility() != View.VISIBLE) {
                        AdLoader adLoader = new AdLoader.Builder(context, adsPref.getAdMobNativeId())
                                .forNativeAd(NativeAd -> {
                                    if (sharedPref.getIsDarkTheme()) {
                                        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundDark));
                                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                                        admob_native_ad.setStyles(styles);
                                        admob_native_background.setBackgroundResource(R.color.colorBackgroundDark);
                                    } else {
                                        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.colorBackgroundLight));
                                        NativeTemplateStyle styles = new NativeTemplateStyle.Builder().withMainBackgroundColor(colorDrawable).build();
                                        admob_native_ad.setStyles(styles);
                                        admob_native_background.setBackgroundResource(R.color.colorBackgroundLight);
                                    }
                                    mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
                                    admob_native_ad.setNativeAd(NativeAd);
                                    admob_native_ad.setVisibility(View.VISIBLE);
                                })
                                .withAdListener(new AdListener() {
                                    @Override
                                    public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                                        admob_native_ad.setVisibility(View.GONE);
                                    }
                                })
                                .build();
                        adLoader.loadAd(Tools.getAdRequest(context));
                    } else {
                        Log.d("NATIVE_AD", "AdMob native ads has been loaded");
                    }
                    break;
                case FAN:
                    if (fan_native_ad.getVisibility() != View.VISIBLE) {
                        nativeAd = new NativeAd(context, adsPref.getFanNativeUnitId());
                        NativeAdListener nativeAdListener = new NativeAdListener() {
                            @Override
                            public void onMediaDownloaded(Ad ad) {

                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {

                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                // Race condition, load() called again before last ad was displayed
                                fan_native_ad.setVisibility(View.VISIBLE);
                                if (nativeAd == null || nativeAd != ad) {
                                    return;
                                }
                                // Inflate Native Ad into Container
                                //inflateAd(nativeAd);
                                nativeAd.unregisterView();
                                // Add the Ad view into the ad container.
                                LayoutInflater inflater = LayoutInflater.from(context);
                                // Inflate the Ad view.  The layout referenced should be the one you created in the last step.

                                nativeAdView = (LinearLayout) inflater.inflate(R.layout.gnt_fan_medium_template, fan_native_ad, false);

                                fan_native_ad.addView(nativeAdView);

                                // Add the AdOptionsView
                                LinearLayout adChoicesContainer = nativeAdView.findViewById(R.id.ad_choices_container);
                                AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, fan_native_ad);
                                adChoicesContainer.removeAllViews();
                                adChoicesContainer.addView(adOptionsView, 0);

                                // Create native UI using the ad metadata.
                                TextView nativeAdTitle = nativeAdView.findViewById(R.id.native_ad_title);
                                com.facebook.ads.MediaView nativeAdMedia = nativeAdView.findViewById(R.id.native_ad_media);
                                TextView nativeAdSocialContext = nativeAdView.findViewById(R.id.native_ad_social_context);
                                TextView nativeAdBody = nativeAdView.findViewById(R.id.native_ad_body);
                                TextView sponsoredLabel = nativeAdView.findViewById(R.id.native_ad_sponsored_label);
                                Button nativeAdCallToAction = nativeAdView.findViewById(R.id.native_ad_call_to_action);

                                LinearLayout fan_native_background = nativeAdView.findViewById(R.id.fan_unit);
                                if (sharedPref.getIsDarkTheme()) {
                                    fan_native_background.setBackgroundResource(R.color.colorBackgroundDark);
                                } else {
                                    fan_native_background.setBackgroundResource(R.color.colorBackgroundLight);
                                }

                                // Set the Text.
                                nativeAdTitle.setText(nativeAd.getAdvertiserName());
                                nativeAdBody.setText(nativeAd.getAdBodyText());
                                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                                nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
                                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
                                sponsoredLabel.setText(nativeAd.getSponsoredTranslation());

                                // Create a list of clickable views
                                List<View> clickableViews = new ArrayList<>();
                                clickableViews.add(nativeAdTitle);
                                clickableViews.add(nativeAdCallToAction);
                                clickableViews.add(nativeAdMedia);

                                // Register the Title and CTA button to listen for clicks.
                                nativeAd.registerViewForInteraction(nativeAdView, nativeAdMedia, clickableViews);

                            }

                            @Override
                            public void onAdClicked(Ad ad) {

                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {

                            }
                        };

                        NativeAd.NativeLoadAdConfig loadAdConfig = nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build();
                        nativeAd.loadAd(loadAdConfig);
                    } else {
                        Log.d("NATIVE_AD", "FAN native ads has been loaded");
                    }
                    break;
                case STARTAPP:
                    if (startapp_native_ad.getVisibility() != View.VISIBLE) {
                        StartAppNativeAd startAppNativeAd = new StartAppNativeAd(context);
                        NativeAdPreferences nativePrefs = new NativeAdPreferences()
                                .setAdsNumber(3)
                                .setAutoBitmapDownload(true)
                                .setPrimaryImageSize(Constant.STARTAPP_IMAGE_MEDIUM);
                        AdEventListener adListener = new AdEventListener() {
                            @Override
                            public void onReceiveAd(com.startapp.sdk.adsbase.Ad arg0) {
                                Log.d("STARTAPP_ADS", "ad loaded");
                                startapp_native_ad.setVisibility(View.VISIBLE);
                                //noinspection rawtypes
                                ArrayList ads = startAppNativeAd.getNativeAds(); // get NativeAds list

                                // Print all ads details to log
                                for (Object ad : ads) {
                                    Log.d("STARTAPP_ADS", ad.toString());
                                }

                                NativeAdDetails ad = (NativeAdDetails) ads.get(0);
                                if (ad != null) {
                                    startapp_native_image.setImageBitmap(ad.getImageBitmap());
                                    startapp_native_title.setText(ad.getTitle());
                                    startapp_native_description.setText(ad.getDescription());
                                    startapp_native_button.setText(ad.isApp() ? "Install" : "Open");
                                    ad.registerViewForInteraction(startapp_native_ad);
                                }

                                if (sharedPref.getIsDarkTheme()) {
                                    startapp_native_background.setBackgroundResource(R.color.colorBackgroundDark);
                                } else {
                                    startapp_native_background.setBackgroundResource(R.color.colorBackgroundLight);
                                }

                            }

                            @Override
                            public void onFailedToReceiveAd(com.startapp.sdk.adsbase.Ad arg0) {
                                startapp_native_ad.setVisibility(View.GONE);
                                Log.d("STARTAPP_ADS", "ad failed");
                            }
                        };
                        startAppNativeAd.loadAd(nativePrefs, adListener);
                    } else {
                        Log.d("NATIVE_AD", "StartApp native ads has been loaded");
                    }
                    break;

                case APPLOVIN:
                    if (!adsPref.getAppLovinMrecAdUnitId().equals("0")) {
                        MaxAdView maxAdView = new MaxAdView(adsPref.getAppLovinMrecAdUnitId(), MaxAdFormat.MREC, context);
                        maxAdView.setListener(new MaxAdViewAdListener() {
                            @Override
                            public void onAdExpanded(MaxAd ad) {

                            }

                            @Override
                            public void onAdCollapsed(MaxAd ad) {

                            }

                            @Override
                            public void onAdLoaded(MaxAd ad) {
                                applovin_mrec_ad.setVisibility(View.VISIBLE);
                                Log.d("AppLovin MREC", "MREC Ad loaded successfully");
                            }

                            @Override
                            public void onAdDisplayed(MaxAd ad) {

                            }

                            @Override
                            public void onAdHidden(MaxAd ad) {

                            }

                            @Override
                            public void onAdClicked(MaxAd ad) {

                            }

                            @Override
                            public void onAdLoadFailed(String adUnitId, MaxError error) {
                                applovin_mrec_ad.setVisibility(View.GONE);
                                Log.d("AppLovin MREC", "failed to load MREC Ad");
                            }

                            @Override
                            public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                            }
                        });

                        // MREC width and height are 300 and 250 respectively, on phones and tablets
                        int widthPx = AppLovinSdkUtils.dpToPx(context, 300);
                        int heightPx = AppLovinSdkUtils.dpToPx(context, 250);

                        maxAdView.setLayoutParams(new FrameLayout.LayoutParams(widthPx, heightPx));

                        // Set background or background color for MRECs to be fully functional
                        if (sharedPref.getIsDarkTheme()) {
                            maxAdView.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundDark));
                        } else {
                            maxAdView.setBackgroundColor(context.getResources().getColor(R.color.colorBackgroundLight));
                        }
                        applovin_mrec_ad_view.addView(maxAdView);

                        // Load the ad
                        maxAdView.loadAd();
                    }
                    break;
            }

        }

    }

}
