package com.app.plantdisease.activities;

import static android.content.ContentValues.TAG;
import static com.app.plantdisease.config.AppConfig.USE_LEGACY_GDPR_EU_CONSENT;
import static com.app.plantdisease.utils.Constant.ADMOB;
import static com.app.plantdisease.utils.Constant.AD_STATUS_ON;
import static com.app.plantdisease.utils.Constant.APPLOVIN;
import static com.app.plantdisease.utils.Constant.BANNER_HOME;
import static com.app.plantdisease.utils.Constant.INTERSTITIAL_POST_LIST;
import static com.app.plantdisease.utils.Constant.STARTAPP;
import static com.app.plantdisease.utils.Constant.UNITY;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.app.plantdisease.BuildConfig;
import com.app.plantdisease.R;
import com.app.plantdisease.callbacks.CallbackSettings;
import com.app.plantdisease.callbacks.CallbackUser;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.fragment.FragmentCategory;
import com.app.plantdisease.fragment.FragmentChat;
import com.app.plantdisease.fragment.FragmentRecent;
import com.app.plantdisease.fragment.FragmentLeafScan;
import com.app.plantdisease.models.Setting;
import com.app.plantdisease.models.User;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.AdNetwork;
import com.app.plantdisease.utils.AdsPref;
import com.app.plantdisease.utils.AppBarLayoutBehavior;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.GDPR;
import com.app.plantdisease.utils.RtlViewPager;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.applovin.sdk.AppLovinMediationProvider;
import com.applovin.sdk.AppLovinSdk;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.AdapterStatus;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.ump.ConsentDebugSettings;
import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;
import com.squareup.picasso.Picasso;
import com.startapp.sdk.adsbase.StartAppAd;
import com.startapp.sdk.adsbase.StartAppSDK;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsListener;
import com.unity3d.ads.UnityAds;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private long exitTime = 0;
    MyApplication myApplication;
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private TextView title_toolbar;
    MenuItem prevMenuItem;
    int pager_number = 4;
    User user;
    Setting post;
    private Call<CallbackUser> callbackCall = null;
    private Call<CallbackSettings> callbackCallSettings = null;
    ImageView img_profile;
    RelativeLayout btn_profile;
    ImageButton btn_search;
    ImageButton btn_overflow;
    SharedPref sharedPref;
    private BottomSheetDialog mBottomSheetDialog;
    AdsPref adsPref;
    private ConsentInformation consentInformation;
    ConsentForm consentForm;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        adsPref = new AdsPref(this);
        if (adsPref.getAdStatus().equals(AD_STATUS_ON) && adsPref.getAdType().equals(STARTAPP)) {
            StartAppSDK.init(MainActivity.this, adsPref.getStartappAppID(), false);
            StartAppSDK.setTestAdsEnabled(BuildConfig.DEBUG);
        }
        if (AppConfig.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_main_rtl);
        } else {
            setContentView(R.layout.activity_main);
        }

        sharedPref = new SharedPref(this);
        adNetwork = new AdNetwork(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }


        checkCameraPermission();


        if (adsPref.getAdStatus().equals(AD_STATUS_ON)) {
            switch (adsPref.getAdType()) {
                case ADMOB:
                    MobileAds.initialize(this, initializationStatus -> {
                        Map<String, AdapterStatus> statusMap = initializationStatus.getAdapterStatusMap();
                        for (String adapterClass : statusMap.keySet()) {
                            AdapterStatus status = statusMap.get(adapterClass);
                            assert status != null;
                            Log.d(TAG, String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status.getDescription(), status.getLatency()));
                            Log.d(TAG, "FAN open bidding with AdMob as mediation partner selected");
                        }
                    });
                    if (USE_LEGACY_GDPR_EU_CONSENT) {
                        GDPR.updateConsentStatus(this);
                    } else {
                        updateConsentStatus();
                    }
                    break;
                case STARTAPP:
                    StartAppSDK.setUserConsent(this, "pas", System.currentTimeMillis(), true);
                    StartAppAd.disableSplash();
                    break;
                case UNITY:
                    UnityAds.addListener(new IUnityAdsListener() {
                        @Override
                        public void onUnityAdsReady(String placementId) {
                            Log.d(TAG, placementId);
                        }

                        @Override
                        public void onUnityAdsStart(String placementId) {

                        }

                        @Override
                        public void onUnityAdsFinish(String placementId, UnityAds.FinishState finishState) {

                        }

                        @Override
                        public void onUnityAdsError(UnityAds.UnityAdsError unityAdsError, String message) {

                        }
                    });
                    UnityAds.initialize(getApplicationContext(), adsPref.getUnityGameId(), BuildConfig.DEBUG, new IUnityAdsInitializationListener() {
                        @Override
                        public void onInitializationComplete() {
                            Log.d(TAG, "Unity Ads Initialization Complete");
                            Log.d(TAG, "Unity Ads Game ID : " + adsPref.getUnityGameId());
                        }

                        @Override
                        public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
                            Log.d(TAG, "Unity Ads Initialization Failed: [" + error + "] " + message);
                        }
                    });
                    break;
                case APPLOVIN:
                    AppLovinSdk.getInstance(this).setMediationProvider(AppLovinMediationProvider.MAX);
                    AppLovinSdk.getInstance(this).initializeSdk(config -> {
                    });
                    final String sdkKey = AppLovinSdk.getInstance(getApplicationContext()).getSdkKey();
                    if (!sdkKey.equals(getString(R.string.applovin_sdk_key))) {
                        Log.e(TAG, "AppLovin ERROR : Please update your sdk key in the manifest file.");
                    }
                    Log.d(TAG, "AppLovin SDK Key : " + sdkKey);
                    break;
            }
        }


   /*     AppBarLayout appBarLayout = findViewById(R.id.tab_appbar_layout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());*/

        myApplication = MyApplication.getInstance();

        title_toolbar = findViewById(R.id.title_toolbar);
        img_profile = findViewById(R.id.img_profile);

        if (AppConfig.ENABLE_RTL_MODE) {
            initViewPagerRTL();
        } else {
            initViewPager();
        }

        Tools.notificationOpenHandler(this, getIntent());

        if (AppConfig.ENABLE_FIXED_BOTTOM_NAVIGATION) {
            navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
        }

        initToolbarIcon();
        displayUserProfile();
        adsPref.saveCounter(1);
        adNetwork.loadBannerAdNetwork(BANNER_HOME);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

        if (!BuildConfig.DEBUG) {
            validate();
        }

    }

    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission();
            return;

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);

                return ;
            }
        }
    }

    private void requestCameraPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                1);
    }

    public void showInterstitialAd() {
        adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
    }

    public void initViewPager() {
        viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPager.setOffscreenPageLimit(pager_number);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemReselectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPager.setCurrentItem(0);
                    break;
                case R.id.navigation_category:
                    viewPager.setCurrentItem(1);
                    break;
                case R.id.navigation_video:
                    viewPager.setCurrentItem(2);
                    break;
                case R.id.navigation_favorite:
                    viewPager.setCurrentItem(3);
                    break;
            }

        });

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_category:
                        viewPager.setCurrentItem(1);
                        return true;

                    case R.id.navigation_video:
                        viewPager.setCurrentItem(2);
                        return true;
                    case R.id.navigation_favorite:
                        viewPager.setCurrentItem(3);
                        return true;
                }
                return false;
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPager.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                } else if (viewPager.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                } else if (viewPager.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_video));
                } else if (viewPager.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void initViewPagerRTL() {
        viewPagerRTL = findViewById(R.id.viewpager_rtl);
        viewPagerRTL.setAdapter(new MyAdapter(getSupportFragmentManager()));
        viewPagerRTL.setOffscreenPageLimit(pager_number);

        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    viewPagerRTL.setCurrentItem(0);
                    return true;
                case R.id.navigation_category:
                    viewPagerRTL.setCurrentItem(1);
                    return true;
                case R.id.navigation_video:
                    viewPagerRTL.setCurrentItem(2);
                    return true;
                case R.id.navigation_favorite:
                    viewPagerRTL.setCurrentItem(3);
                    return true;
            }
            return false;
        });




        viewPagerRTL.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                navigation.getMenu().getItem(position).setChecked(true);
                prevMenuItem = navigation.getMenu().getItem(position);

                if (viewPagerRTL.getCurrentItem() == 0) {
                    title_toolbar.setText(getResources().getString(R.string.app_name));
                } else if (viewPagerRTL.getCurrentItem() == 1) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_category));
                } else if (viewPagerRTL.getCurrentItem() == 2) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_video));
                } else if (viewPagerRTL.getCurrentItem() == 3) {
                    title_toolbar.setText(getResources().getString(R.string.title_nav_favorite));
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class MyAdapter extends FragmentPagerAdapter {

        MyAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new FragmentRecent();
                case 1:
                    return new FragmentCategory();
                case 2:
                    return new FragmentLeafScan();
                case 3:
                    return new FragmentChat();
            }
            return null;
        }

        @Override
        public int getCount() {
            return pager_number;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void initToolbarIcon() {

        if (sharedPref.getIsDarkTheme()) {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
            navigation.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            findViewById(R.id.toolbar).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        btn_search = findViewById(R.id.btn_search);
        btn_search.setOnClickListener(view -> new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), ActivitySearch.class)), 50));

        btn_profile = findViewById(R.id.btn_profile);
        btn_profile.setOnClickListener(view -> new Handler().postDelayed(() -> startActivity(new Intent(getApplicationContext(), ActivityProfile.class)), 50));

        btn_overflow = findViewById(R.id.btn_overflow);
        btn_overflow.setOnClickListener(view -> showBottomSheetDialog());

        if (AppConfig.DISABLE_LOGIN_REGISTER) {
            btn_profile.setVisibility(View.GONE);
            btn_overflow.setVisibility(View.VISIBLE);
        } else {
            btn_profile.setVisibility(View.VISIBLE);
            btn_overflow.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if (AppConfig.ENABLE_RTL_MODE) {
            if (viewPagerRTL.getCurrentItem() != 0) {
                viewPagerRTL.setCurrentItem((0), true);
            } else {
                if (AppConfig.ENABLE_EXIT_DIALOG) {
                    exitDialog();
                } else {
                    exitApp();
                }
            }
        } else {
            if (viewPager.getCurrentItem() != 0) {
                viewPager.setCurrentItem((0), true);
            } else {
                if (AppConfig.ENABLE_EXIT_DIALOG) {
                    exitDialog();
                } else {
                    exitApp();
                }
            }
        }

    }

    public void exitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
        dialog.setTitle(R.string.dialog_close_title);
        dialog.setMessage(R.string.dialog_close_msg);
        dialog.setPositiveButton(R.string.dialog_option_quit, (dialogInterface, i) -> finish());

        dialog.setNegativeButton(R.string.dialog_option_rate_us, (dialogInterface, i) -> {
            final String appName = getPackageName();
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName)));
            } catch (android.content.ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appName)));
            }

            finish();
        });

        dialog.setNeutralButton(R.string.dialog_option_more, (dialogInterface, i) -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));

            finish();
        });
        dialog.show();
    }

    private void displayUserProfile() {
        if (myApplication.getIsLogin()) {
            requestUserData();
        } else {
            img_profile.setImageResource(R.drawable.ic_account_circle_white);
        }
    }

    private void requestUserData() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getUser(myApplication.getUserId());
        callbackCall.enqueue(new Callback<CallbackUser>() {
            @Override
            public void onResponse(Call<CallbackUser> call, Response<CallbackUser> response) {
                CallbackUser resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    user = resp.response;
                    if (user.image.equals("")) {
                        img_profile.setImageResource(R.drawable.ic_account_circle_white);
                    } else {
                        Picasso.get()
                                .load(AppConfig.ADMIN_PANEL_URL + "/upload/avatar/" + user.image.replace(" ", "%20"))
                                .resize(54, 54)
                                .centerCrop()
                                .placeholder(R.drawable.ic_account_circle_white)
                                .into(img_profile);
                    }
                }
            }

            @Override
            public void onFailure(Call<CallbackUser> call, Throwable t) {
            }

        });
    }

    private void validate() {
        ApiInterface api = RestAdapter.createAPI();
        callbackCallSettings = api.getSettings();
        callbackCallSettings.enqueue(new Callback<CallbackSettings>() {
            @Override
            public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    if (BuildConfig.APPLICATION_ID.equals(post.package_name)) {
                        Log.d("ACTIVITY_MAIN", "Package Name Validated");
                    } else {
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setTitle(getResources().getString(R.string.whops));
                        dialog.setMessage(getResources().getString(R.string.msg_validate));
                        dialog.setPositiveButton(getResources().getString(R.string.dialog_ok), (dialogInterface, i) -> finish());
                        dialog.setCancelable(false);
                        dialog.show();
                        Log.d("ACTIVITY_MAIN", "Package Name NOT Validated");
                    }
                }
            }

            @Override
            public void onFailure(Call<CallbackSettings> call, Throwable t) {
            }

        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode==this.RESULT_OK){
            super.onActivityResult(requestCode, resultCode, data);
            setResult(RESULT_OK, data);
        }
    }

    public void aboutDialog() {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(MainActivity.this);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_about, null);
        ((TextView) view.findViewById(R.id.txt_app_version)).setText(BuildConfig.VERSION_NAME + "");
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton(R.string.dialog_ok, (dialog, which) -> dialog.dismiss());
        alert.show();
    }

    private void showBottomSheetDialog() {

        final View view = getLayoutInflater().inflate(R.layout.lyt_bottom_sheet, null);

        FrameLayout lyt_bottom_sheet = view.findViewById(R.id.bottom_sheet);

        Switch switch_theme = view.findViewById(R.id.switch_theme);

        if (sharedPref.getIsDarkTheme()) {
            switch_theme.setChecked(true);
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_dark));
        } else {
            switch_theme.setChecked(false);
            lyt_bottom_sheet.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_rounded_default));
        }

        switch_theme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Log.e("INFO", "" + isChecked);
            sharedPref.setIsDarkTheme(isChecked);
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, Constant.DELAY_RIPPLE);
        });

        view.findViewById(R.id.btn_switch_theme).setOnClickListener(v -> {
            if (switch_theme.isChecked()) {
                sharedPref.setIsDarkTheme(false);
                switch_theme.setChecked(false);
            } else {
                sharedPref.setIsDarkTheme(true);
                switch_theme.setChecked(true);
            }
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }, Constant.DELAY_RIPPLE);
        });

        view.findViewById(R.id.btn_publisher_info).setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(getApplicationContext(), ActivityPublisherInfo.class));
                mBottomSheetDialog.dismiss();
            }, Constant.DELAY_RIPPLE);
        });
        view.findViewById(R.id.btn_privacy_policy).setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(getApplicationContext(), ActivityPrivacyPolicy.class));
                mBottomSheetDialog.dismiss();
            }, Constant.DELAY_RIPPLE);
        });
        view.findViewById(R.id.btn_rate).setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)));
                mBottomSheetDialog.dismiss();
            }, Constant.DELAY_RIPPLE);
        });
        view.findViewById(R.id.btn_more).setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.play_more_apps))));
                mBottomSheetDialog.dismiss();
            }, Constant.DELAY_RIPPLE);
        });
        view.findViewById(R.id.btn_about).setOnClickListener(v -> {
            new Handler().postDelayed(() -> {
                aboutDialog();
                mBottomSheetDialog.dismiss();
            }, Constant.DELAY_RIPPLE);
        });

        mBottomSheetDialog = new BottomSheetDialog(this, R.style.SheetDialog);
        mBottomSheetDialog.setContentView(view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBottomSheetDialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(dialog -> mBottomSheetDialog = null);

    }

    @Override
    protected void onResume() {
        super.onResume();
        displayUserProfile();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void updateConsentStatus() {
        if (BuildConfig.DEBUG) {
            ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(this)
                    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_NOT_EEA)
                    .addTestDeviceHashedId("TEST-DEVICE-HASHED-ID")
                    .build();
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().setConsentDebugSettings(debugSettings).build();
            consentInformation = UserMessagingPlatform.getConsentInformation(this);
            consentInformation.requestConsentInfoUpdate(this, params, () -> {
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm();
                        }
                    },
                    formError -> {
                    });
        } else {
            ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
            consentInformation = UserMessagingPlatform.getConsentInformation(this);
            consentInformation.requestConsentInfoUpdate(this, params, () -> {
                        if (consentInformation.isConsentFormAvailable()) {
                            loadForm();
                        }
                    },
                    formError -> {
                    });
        }
    }

    public void loadForm() {
        UserMessagingPlatform.loadConsentForm(this, consentForm -> {
                    MainActivity.this.consentForm = consentForm;
                    if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                        consentForm.show(MainActivity.this, formError -> {
                            loadForm();
                        });
                    }
                },
                formError -> {
                }
        );
    }

    @Override
    public AssetManager getAssets() {
        return getResources().getAssets();
    }

}
