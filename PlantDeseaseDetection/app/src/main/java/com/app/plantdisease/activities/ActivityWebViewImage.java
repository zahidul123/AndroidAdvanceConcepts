package com.app.plantdisease.activities;

import static com.app.plantdisease.utils.Tools.PERMISSIONS_REQUEST;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.app.plantdisease.R;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.app.plantdisease.utils.TouchImageView;
import com.squareup.picasso.Picasso;

public class ActivityWebViewImage extends AppCompatActivity {

    TouchImageView news_image;
    String str_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_full_screen_image);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        str_image = getIntent().getStringExtra("image_url");

        news_image = findViewById(R.id.image);

        Picasso.get()
                .load(str_image.replace(" ", "%20"))
                .placeholder(R.drawable.ic_thumbnail)
                .into(news_image);

        initToolbar();

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPref sharedPref = new SharedPref(this);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {

            case R.id.close_image:
                new Handler().postDelayed(this::finish, 300);
                return true;

            case R.id.save_image:
                new Handler().postDelayed(this::requestStoragePermission, 300);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(ActivityWebViewImage.this, "android.permission.WRITE_EXTERNAL_STORAGE") != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, PERMISSIONS_REQUEST);
            } else {
                downloadImage();
            }
        } else {
            downloadImage();
        }
    }

    public void downloadImage() {
        String image_name = str_image;
        String image_url = AppConfig.ADMIN_PANEL_URL + "/upload/" + str_image;
        Tools.downloadImage(this, image_name, image_url, "image/jpeg");
    }

}
