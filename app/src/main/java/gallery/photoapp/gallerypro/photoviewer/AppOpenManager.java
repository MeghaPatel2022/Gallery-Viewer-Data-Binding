package gallery.photoapp.gallerypro.photoviewer;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Date;

import gallery.photoapp.gallerypro.photoviewer.application.MyApplicationClass;

import static androidx.lifecycle.Lifecycle.Event.ON_START;

public class AppOpenManager implements Application.ActivityLifecycleCallbacks {

    private static final String LOG_TAG = "AppOpenManager";
    private static final String AD_UNIT_ID = "ca-app-pub-4293491867572780/3408077952";
    //    private static final String AD_UNIT_ID = "ca-app-pub-3940256099942544/3419835294";
    private static boolean isShowingAd = false;
    private final FirebaseAnalytics mFirebaseAnalytics;
    private final MyApplicationClass myApplication;
    private final AppOpenAd.AppOpenAdLoadCallback loadCallback;
    private final Context mContext;
    private AppOpenAd appOpenAd = null;
    private Activity currentActivity = null;
    private long loadTime = 0;

    /**
     * Constructor
     */
    public AppOpenManager(MyApplicationClass myApplication) {
        this.myApplication = myApplication;
        mContext = myApplication.getApplicationContext();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
        this.myApplication.registerActivityLifecycleCallbacks(this);

        loadCallback =
                new AppOpenAd.AppOpenAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull AppOpenAd appOpenAd) {
                        super.onAdLoaded(appOpenAd);
                        fireAnalyticsAds("admob_openApp", "loaded");
                        AppOpenManager.this.appOpenAd = appOpenAd;
                        AppOpenManager.this.loadTime = (new Date()).getTime();
                        showAdIfAvailable();
                        currentActivity = null;
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        if (loadAdError.getMessage() != null)
                            fireAnalyticsAds("admob_openApp_Error", loadAdError.getMessage());
                        AppOpenManager.this.appOpenAd = null;
                        isShowingAd = false;

                        Intent intent = new Intent(currentActivity, MainActivity.class);
                        currentActivity.startActivity(intent);
                        currentActivity.finish();

                    }

                };

        AdRequest request = getAdRequest();
        fireAnalyticsAds("admob_openApp", "Ad Request send");
        AppOpenAd.load(
                myApplication, AD_UNIT_ID, request,
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT, loadCallback);

    }

    private void fireAnalyticsAds(String arg1, String arg2) {
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, arg1);
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, arg2);
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
        mFirebaseAnalytics.setAnalyticsCollectionEnabled(true);
    }

    /**
     * LifecycleObserver methods
     */
    @OnLifecycleEvent(ON_START)
    public void onStart() {
//        showAdIfAvailable();
        Log.d(LOG_TAG, "onStart");
    }


    /**
     * Creates and returns ad request.
     */
    private AdRequest getAdRequest() {
        return new AdRequest.Builder().build();
    }

    /**
     * Utility method to check if ad was loaded more than n hours ago.
     */
    private boolean wasLoadTimeLessThanNHoursAgo(long numHours) {
        long dateDifference = (new Date()).getTime() - this.loadTime;
        long numMilliSecondsPerHour = 3600000;
        return (dateDifference < (numMilliSecondsPerHour * numHours));
    }

    /**
     * Utility method that checks if ad exists and can be shown.
     */
    public boolean isAdAvailable() {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4);
    }

    /**
     * ActivityLifecycleCallback methods
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        currentActivity = activity;
        showAdIfAvailable();
    }

    @Override
    public void onActivityStarted(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        currentActivity = null;
    }

    /**
     * Shows the ad if one isn't already showing.
     */
    public void showAdIfAvailable() {
        // Only show ad if there is not already an app open ad currently showing
        // and an ad is available.
        if (!isShowingAd && isAdAvailable()) {
            Log.d(LOG_TAG, "Will show ad.");

            FullScreenContentCallback fullScreenContentCallback =
                    new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            // Set the reference to null so isAdAvailable() returns false.
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
//                            fetchAd();
                        }

                        @Override
                        public void onAdFailedToShowFullScreenContent(AdError adError) {
                            AppOpenManager.this.appOpenAd = null;
                            isShowingAd = false;
                            Intent intent = new Intent(currentActivity, MainActivity.class);
                            currentActivity.startActivity(intent);
                            currentActivity.finish();
                        }

                        @Override
                        public void onAdShowedFullScreenContent() {
                            isShowingAd = true;
                        }
                    };

            appOpenAd.show(currentActivity);
            appOpenAd.setFullScreenContentCallback(fullScreenContentCallback);
        }
//        else {
//            Log.d(LOG_TAG, "Can not show ad.");
//            fetchAd();
//        }
    }

}
