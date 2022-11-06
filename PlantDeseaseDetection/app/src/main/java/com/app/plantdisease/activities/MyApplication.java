package com.app.plantdisease.activities;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.multidex.MultiDex;

import com.app.plantdisease.callbacks.CallbackAds;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.Ads;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.AdsPref;
import com.facebook.ads.AudienceNetworkAds;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;
import com.onesignal.OneSignal;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";
    public String prefName = "news_pref";
    public static MyApplication mInstance;
    FirebaseAnalytics mFirebaseAnalytics;
    public SharedPreferences preferences;
    private AppOpenAdManager appOpenAdManager;
    AdsPref adsPref;
    String message = "";
    String big_picture = "";
    String title = "";
    String link = "";
    long post_id = -1;
    long unique_id = -1;
    Ads ads;

    public MyApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        adsPref = new AdsPref(this);
        MobileAds.initialize(this, initializationStatus -> {
        });
        appOpenAdManager = new AppOpenAdManager.Builder(this).build();
        AudienceNetworkAds.initialize(this);
        mInstance = this;
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        OneSignal.disablePush(false);
        Log.d(TAG, "OneSignal Notification is enabled");

        // Enable verbose OneSignal logging to debug issues if needed.
        OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);
        OneSignal.initWithContext(this);
        requestTopic();

        OneSignal.setNotificationOpenedHandler(
                result -> {
                    title = result.getNotification().getTitle();
                    message = result.getNotification().getBody();
                    big_picture = result.getNotification().getBigPicture();
                    Log.d(TAG, title + ", " + message + ", " + big_picture);
                    try {
                        unique_id = result.getNotification().getAdditionalData().getLong("unique_id");
                        post_id = result.getNotification().getAdditionalData().getLong("post_id");
                        link = result.getNotification().getAdditionalData().getString("link");
                        Log.d(TAG, post_id + ", " + unique_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("unique_id", unique_id);
                    intent.putExtra("post_id", post_id);
                    intent.putExtra("title", title);
                    intent.putExtra("link", link);
                    startActivity(intent);
                });

        OneSignal.unsubscribeWhenNotificationsAreDisabled(true);

    }

    public AppOpenAdManager getAppOpenAdManager() {
        return this.appOpenAdManager;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static synchronized MyApplication getInstance() {
        return mInstance;
    }

    private void requestTopic() {
        Call<CallbackAds> callbackCall = RestAdapter.createAPI().getAds(AppConfig.API_KEY);
        callbackCall.enqueue(new Callback<CallbackAds>() {
            public void onResponse(Call<CallbackAds> call, Response<CallbackAds> response) {
                CallbackAds resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    ads = resp.ads;
                    FirebaseMessaging.getInstance().subscribeToTopic(ads.fcm_notification_topic);
                    OneSignal.setAppId(ads.onesignal_app_id);
                    Log.d(TAG, "FCM Subscribe topic : " + ads.fcm_notification_topic);
                    Log.d(TAG, "OneSignal App ID : " + ads.onesignal_app_id);
                }
            }

            public void onFailure(Call<CallbackAds> call, Throwable th) {
                Log.e("onFailure", "" + th.getMessage());
            }
        });
    }

    public void saveIsLogin(boolean flag) {
        preferences = this.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("IsLoggedIn", flag);
        editor.apply();
    }

    public boolean getIsLogin() {
        preferences = this.getSharedPreferences(prefName, 0);
        if (preferences != null) {
            return preferences.getBoolean("IsLoggedIn", false);
        }
        return false;
    }

    public void saveLogin(String user_id, String user_name, String email) {
        preferences = this.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_id", user_id);
        editor.putString("user_name", user_name);
        editor.putString("email", email);
        editor.apply();
    }

    public String getUserId() {
        preferences = this.getSharedPreferences(prefName, 0);
        if (preferences != null) {
            return preferences.getString("user_id", "");
        }
        return "";
    }

    public String getUserName() {
        preferences = this.getSharedPreferences(prefName, 0);
        if (preferences != null) {
            return preferences.getString("user_name", "");
        }
        return "";
    }

    public String getUserEmail() {
        preferences = this.getSharedPreferences(prefName, 0);
        if (preferences != null) {
            return preferences.getString("email", "");
        }
        return "";
    }

    public String getType() {
        preferences = this.getSharedPreferences(prefName, 0);
        if (preferences != null) {
            return preferences.getString("type", "");
        }
        return "";
    }

    public void saveType(String type) {
        preferences = this.getSharedPreferences(prefName, 0);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("type", type);
        editor.apply();
    }

}
