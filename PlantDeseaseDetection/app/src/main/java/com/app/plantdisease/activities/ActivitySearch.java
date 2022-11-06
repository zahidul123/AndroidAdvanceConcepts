package com.app.plantdisease.activities;

import static com.app.plantdisease.utils.Constant.BANNER_SEARCH;
import static com.app.plantdisease.utils.Constant.INTERSTITIAL_POST_LIST;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.app.plantdisease.R;
import com.app.plantdisease.adapter.AdapterNews;
import com.app.plantdisease.adapter.AdapterSearch;
import com.app.plantdisease.callbacks.CallbackRecent;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.AdNetwork;
import com.app.plantdisease.utils.AdsPref;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.NetworkCheck;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivitySearch extends AppCompatActivity {

    private EditText et_search;
    private RecyclerView recyclerView;
    private AdapterNews mAdapter;
    private RecyclerView recyclerSuggestion;
    private AdapterSearch mAdapterSuggestion;
    private LinearLayout lyt_suggestion;
    private ImageButton bt_clear;
    private View parent_view;
    private Call<CallbackRecent> callbackCall = null;
    private ShimmerFrameLayout lyt_shimmer;
    public static final String EXTRA_OBJC = "key.EXTRA_OBJC";
    private AdsPref adsPref;
    private AdNetwork adNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_search);
        parent_view = findViewById(android.R.id.content);

        adsPref = new AdsPref(this);
        adNetwork = new AdNetwork(this);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        initComponent();
        setupToolbar();
        adNetwork.loadBannerAdNetwork(BANNER_SEARCH);
        adNetwork.loadInterstitialAdNetwork(INTERSTITIAL_POST_LIST);

    }

    private void initComponent() {
        lyt_suggestion = findViewById(R.id.lyt_suggestion);
        et_search = findViewById(R.id.et_search);
        bt_clear = findViewById(R.id.bt_clear);
        bt_clear.setVisibility(View.GONE);
        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerSuggestion = findViewById(R.id.recyclerSuggestion);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        recyclerSuggestion.setLayoutManager(new LinearLayoutManager(this));
        recyclerSuggestion.setHasFixedSize(true);

        et_search.addTextChangedListener(textWatcher);

        //set data and list adapter
        mAdapter = new AdapterNews(this, recyclerView, new ArrayList<>());
        recyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickListener((view, obj, position) -> {
            Intent intent = new Intent(getApplicationContext(), ActivityPostDetail.class);
            intent.putExtra(EXTRA_OBJC, obj);
            startActivity(intent);
            adNetwork.showInterstitialAdNetwork(INTERSTITIAL_POST_LIST, adsPref.getInterstitialAdInterval());
        });

        //set data and list adapter suggestion
        mAdapterSuggestion = new AdapterSearch(this);
        recyclerSuggestion.setAdapter(mAdapterSuggestion);
        showSuggestionSearch();
        mAdapterSuggestion.setOnItemClickListener((view, viewModel, pos) -> {
            et_search.setText(viewModel);
            lyt_suggestion.setVisibility(View.GONE);
            hideKeyboard();
            searchAction();
        });

        bt_clear.setOnClickListener(view -> et_search.setText(""));

        et_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard();
                searchAction();
                return true;
            }
            return false;
        });

        et_search.setOnTouchListener((view, motionEvent) -> {
            showSuggestionSearch();
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return false;
        });

    }

    public void setupToolbar() {
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPref sharedPref = new SharedPref(this);
        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
            findViewById(R.id.bg_view).setBackgroundColor(getResources().getColor(R.color.colorBackgroundDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.bg_view).setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence c, int i, int i1, int i2) {
            if (c.toString().trim().length() == 0) {
                bt_clear.setVisibility(View.GONE);
            } else {
                bt_clear.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence c, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private void requestSearchApi(final String query) {
        ApiInterface apiInterface = RestAdapter.createAPI();
        if (AppConfig.ENABLE_RTL_MODE) {
            callbackCall = apiInterface.getSearchPostsRTL(AppConfig.API_KEY, query, Constant.MAX_SEARCH_RESULT);
        } else {
            callbackCall = apiInterface.getSearchPosts(AppConfig.API_KEY, query, Constant.MAX_SEARCH_RESULT);
        }
        callbackCall.enqueue(new Callback<CallbackRecent>() {
            @Override
            public void onResponse(Call<CallbackRecent> call, Response<CallbackRecent> response) {
                CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    mAdapter.insertData(resp.posts);
                    if (resp.posts.size() == 0) showNotFoundView(true);
                } else {
                    onFailRequest();
                }
                swipeProgress(false);
            }

            @Override
            public void onFailure(Call<CallbackRecent> call, Throwable t) {
                onFailRequest();
                swipeProgress(false);
            }

        });
    }

    private void onFailRequest() {
        if (NetworkCheck.isConnect(this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void searchAction() {
        lyt_suggestion.setVisibility(View.GONE);
        showFailedView(false, "");
        showNotFoundView(false);
        final String query = et_search.getText().toString().trim();
        if (!query.equals("")) {
            mAdapterSuggestion.addSearchHistory(query);
            mAdapter.resetListData();
            swipeProgress(true);
            new Handler().postDelayed(() -> requestSearchApi(query), Constant.DELAY_TIME);
        } else {
            Toast.makeText(this, R.string.msg_search_input, Toast.LENGTH_SHORT).show();
            swipeProgress(false);
        }
    }

    private void showSuggestionSearch() {
        mAdapterSuggestion.refreshItems();
        lyt_suggestion.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void showFailedView(boolean show, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> searchAction());
    }

    private void showNotFoundView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item);
        ((TextView) findViewById(R.id.no_item_message)).setText(R.string.msg_no_news_found);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_no_item.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_no_item.setVisibility(View.GONE);
        }
    }

    private void swipeProgress(final boolean show) {
        if (!show) {
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        } else {
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        }
    }

    @Override
    public void onBackPressed() {
        if (et_search.length() > 0) {
            et_search.setText("");
        } else {
            super.onBackPressed();
        }
    }



}
