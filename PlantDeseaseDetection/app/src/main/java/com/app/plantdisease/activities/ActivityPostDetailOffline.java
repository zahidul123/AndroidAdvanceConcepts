package com.app.plantdisease.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;

import com.app.plantdisease.BuildConfig;
import com.app.plantdisease.R;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.News;
import com.app.plantdisease.utils.AppBarLayoutBehavior;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.DbHandler;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ActivityPostDetailOffline extends AppCompatActivity {

    private News post;
    View parent_view, lyt_parent;
    private Menu menu;
    TextView txt_title, txt_category, txt_date, txt_comment_count, txt_comment_text, txt_view_count;
    ImageView img_thumb_video;
    LinearLayout btn_comment, btn_view;
    private WebView webview;
    DbHandler databaseHandler;
    private String bg_paragraph;
    private String single_choice_selected;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_post_detail_offline);

        sharedPref = new SharedPref(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        databaseHandler = new DbHandler(getApplicationContext());

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        parent_view = findViewById(android.R.id.content);
        webview = findViewById(R.id.news_description);
        lyt_parent = findViewById(R.id.lyt_parent);

        txt_title = findViewById(R.id.title);
        txt_category = findViewById(R.id.category);
        txt_date = findViewById(R.id.date);
        txt_comment_count = findViewById(R.id.txt_comment_count);
        txt_comment_text = findViewById(R.id.txt_comment_text);
        txt_view_count = findViewById(R.id.txt_view_count);
        btn_comment = findViewById(R.id.btn_comment);
        btn_view = findViewById(R.id.btn_view);
        img_thumb_video = findViewById(R.id.thumbnail_video);

        btn_comment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            startActivity(intent);
        });

        txt_comment_text.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            startActivity(intent);
        });

        // get extra object
        post = (News) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        initToolbar();

        displayData();

    }

    private void initToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(post.category_name);
        }
    }

    private void displayData() {
        txt_title.setText(Html.fromHtml(post.news_title));
        txt_comment_count.setText("" + post.comments_count);

        new Handler().postDelayed(() -> {
            if (post.comments_count == 0) {
                txt_comment_text.setText(R.string.txt_no_comment);
            }
            if (post.comments_count == 1) {
                txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comment));
            } else if (post.comments_count > 1) {
                txt_comment_text.setText(getResources().getString(R.string.txt_read) + " " + post.comments_count + " " + getResources().getString(R.string.txt_comments));
            }
        }, 1000);

        webview.setBackgroundColor(Color.TRANSPARENT);
        webview.getSettings().setDefaultTextEncodingName("UTF-8");
        webview.setFocusableInTouchMode(false);
        webview.setFocusable(false);

        if (!AppConfig.ENABLE_TEXT_SELECTION) {
            webview.setOnLongClickListener(v -> true);
            webview.setLongClickable(false);
        }

        webview.getSettings().setJavaScriptEnabled(true);

        WebSettings webSettings = webview.getSettings();
        if (sharedPref.getFontSize() == 0) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
        } else if (sharedPref.getFontSize() == 1) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
        } else if (sharedPref.getFontSize() == 2) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        } else if (sharedPref.getFontSize() == 3) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
        } else if (sharedPref.getFontSize() == 4) {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
        } else {
            webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
        }

        String mimeType = "text/html; charset=UTF-8";
        String encoding = "utf-8";
        String htmlText = post.news_description;

        if (sharedPref.getIsDarkTheme()) {
            bg_paragraph = "<style type=\"text/css\">body{color: #eeeeee;} a{color:#ffffff; font-weight:bold;}";
        } else {
            bg_paragraph = "<style type=\"text/css\">body{color: #000000;} a{color:#1e88e5; font-weight:bold;}";
        }

        String font_style_default = "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font/custom_font.ttf\")}body {font-family: MyFont; font-size: medium; text-align: left;}</style>";

        String text = "<html><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        String text_rtl = "<html dir='rtl'><head>"
                + font_style_default
                + "<style>img{max-width:100%;height:auto;} figure{max-width:100%;height:auto;} iframe{width:100%;}</style> "
                + bg_paragraph
                + "</style></head>"
                + "<body>"
                + htmlText
                + "</body></html>";

        if (AppConfig.ENABLE_RTL_MODE) {
            webview.loadDataWithBaseURL(null, text_rtl, mimeType, encoding, null);
        } else {
            webview.loadDataWithBaseURL(null, text, mimeType, encoding, null);
        }

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (AppConfig.OPEN_LINK_INSIDE_APP) {
                    if (url.startsWith("http://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.startsWith("https://")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebView.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".JPG") || url.endsWith(".JPEG")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    }
                    if (url.endsWith(".pdf")) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                } else {
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png") || url.endsWith(".JPG") || url.endsWith(".JPEG")) {
                        Intent intent = new Intent(getApplicationContext(), ActivityWebViewImage.class);
                        intent.putExtra("image_url", url);
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                        startActivity(intent);
                    }
                }
                return true;
            }
        });

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txt_date.setVisibility(View.VISIBLE);
            findViewById(R.id.lyt_date).setVisibility(View.VISIBLE);
        } else {
            txt_date.setVisibility(View.GONE);
            findViewById(R.id.lyt_date).setVisibility(View.GONE);
        }
        txt_date.setText(Tools.getFormatedDate(post.news_date));

        ImageView news_image = findViewById(R.id.image);

        if (post.content_type != null && post.content_type.equals("youtube")) {
            Picasso.get()
                    .load(Constant.YOUTUBE_IMG_FRONT + post.video_id + Constant.YOUTUBE_IMG_BACK)
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                intent.putExtra("video_id", post.video_id);
                startActivity(intent);
            });

        } else if (post.content_type != null && post.content_type.equals("Url")) {

            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                intent.putExtra("video_url", post.video_url);
                startActivity(intent);
            });
        } else if (post.content_type != null && post.content_type.equals("Upload")) {

            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                startActivity(intent);
            });
        } else {
            Picasso.get()
                    .load(AppConfig.ADMIN_PANEL_URL + "/upload/" + post.news_image.replace(" ", "%20"))
                    .placeholder(R.drawable.ic_thumbnail)
                    .into(news_image);

            news_image.setOnClickListener(view -> {
                Intent intent = new Intent(getApplicationContext(), ActivityFullScreenImage.class);
                intent.putExtra("image", post.news_image);
                startActivity(intent);
            });
        }

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

        if (AppConfig.DISABLE_LOGIN_REGISTER) {
            btn_comment.setVisibility(View.GONE);
            txt_comment_text.setVisibility(View.GONE);
        } else {
            btn_comment.setVisibility(View.VISIBLE);
            txt_comment_text.setVisibility(View.VISIBLE);
        }

        if (AppConfig.ENABLE_VIEW_COUNT) {
            btn_view.setVisibility(View.VISIBLE);
            txt_view_count.setText("" + Tools.withSuffix(post.view_count));
        } else {
            btn_view.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_news_detail, menu);
        this.menu = menu;
        addToFavorite();
        return true;
    }

    public void addToFavorite() {
        List<News> data = databaseHandler.getFavRow(post.nid);
        if (data.size() == 0) {
            menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
        } else {
            if (data.get(0).getNid() == post.nid) {
                menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_font_size:
                String[] items = getResources().getStringArray(R.array.dialog_font_size);
                single_choice_selected = items[sharedPref.getFontSize()];
                int itemSelected = sharedPref.getFontSize();
                AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityPostDetailOffline.this);
                dialog.setTitle(getString(R.string.title_dialog_font_size));
                dialog.setSingleChoiceItems(items, itemSelected, (dialogInterface, i) -> single_choice_selected = items[i]);
                dialog.setPositiveButton(R.string.dialog_ok, (dialogInterface, i) -> {
                    WebSettings webSettings = webview.getSettings();
                    if (single_choice_selected.equals(getResources().getString(R.string.font_size_xsmall))) {
                        sharedPref.updateFontSize(0);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_XSMALL);
                    } else if (single_choice_selected.equals(getResources().getString(R.string.font_size_small))) {
                        sharedPref.updateFontSize(1);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_SMALL);
                    } else if (single_choice_selected.equals(getResources().getString(R.string.font_size_medium))) {
                        sharedPref.updateFontSize(2);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                    } else if (single_choice_selected.equals(getResources().getString(R.string.font_size_large))) {
                        sharedPref.updateFontSize(3);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_LARGE);
                    } else if (single_choice_selected.equals(getResources().getString(R.string.font_size_xlarge))) {
                        sharedPref.updateFontSize(4);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_XLARGE);
                    } else {
                        sharedPref.updateFontSize(2);
                        webSettings.setDefaultFontSize(Constant.FONT_SIZE_MEDIUM);
                    }
                    dialogInterface.dismiss();
                });
                dialog.show();
                break;

            case R.id.action_later:

                List<News> data = databaseHandler.getFavRow(post.nid);
                if (data.size() == 0) {
                    databaseHandler.AddtoFavorite(new News(
                            post.nid,
                            post.news_title,
                            post.category_name,
                            post.news_date,
                            post.news_image,
                            post.news_description,
                            post.content_type,
                            post.video_url,
                            post.video_id,
                            post.comments_count
                    ));
                    Snackbar.make(parent_view, R.string.favorite_added, Snackbar.LENGTH_SHORT).show();
                    menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_white));

                } else {
                    if (data.get(0).getNid() == post.nid) {
                        databaseHandler.RemoveFav(new News(post.nid));
                        Snackbar.make(parent_view, R.string.favorite_removed, Snackbar.LENGTH_SHORT).show();
                        menu.getItem(1).setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_favorite_outline_white));
                    }
                }

                break;

            case R.id.action_share:

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.news_title + "\n\n" + getResources().getString(R.string.share_content) + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

}
