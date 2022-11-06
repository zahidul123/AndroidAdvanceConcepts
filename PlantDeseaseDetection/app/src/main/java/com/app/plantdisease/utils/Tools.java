package com.app.plantdisease.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import com.app.plantdisease.R;
import com.app.plantdisease.activities.ActivityNotificationDetail;
import com.app.plantdisease.config.AppConfig;
import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.ads.mediation.facebook.FacebookAdapter;
import com.google.ads.mediation.facebook.FacebookExtras;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Tools {

    public static final int PERMISSIONS_REQUEST = 102;

    public static void getTheme(Context context) {
        SharedPref sharedPref = new SharedPref(context);
        if (sharedPref.getIsDarkTheme()) {
            context.setTheme(R.style.AppDarkTheme);
        } else {
            context.setTheme(R.style.AppTheme);
        }
    }

    public static AdRequest getAdRequest(Activity activity) {
        Bundle extras = new FacebookExtras().setNativeBanner(true).build();
        if (AppConfig.USE_LEGACY_GDPR_EU_CONSENT) {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(activity))
                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .build();
        } else {
            return new AdRequest.Builder()
                    .addNetworkExtrasBundle(FacebookAdapter.class, extras)
                    .build();
        }
    }

    public static void notificationOpenHandler(Context context, Intent getIntent) {
        long unique_id = getIntent.getLongExtra("unique_id", 0);
        long post_id = getIntent.getLongExtra("post_id", 0);
        String title = getIntent.getStringExtra("title");
        String link = getIntent.getStringExtra("link");
        if (post_id == 0) {
            if (link != null && !link.equals("")) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            }
        } else {
            Intent intent = new Intent(context, ActivityNotificationDetail.class);
            intent.putExtra("id", post_id);
            context.startActivity(intent);
        }
        Log.d("unique_id", "unique id : " + unique_id);
    }

    public static AdSize getAdSize(Activity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;
        int adWidth = (int) (widthPixels / density);
        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }

    public static String withSuffix(long count) {
        if (count < 1000) return "" + count;
        int exp = (int) (Math.log(count) / Math.log(1000));
        return String.format("%.1f%c", count / Math.pow(1000, exp), "KMGTPE".charAt(exp - 1));
    }

    public static long timeStringtoMilis(String time) {
        long milis = 0;

        try {
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = sd.parse(time);
            milis = date.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return milis;
    }

    public static String getFormatedDate(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getFormatedDateSimple(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            SimpleDateFormat oldFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat newFormat = new SimpleDateFormat("MMMM dd, yyyy");
            try {
                String newStr = newFormat.format(oldFormat.parse(date_str));
                return newStr;
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static CharSequence getTimeAgo(String date_str) {
        if (date_str != null && !date_str.trim().equals("")) {
            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //sdf.setTimeZone(TimeZone.getTimeZone("CET"));
            try {
                long time = sdf.parse(date_str).getTime();
                long now = System.currentTimeMillis();
                return DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            } catch (ParseException e) {
                return "";
            }
        } else {
            return "";
        }
    }

    public static void downloadImage(Activity activity, String filename, String downloadUrlOfImage, String mimeType) {
        try {
            DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri downloadUri = Uri.parse(downloadUrlOfImage);
            DownloadManager.Request request = new DownloadManager.Request(downloadUri);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setAllowedOverRoaming(false)
                    .setTitle(filename)
                    .setMimeType(mimeType) // Your file type. You can use this code to download other file types also.
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, File.separator + filename + ".jpg");
            dm.enqueue(request);
            //Toast.makeText(activity, "Image download started.", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download started.", Snackbar.LENGTH_SHORT).show();
        } catch (Exception e) {
            //Toast.makeText(activity, "", Toast.LENGTH_SHORT).show();
            Snackbar.make(activity.findViewById(android.R.id.content), "Image download failed.", Snackbar.LENGTH_SHORT).show();
        }
    }

}
