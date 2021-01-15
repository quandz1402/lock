package com.lockscreenamongus.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdBase;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdView;
import com.google.android.ads.nativetemplates.NativeTemplateStyle;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.lockscreenamongus.MainActivity;
import com.lockscreenamongus.R;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.io.IOException;

public class InterAds {

    private Activity activity;
    private Activity activityDiaLog;
    private InterstitialAd mInterstitialAd;
    private com.facebook.ads.InterstitialAd interstitialAd;
    private AdsInterShow adsInterShow = AdsInterShow.INTER;
    private int countInterShow = 0;
    private AdView adViewBanner;
    private NativeAd nativeAd;
    private UnifiedNativeAd unifiedNativeAd;
    private com.google.android.gms.ads.AdView mAdView;
    private boolean bannerFb = true;
    private boolean nativeFb = true;
    private boolean isLimitNativeAdsGG = false;
    private boolean isLimitBannerAdsGG = false;
    private AdsListener adsListener;
    private static final String appUnityId = "3949993";
    private static String interstitialAdPlacement = "Inter";

    public enum AdsInterShow {
        INTER,
        NATIVE,
        BANNER,
        SHOWING
    }

    public InterAds(Activity activity) {
        this.activity = activity;
    }

    public void initAds(){
        initInterFB();
        loadNativeAd();
        loadBannerFb();

        final UnityAdsListener myAdsListener = new UnityAdsListener ();
        // Add the listener to the SDK:
        UnityAds.addListener(myAdsListener);
        // Initialize the SDK:
        UnityAds.initialize (activity, appUnityId);
    }

    public void showInter(@NonNull AdsListener adsListenerShow, Activity activityDiaLog) {

        InterAds.this.adsListener = adsListenerShow;
        InterAds.this.activityDiaLog = activityDiaLog;

        if(countInterShow <= MainActivity.NUMBER_OF_INTER){

            switch (adsInterShow){
                case INTER:
                    if (interstitialAd != null && interstitialAd.isAdLoaded()) {
                        interstitialAd.show();
                    } else {
                        if(isOnline()){
                            if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
                                mInterstitialAd.show();
                            } else {
                                displayInterstitialAdUnity();
                            }
                        } else {
                            adsListener.onAdsClose();
                            Toast.makeText(activity, "Please check network connection!", Toast.LENGTH_SHORT).show();
                        }
                    }
                    adsInterShow = AdsInterShow.NATIVE;
                    break;
                case NATIVE:
                    if(!isLimitNativeAdsGG){
                        showDialogNative();
                    } else {
                        displayInterstitialAdUnity();
                    }
                    adsInterShow = AdsInterShow.BANNER;
                    break;

                case BANNER:
                    if(!isLimitBannerAdsGG){
                        showDialogBanner();
                    } else {
                        displayInterstitialAdUnity();
                    }
                    adsInterShow = AdsInterShow.INTER;
                    break;
            }

        }

    }

    private void displayInterstitialAdUnity () {
        if (UnityAds.isReady (interstitialAdPlacement)) {
            UnityAds.show (activityDiaLog, interstitialAdPlacement);
        } else {
            adsListener.onAdsClose();
        }
    }

    public void onDestroy(){
        if (interstitialAd != null) {
            interstitialAd.destroy();
        }
        if (adViewBanner != null) {
            adViewBanner.destroy();
        }
        if(nativeAd != null){
            nativeAd.destroy();
        }
    }

    private class UnityAdsListener implements IUnityAdsListener {

        @Override
        public void onUnityAdsReady (String placementId) {
            // Implement functionality for an ad being ready to show.
        }

        @Override
        public void onUnityAdsStart (String placementId) {
            // Implement functionality for a user starting to watch an ad.
        }

        @Override
        public void onUnityAdsFinish (String placementId, UnityAds.FinishState finishState) {
            // Implement functionality for a user finishing an ad.
            adsListener.onAdsClose();
        }

        @Override
        public void onUnityAdsError (UnityAds.UnityAdsError error, String message) {
            // Implement functionality for a Unity Ads service error occurring.
        }
    }


    private void showDialogNative(){

        adsInterShow = AdsInterShow.SHOWING;
        final Dialog dialog = new Dialog(activityDiaLog, R.style.FullscreenDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if(nativeFb){
            dialog.setContentView(R.layout.ads_custom_dialog);
            RelativeLayout nativeAdContainer = (RelativeLayout) dialog.findViewById(R.id.native_ad_container);
            nativeAdContainer.removeAllViews();
            if(nativeAd != null && nativeAd.isAdLoaded()){
                View adView = NativeAdView.render(activityDiaLog, nativeAd);
                nativeAdContainer.addView(adView, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 800));
            }

        } else {
            dialog.setContentView(R.layout.ads_custom_dialog_admob);
            ImageView imgAd = (ImageView) dialog.findViewById(R.id.imgAd);

            NativeTemplateStyle styles = new
                    NativeTemplateStyle.Builder().withMainBackgroundColor(new ColorDrawable(0xFFFFFFFF)).build();

            TemplateView template = dialog.findViewById(R.id.my_template);
//      template.setStyles(styles);
            template.setNativeAd(unifiedNativeAd);
            imgAd.setVisibility(View.GONE);

        }

        final TextView btnCancelNative = dialog.findViewById(R.id.btnCancel);
        btnCancelNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nativeAd.destroy();
                dialog.dismiss();
                adsListener.onAdsClose();
//        adsShowing = false;
                adsInterShow = AdsInterShow.BANNER;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nativeFb = true;
                        loadNativeAd();
                    }
                }, MainActivity.TIME_REQUEST_ADS);
            }
        });

        btnCancelNative.setActivated(false);
        btnCancelNative.setClickable(false);

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                btnCancelNative.setText("Cancel (" + millisUntilFinished / 1000 + ")");
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                btnCancelNative.setActivated(true);
                btnCancelNative.setText("Cancel");
                btnCancelNative.setClickable(true);
                btnCancelNative.setBackground(ContextCompat.getDrawable(activity, R.drawable.cancel_border));
                btnCancelNative.setTextColor(Color.parseColor("#000000"));

            }
        }.start();

        dialog.show();

    }

    private void showDialogBanner(){
        adsInterShow = AdsInterShow.SHOWING;
        final Dialog dialog = new Dialog(activityDiaLog, R.style.FullscreenDialogTheme);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.ads_custom_dialog_banner);

        RelativeLayout adContainer = (RelativeLayout) dialog.findViewById(R.id.banner_container);
        adContainer.removeAllViews();

        // Add the ad view to your activity layout
        if(bannerFb){
            adContainer.addView(adViewBanner);
        } else {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            ((RelativeLayout)adContainer).addView(mAdView, params);
        }

        final TextView btnCancelNative = dialog.findViewById(R.id.btnCancel);
        btnCancelNative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adViewBanner.destroy();
                adViewBanner = null;
                dialog.dismiss();
                adsListener.onAdsClose();
                adsInterShow = AdsInterShow.INTER;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        bannerFb = true;
                        loadBannerFb();
                    }
                }, MainActivity.TIME_REQUEST_ADS);
            }
        });

        btnCancelNative.setActivated(false);
        btnCancelNative.setClickable(false);

        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                btnCancelNative.setText("Cancel (" + millisUntilFinished / 1000 + ")");
                //here you can have your logic to set text to edittext
            }

            public void onFinish() {
                btnCancelNative.setActivated(true);
                btnCancelNative.setText("Cancel");
                btnCancelNative.setClickable(true);
                btnCancelNative.setBackground(ContextCompat.getDrawable(activity, R.drawable.cancel_border));
                btnCancelNative.setTextColor(Color.parseColor("#000000"));

            }
        }.start();

        dialog.show();

    }

    private void initInterFB(){
        interstitialAd = new com.facebook.ads.InterstitialAd(activity, MainActivity.INTER_ID_FB);
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial ad displayed callback
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
                countInterShow++;
                adsInterShow = AdsInterShow.NATIVE;
                adsListener.onAdsClose();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadAdsFB();
                    }
                }, MainActivity.TIME_REQUEST_ADS);
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                loadInterAmdob();
            }

            @Override
            public void onAdLoaded(Ad ad) {

                // Interstitial ad is loaded and ready to be displayed
                // Show the ad
//                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };


        interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());
    }

    private void loadAdsFB(){
        if ( !interstitialAd.isAdLoaded()) {
            interstitialAd.loadAd();
        }
    }

    private void loadInterAmdob(){
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId(MainActivity.INTER_ID);
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
            }

            @Override
            public void onAdClosed() {
                countInterShow++;
                adsInterShow = AdsInterShow.NATIVE;
                adsListener.onAdsClose();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadInterAmdob();
                    }
                }, MainActivity.TIME_REQUEST_ADS);
            }


        });
    }


    private void loadNativeAd() {
        nativeAd = new NativeAd(activity, MainActivity.NATIVE_ID_FB_LARGER);
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {

            }

            @Override
            public void onError(Ad ad, AdError adError) {
                nativeFb = false;
                loadNativeAdmob();
            }

            @Override
            public void onAdLoaded(Ad ad) {
//        MainActivity.this.ad = ad;
                nativeFb = true;
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }

                nativeAd.downloadMedia();
            }

            @Override
            public void onAdClicked(Ad ad) {

            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .withMediaCacheFlag(NativeAdBase.MediaCacheFlag.ALL)
                        .build());
    }

    private void loadNativeAdmob(){
        AdLoader adLoader = new AdLoader.Builder(activity, MainActivity.NATIVE_AD_ID)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        InterAds.this.unifiedNativeAd = unifiedNativeAd;
                        isLimitNativeAdsGG = false;
                    }
                })
                .withAdListener(new AdListener() {
                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        isLimitNativeAdsGG = true;
                    }

                    @Override
                    public void onAdClosed() {
                        super.onAdClosed();
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());
    }

    private void loadBannerFb(){
        if (adViewBanner != null) {
            adViewBanner.destroy();
            adViewBanner = null;
        }
        adViewBanner = new AdView(activity, MainActivity.BANNER_ID_FB_LARGER, AdSize.RECTANGLE_HEIGHT_250);
        com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                bannerFb = false;
                loadBannerAdmob();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback

                bannerFb = true;
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };

        // Request an ad
        adViewBanner.loadAd(adViewBanner.buildLoadAdConfig().withAdListener(adListener).build());
    }

    private void loadBannerAdmob (){
        mAdView = new com.google.android.gms.ads.AdView(activity);
        mAdView.setAdSize(com.google.android.gms.ads.AdSize.MEDIUM_RECTANGLE);
        mAdView.setAdUnitId(MainActivity.BANNER_ID);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                isLimitBannerAdsGG = false;
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
                isLimitBannerAdsGG = true;
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
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });
        mAdView.loadAd(adRequest);
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }
}
