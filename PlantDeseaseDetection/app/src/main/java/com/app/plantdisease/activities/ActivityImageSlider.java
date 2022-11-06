package com.app.plantdisease.activities;

import static com.app.plantdisease.utils.Tools.PERMISSIONS_REQUEST;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.app.plantdisease.R;
import com.app.plantdisease.adapter.AdapterImageSlider;
import com.app.plantdisease.callbacks.CallbackPostDetail;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.Images;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.NetworkCheck;
import com.app.plantdisease.utils.RtlViewPager;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityImageSlider extends AppCompatActivity {

    private Call<CallbackPostDetail> callbackCall = null;
    ImageButton lyt_close, lyt_save;
    TextView txt_number;
    ViewPager viewPager;
    RtlViewPager viewPagerRTL;
    Long nid;
    int position;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = new SharedPref(this);
        setTheme(R.style.AppDarkTheme);

        if (AppConfig.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_image_slider_rtl);
        } else {
            setContentView(R.layout.activity_image_slider);
        }

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        lyt_close = findViewById(R.id.lyt_close);
        lyt_save = findViewById(R.id.lyt_save);
        txt_number = findViewById(R.id.txt_number);

        nid = getIntent().getLongExtra("nid", 0);
        position = getIntent().getIntExtra("position", 0);
        //post = (News) getIntent().getSerializableExtra(EXTRA_OBJC);
        requestAction();

        initToolbar();

    }

    private void requestAction() {
        showFailedView(false, "");
        requestPostData();
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI().getNewsDetail(nid);
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(Call<CallbackPostDetail> call, Response<CallbackPostDetail> response) {
                CallbackPostDetail responseHome = response.body();
                if (responseHome == null || !responseHome.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                displayAllData(responseHome);
            }

            public void onFailure(Call<CallbackPostDetail> call, Throwable th) {
                Log.e("onFailure", th.getMessage());
                if (!call.isCanceled()) {
                    onFailRequest();
                }
            }
        });
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(ActivityImageSlider.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_home);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void displayAllData(CallbackPostDetail responseHome) {
        displayImages(responseHome.images);
    }

    private void displayImages(final List<Images> list) {
        final AdapterImageSlider adapter = new AdapterImageSlider(ActivityImageSlider.this, list);
        if (AppConfig.ENABLE_RTL_MODE) {
            viewPagerRTL = findViewById(R.id.view_pager_image_rtl);
            viewPagerRTL.setAdapter(adapter);
            viewPagerRTL.setOffscreenPageLimit(list.size());
            viewPagerRTL.setCurrentItem(position);
            viewPagerRTL.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(final int position) {
                    super.onPageSelected(position);
                    txt_number.setText((position + 1) + " of " + list.size());
                    lyt_save.setOnClickListener(view -> requestStoragePermission(list, position));
                }
            });
        } else {
            viewPager = findViewById(R.id.view_pager_image);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(list.size());
            viewPager.setCurrentItem(position);
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(final int position) {
                    super.onPageSelected(position);
                    txt_number.setText((position + 1) + " of " + list.size());
                    lyt_save.setOnClickListener(view -> requestStoragePermission(list, position));
                }
            });
        }

        txt_number.setText((position + 1) + " of " + list.size());
        lyt_save.setOnClickListener(view -> requestStoragePermission(list, position));
        lyt_close.setOnClickListener(view -> finish());

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void requestStoragePermission(final List<Images> images, final int position) {
        if (ContextCompat.checkSelfPermission(ActivityImageSlider.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage(images, position);
            }
        } else {
            downloadImage(images, position);
        }
    }

    public void downloadImage(final List<Images> images, final int position) {
        String image_name = images.get(position).image_name;
        String image_url = AppConfig.ADMIN_PANEL_URL + "/upload/" + images.get(position).image_name;
        Tools.downloadImage(this, image_name, image_url, "image/jpeg");
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        super.onDestroy();
    }


}
