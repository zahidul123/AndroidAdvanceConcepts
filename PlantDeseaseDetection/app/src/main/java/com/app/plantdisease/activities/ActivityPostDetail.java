package com.app.plantdisease.activities;

import static com.app.plantdisease.utils.Constant.BANNER_POST_DETAIL;
import static com.app.plantdisease.utils.Constant.INTERSTITIAL_POST_DETAIL;
import static com.app.plantdisease.utils.Constant.NATIVE_AD_POST_DETAIL;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.app.plantdisease.BuildConfig;
import com.app.plantdisease.R;
import com.app.plantdisease.adapter.AdapterImage;
import com.app.plantdisease.adapter.AdapterRelated;
import com.app.plantdisease.callbacks.CallbackPostDetail;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.Images;
import com.app.plantdisease.models.News;
import com.app.plantdisease.models.Value;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.AdNetwork;
import com.app.plantdisease.utils.AdsPref;
import com.app.plantdisease.utils.AppBarLayoutBehavior;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.DbHandler;
import com.app.plantdisease.utils.NetworkCheck;
import com.app.plantdisease.utils.RtlViewPager;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityPostDetail extends AppCompatActivity {

    private Call<CallbackPostDetail> callbackCall = null;
    private View lyt_main_content;
    private ViewPager viewPager;
    private RtlViewPager viewPagerRTL;
    private News post;
    private Menu menu;
    TextView txt_title, txt_category, txt_date, txt_comment_count, txt_comment_text, txt_view_count;
    ImageView img_thumb_video, img_date;
    LinearLayout btn_comment, btn_view;
    private WebView webview;
    DbHandler databaseHandler;
    CoordinatorLayout parent_view;
    private ShimmerFrameLayout lyt_shimmer;
    RelativeLayout lyt_related;
    private SwipeRefreshLayout swipe_refresh;
    private String bg_paragraph;
    private String single_choice_selected;
    SharedPref sharedPref;
    AdsPref adsPref;
    AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        if (AppConfig.ENABLE_RTL_MODE) {
            setContentView(R.layout.activity_post_detail_rtl);
        } else {
            setContentView(R.layout.activity_post_detail);
        }

        post = (News) getIntent().getSerializableExtra(Constant.EXTRA_OBJC);

        sharedPref = new SharedPref(this);
        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_POST_DETAIL);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_DETAIL);
        adNetwork.loadNativeAdNetwork(NATIVE_AD_POST_DETAIL);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        databaseHandler = new DbHandler(getApplicationContext());

        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);
        ((CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams()).setBehavior(new AppBarLayoutBehavior());

        swipe_refresh = findViewById(R.id.swipe_refresh_layout);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        swipe_refresh.setRefreshing(false);

        lyt_main_content = findViewById(R.id.lyt_main_content);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        parent_view = findViewById(R.id.coordinatorLayout);
        webview = findViewById(R.id.news_description);
        txt_title = findViewById(R.id.title);
        txt_category = findViewById(R.id.category);
        txt_date = findViewById(R.id.date);
        img_date = findViewById(R.id.ic_date);
        txt_comment_count = findViewById(R.id.txt_comment_count);
        txt_comment_text = findViewById(R.id.txt_comment_text);
        txt_view_count = findViewById(R.id.txt_view_count);
        btn_comment = findViewById(R.id.btn_comment);
        btn_view = findViewById(R.id.btn_view);
        img_thumb_video = findViewById(R.id.thumbnail_video);

        lyt_related = findViewById(R.id.lyt_related);

        requestAction();

        swipe_refresh.setOnRefreshListener(() -> {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
            requestAction();
        });

        initToolbar();
        updateView(post.nid);

    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        new Handler().postDelayed(this::requestPostData, 200);
    }

    private void requestPostData() {
        this.callbackCall = RestAdapter.createAPI().getNewsDetail(post.nid);
        this.callbackCall.enqueue(new Callback<CallbackPostDetail>() {
            public void onResponse(Call<CallbackPostDetail> call, Response<CallbackPostDetail> response) {
                CallbackPostDetail responseHome = response.body();
                if (responseHome == null || !responseHome.status.equals("ok")) {
                    onFailRequest();
                    return;
                }
                displayAllData(responseHome);
                swipeProgress(false);
                lyt_main_content.setVisibility(View.VISIBLE);
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
        swipeProgress(false);
        lyt_main_content.setVisibility(View.GONE);
        if (NetworkCheck.isConnect(ActivityPostDetail.this)) {
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

    private void swipeProgress(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_main_content.setVisibility(View.VISIBLE);
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_main_content.setVisibility(View.GONE);
        });
    }

    private void displayAllData(CallbackPostDetail responseHome) {
        displayImages(responseHome.images);
        displayPostData(responseHome.post);
        displayRelated(responseHome.related);
    }

    private void displayPostData(final News post) {
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
        }, 1500);

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

        String text_default = "<html><head>"
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
            webview.loadDataWithBaseURL(null, text_default, mimeType, encoding, null);
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
                    if (url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".png")) {
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
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
                return true;
            }
        });

        FrameLayout customViewContainer = findViewById(R.id.customViewContainer);
        webview.setWebChromeClient(new WebChromeClient() {
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                webview.setVisibility(View.INVISIBLE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);
            }

            public void onHideCustomView() {
                super.onHideCustomView();
                webview.setVisibility(View.VISIBLE);
                customViewContainer.setVisibility(View.GONE);
            }
        });

        txt_category.setText(post.category_name);
        txt_category.setBackgroundColor(ContextCompat.getColor(this, R.color.colorCategory));

        if (AppConfig.ENABLE_DATE_DISPLAY) {
            txt_date.setVisibility(View.VISIBLE);
            img_date.setVisibility(View.VISIBLE);
        } else {
            txt_date.setVisibility(View.GONE);
            img_date.setVisibility(View.GONE);
        }
        txt_date.setText(Tools.getFormatedDate(post.news_date));

        if (!post.content_type.equals("Post")) {
            img_thumb_video.setVisibility(View.VISIBLE);
        } else {
            img_thumb_video.setVisibility(View.GONE);
        }

        new Handler().postDelayed(() -> {
            lyt_related.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txt_related)).setText(getString(R.string.txt_suggested));
        }, 2000);

        btn_comment.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
        });

        txt_comment_text.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ActivityComments.class);
            intent.putExtra("nid", post.nid);
            intent.putExtra("count", post.comments_count);
            intent.putExtra("post_title", post.news_title);
            startActivity(intent);
        });

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

    private void displayImages(final List<Images> list) {

        TabLayout tabLayout = findViewById(R.id.tabDots);
        final AdapterImage adapter = new AdapterImage(ActivityPostDetail.this, list);

        if (AppConfig.ENABLE_RTL_MODE) {
            viewPagerRTL = findViewById(R.id.view_pager_image_rtl);
            viewPagerRTL.setAdapter(adapter);
            viewPagerRTL.setOffscreenPageLimit(list.size());
            viewPagerRTL.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            tabLayout.setupWithViewPager(viewPagerRTL, true);
        } else {
            viewPager = findViewById(R.id.view_pager_image);
            viewPager.setAdapter(adapter);
            viewPager.setOffscreenPageLimit(list.size());
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                }
            });
            tabLayout.setupWithViewPager(viewPager, true);
        }

        if (list.size() > 1) {
            tabLayout.setVisibility(View.VISIBLE);
        } else {
            tabLayout.setVisibility(View.GONE);
        }

        adapter.setOnItemClickListener((view, p, position) -> {

            switch (p.content_type) {
                case "youtube": {
                    Intent intent = new Intent(getApplicationContext(), ActivityYoutubePlayer.class);
                    intent.putExtra("video_id", p.video_id);
                    startActivity(intent);
                    break;
                }
                case "Url": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", post.video_url);
                    startActivity(intent);
                    break;
                }
                case "Upload": {
                    Intent intent = new Intent(getApplicationContext(), ActivityVideoPlayer.class);
                    intent.putExtra("video_url", AppConfig.ADMIN_PANEL_URL + "/upload/video/" + post.video_url);
                    startActivity(intent);
                    break;
                }
                default: {
                    Intent intent = new Intent(getApplicationContext(), ActivityImageSlider.class);
                    intent.putExtra("position", position);
                    intent.putExtra("nid", post.nid);
                    startActivity(intent);
                    break;
                }
            }

            if (adsPref.getCounter() >= adsPref.getInterstitialAdInterval()) {
                Log.d("COUNTER_STATUS", "reset and show interstitial");
                adsPref.saveCounter(1);
                adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_DETAIL, 1);
            } else {
                adsPref.saveCounter(adsPref.getCounter() + 1);
            }

        });

    }

    private void displayRelated(List<News> list) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view_related);
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityPostDetail.this));
        AdapterRelated adapterNews = new AdapterRelated(ActivityPostDetail.this, recyclerView, list);
        recyclerView.setAdapter(adapterNews);
        recyclerView.setNestedScrollingEnabled(false);
        adapterNews.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
        });
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
                AlertDialog.Builder dialog = new AlertDialog.Builder(ActivityPostDetail.this);
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
                sendIntent.putExtra(Intent.EXTRA_TEXT, post.news_title + "\n\n" + getResources().getString(R.string.share_content) + "\n\n" + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);

                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    private void updateView(long nid) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Value> call = apiInterface.updateView(nid);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {
                Value data = response.body();
                if (data != null) {
                    Log.d("UPDATE_VIEW", "View counter updated +" + data.value);
                }
            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {

            }
        });
    }

    public void onDestroy() {
        if (!(callbackCall == null || callbackCall.isCanceled())) {
            this.callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("COUNTER", "counter : " + adsPref.getCounter());
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}
