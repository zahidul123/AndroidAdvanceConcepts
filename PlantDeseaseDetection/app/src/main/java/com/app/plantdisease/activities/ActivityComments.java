package com.app.plantdisease.activities;

import static com.app.plantdisease.utils.Constant.BANNER_COMMENT;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.plantdisease.R;
import com.app.plantdisease.adapter.AdapterComments;
import com.app.plantdisease.callbacks.CallbackComments;
import com.app.plantdisease.callbacks.CallbackSettings;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.Comments;
import com.app.plantdisease.models.Setting;
import com.app.plantdisease.models.Value;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.AdNetwork;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.NetworkCheck;
import com.app.plantdisease.utils.SharedPref;
import com.app.plantdisease.utils.Tools;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityComments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipe_refresh;
    private AdapterComments adapterCategory;
    private Call<CallbackComments> callbackCall = null;
    private Call<CallbackSettings> callbackCallSettings = null;
    StaggeredGridLayoutManager staggeredGridLayoutManager;
    Long nid, comments_count;
    MyApplication myApplication;
    View view;
    private ShimmerFrameLayout lyt_shimmer;
    String post_title;
    LinearLayout lyt_comment_header;
    EditText edt_comment_message;
    LinearLayout btn_post_comment;
    private ProgressDialog progress;
    Setting post;
    AdNetwork adNetwork;
    SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Tools.getTheme(this);
        setContentView(R.layout.activity_comments);
        view = findViewById(android.R.id.content);

        sharedPref = new SharedPref(this);
        adNetwork = new AdNetwork(this);
        adNetwork.loadBannerAdNetwork(BANNER_COMMENT);

        if (AppConfig.ENABLE_RTL_MODE) {
            getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }

        myApplication = MyApplication.getInstance();

        nid = getIntent().getLongExtra("nid", 0);
        comments_count = getIntent().getLongExtra("count", 0);
        post_title = getIntent().getStringExtra("post_title");

        setupToolbar();

        lyt_shimmer = findViewById(R.id.shimmer_view_container);
        lyt_comment_header = findViewById(R.id.lyt_comment_header);
        swipe_refresh = findViewById(R.id.swipe_refresh_layout_category);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        edt_comment_message = findViewById(R.id.edt_comment_message);
        btn_post_comment = findViewById(R.id.btn_post_comment);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);

        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, 1);
        recyclerView.setLayoutManager(staggeredGridLayoutManager);

        //set data and list adapter
        adapterCategory = new AdapterComments(ActivityComments.this, new ArrayList<>());
        recyclerView.setAdapter(adapterCategory);

        // on item list clicked
        adapterCategory.setOnItemClickListener((v, obj, position, context) -> {

            if (myApplication.getIsLogin() && myApplication.getUserId().equals(obj.user_id)) {

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_edit, null);

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setView(mView);

                final LinearLayout btn_edit = mView.findViewById(R.id.menu_edit);
                final LinearLayout btn_delete = mView.findViewById(R.id.menu_delete);

                final AlertDialog alertDialog = alert.create();

                btn_edit.setOnClickListener(view -> {
                    alertDialog.dismiss();
                    dialogUpdateComment(obj);
                });

                btn_delete.setOnClickListener(view -> {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(getString(R.string.confirm_delete_comment));
                    builder.setPositiveButton(getString(R.string.dialog_yes), (dialog, which) -> {
                        ApiInterface apiInterface = RestAdapter.createAPI();
                        Call<Value> call = apiInterface.deleteComment(obj.comment_id);
                        call.enqueue(new Callback<Value>() {
                            @Override
                            public void onResponse(Call<Value> call, Response<Value> response) {
                                assert response.body() != null;
                                String value = response.body().value;
                                if (value.equals("1")) {
                                    Toast.makeText(ActivityComments.this, getString(R.string.msg_success_delete_comment), Toast.LENGTH_SHORT).show();
                                    adapterCategory.resetListData();
                                    edt_comment_message.setText("");
                                    requestAction();
                                    hideKeyboard();
                                } else {
                                    Toast.makeText(ActivityComments.this, getString(R.string.msg_failed_delete_comment), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Value> call, Throwable t) {
                                t.printStackTrace();
                                Toast.makeText(ActivityComments.this, "Network error!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    });

                    builder.setNegativeButton(getString(R.string.dialog_no), null);
                    AlertDialog alert1 = builder.create();
                    alert1.show();

                    alertDialog.dismiss();
                });

                alertDialog.show();

            } else if (myApplication.getIsLogin()) {

                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(context);
                View mView = layoutInflaterAndroid.inflate(R.layout.custom_dialog_reply, null);

                final AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setView(mView);

                final LinearLayout btn_reply = mView.findViewById(R.id.menu_reply);

                final AlertDialog alertDialog = alert.create();

                btn_reply.setOnClickListener(view -> {
                    alertDialog.dismiss();

                    edt_comment_message.setText("@" + obj.name + " ");
                    edt_comment_message.setSelection(edt_comment_message.getText().length());
                    edt_comment_message.requestFocus();
                    InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    manager.showSoftInput(edt_comment_message, InputMethodManager.SHOW_IMPLICIT);

                });
                alertDialog.show();

            }

        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(() -> {
            adapterCategory.resetListData();
            requestActionOnRefresh();
        });

        requestAction();

    }

    public void setupToolbar() {

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (sharedPref.getIsDarkTheme()) {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
            findViewById(R.id.lyt_post_comment).setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
            findViewById(R.id.lyt_post_comment).setBackgroundColor(getResources().getColor(R.color.colorBackgroundLight));
        }

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(getResources().getString(R.string.title_comments));
        }
    }

    private void displayApiResult(final List<Comments> categories) {
        swipeProgress(false);
        adapterCategory.setListData(categories);
        if (categories.size() == 0) {
            showNoItemView(true);
        }
    }

    private void onFailRequest() {
        swipeProgress(false);
        if (NetworkCheck.isConnect(ActivityComments.this)) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_no_network));
        }
    }

    private void requestCategoriesApi() {
        ApiInterface apiInterface = RestAdapter.createAPI();
        callbackCall = apiInterface.getComments(nid);
        callbackCall.enqueue(new Callback<CallbackComments>() {
            @Override
            public void onResponse(Call<CallbackComments> call, Response<CallbackComments> response) {
                CallbackComments resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    displayApiResult(resp.comments);
                    initPostData();
                    Log.d("ACTIVITY_COMMENT", "Init Response");
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackComments> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    private void requestAction() {
        showFailedView(false, "");
        swipeProgress(true);
        showNoItemView(false);
        new Handler().postDelayed(this::requestCategoriesApi, Constant.DELAY_TIME);
    }

    private void requestActionOnRefresh() {
        showFailedView(false, "");
        swipeProgressOnRefresh(true);
        showNoItemView(false);
        new Handler().postDelayed(this::requestCategoriesApi, Constant.DELAY_TIME);
    }

    private void initPostData() {
        edt_comment_message.setOnClickListener(view -> {
            if (!myApplication.getIsLogin()) {
                startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
            }
        });
        edt_comment_message.setOnFocusChangeListener((v, hasFocus) -> {
            if (!myApplication.getIsLogin()) {
                startActivity(new Intent(getApplicationContext(), ActivityUserLogin.class));
            }
        });

        ((TextView) findViewById(R.id.txt_comment_count)).setText("" + adapterCategory.getItemCount());

        TextView txt_comment_text = findViewById(R.id.txt_comment_text);
        if (adapterCategory.getItemCount() <= 1) {
            txt_comment_text.setText("Comment");
        } else {
            txt_comment_text.setText("Comments");
        }

        ((TextView) findViewById(R.id.txt_post_title)).setText(post_title);

        requestPostComment();

    }

    private void requestPostComment() {
        ApiInterface api = RestAdapter.createAPI();
        callbackCallSettings = api.getSettings();
        callbackCallSettings.enqueue(new Callback<CallbackSettings>() {
            @Override
            public void onResponse(Call<CallbackSettings> call, Response<CallbackSettings> response) {
                CallbackSettings resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post = resp.post;
                    btn_post_comment.setOnClickListener(view -> {
                        if (edt_comment_message.getText().toString().equals("")) {
                            Toast.makeText(getApplicationContext(), R.string.msg_write_comment, Toast.LENGTH_SHORT).show();
                        } else if (edt_comment_message.getText().toString().length() <= 6) {
                            Toast.makeText(getApplicationContext(), R.string.msg_write_comment_character, Toast.LENGTH_SHORT).show();
                        } else {
                            dialogSendComment();
                        }
                    });

                    Log.d("ACTIVITY_COMMENT", "Ready Post Comment");
                } else {
                    onFailRequest();
                }
            }

            @Override
            public void onFailure(Call<CallbackSettings> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest();
            }

        });
    }

    public void dialogSendComment() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityComments.this);
        builder.setMessage(getString(R.string.confirm_send_comment));
        builder.setPositiveButton(getString(R.string.dialog_yes), (dialogInterface, i) -> {
            if (post.comment_approval.equals("yes")) {
                sendCommentApproval();
            } else {
                sendComment();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_no), (dialog, which) -> {
        });
        android.app.AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        swipeProgress(false);
        if (callbackCall != null && callbackCall.isExecuted()) {
            callbackCall.cancel();
        }
        lyt_shimmer.stopShimmer();
    }

    private void showFailedView(boolean flag, String message) {
        View lyt_failed = findViewById(R.id.lyt_failed_category);
        ((TextView) findViewById(R.id.failed_message)).setText(message);
        if (flag) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction());
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = findViewById(R.id.lyt_no_item_category);
        ((TextView) findViewById(R.id.txt_no_comment)).setText(R.string.msg_no_comment);
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
            swipe_refresh.setRefreshing(false);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_comment_header.setVisibility(View.VISIBLE);
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(false);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_comment_header.setVisibility(View.INVISIBLE);
        });
    }

    private void swipeProgressOnRefresh(final boolean show) {
        if (!show) {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            lyt_comment_header.setVisibility(View.VISIBLE);
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
            lyt_comment_header.setVisibility(View.INVISIBLE);
        });
    }

    public void sendComment() {

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getResources().getString(R.string.sending_comment));
        progress.show();

        String content = edt_comment_message.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Value> call = apiInterface.sendComment(nid, myApplication.getUserId(), content, date_time);
        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {

                assert response.body() != null;
                String value = response.body().value;

                new Handler().postDelayed(() -> {
                    progress.dismiss();
                    if (value.equals("1")) {
                        Toast.makeText(getApplicationContext(), R.string.msg_comment_success, Toast.LENGTH_SHORT).show();
                        edt_comment_message.setText("");
                        adapterCategory.resetListData();
                        requestAction();
                        hideKeyboard();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.msg_comment_failed, Toast.LENGTH_SHORT).show();
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void sendCommentApproval() {

        progress = new ProgressDialog(this);
        progress.setCancelable(false);
        progress.setMessage(getResources().getString(R.string.sending_comment));
        progress.show();

        String content = edt_comment_message.getText().toString();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date_time = simpleDateFormat.format(new Date());

        ApiInterface apiInterface = RestAdapter.createAPI();
        Call<Value> call = apiInterface.sendComment(nid, myApplication.getUserId(), content, date_time);

        call.enqueue(new Callback<Value>() {
            @Override
            public void onResponse(Call<Value> call, Response<Value> response) {

                assert response.body() != null;
                final String value = response.body().value;

                new Handler().postDelayed(() -> {
                    progress.dismiss();
                    if (value.equals("1")) {
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ActivityComments.this);
                        builder.setMessage(R.string.msg_comment_approval);
                        builder.setPositiveButton(getString(R.string.dialog_ok), (dialogInterface, i) -> {
                            Toast.makeText(getApplicationContext(), R.string.msg_comment_success, Toast.LENGTH_SHORT).show();
                            edt_comment_message.setText("");
                            adapterCategory.resetListData();
                            requestAction();
                            hideKeyboard();
                        });
                        android.app.AlertDialog alert = builder.create();
                        alert.show();
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.msg_comment_failed, Toast.LENGTH_SHORT).show();
                    }
                }, Constant.DELAY_REFRESH);

            }

            @Override
            public void onFailure(Call<Value> call, Throwable t) {
                progress.dismiss();
                Toast.makeText(getApplicationContext(), "Network Error!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void dialogUpdateComment(Comments obj) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(ActivityComments.this);
        View view = layoutInflaterAndroid.inflate(R.layout.custom_dialog_comment, null);

        if (sharedPref.getIsDarkTheme()) {
            view.findViewById(R.id.header_update_comment).setBackgroundColor(getResources().getColor(R.color.colorToolbarDark));
        } else {
            view.findViewById(R.id.header_update_comment).setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        }

        EditText edt_id = view.findViewById(R.id.edt_id);
        edt_id.setText(obj.comment_id);

        EditText edt_date_time = view.findViewById(R.id.edt_date_time);
        edt_date_time.setText(obj.date_time);

        EditText edt_update_message = view.findViewById(R.id.edt_update_message);
        edt_update_message.setText(obj.content);
        edt_update_message.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.showSoftInput(edt_update_message, InputMethodManager.SHOW_IMPLICIT);

        final AlertDialog.Builder alert = new AlertDialog.Builder(ActivityComments.this);
        alert.setView(view);
        alert.setCancelable(false);
        alert.setPositiveButton("UPDATE", (dialog, which) -> {

            if (edt_update_message.getText().toString().equals("")) {
                Toast.makeText(getApplicationContext(), R.string.msg_write_comment, Toast.LENGTH_SHORT).show();
            } else if (edt_update_message.getText().toString().length() <= 6) {
                Toast.makeText(getApplicationContext(), R.string.msg_write_comment_character, Toast.LENGTH_SHORT).show();
            } else {

                dialog.dismiss();
                hideKeyboard();

                progress = new ProgressDialog(this);
                progress.setCancelable(false);
                progress.setMessage(getResources().getString(R.string.updating_comment));
                progress.show();

                String comment_id = edt_id.getText().toString();
                String date_time = edt_date_time.getText().toString();
                String content = edt_update_message.getText().toString();

                ApiInterface apiInterface = RestAdapter.createAPI();
                Call<Value> call = apiInterface.updateComment(comment_id, date_time, content);
                call.enqueue(new Callback<Value>() {
                    @Override
                    public void onResponse(Call<Value> call, Response<Value> response) {

                        assert response.body() != null;
                        String value = response.body().value;

                        new Handler().postDelayed(() -> {
                            progress.dismiss();
                            if (value.equals("1")) {
                                Toast.makeText(getApplicationContext(), getString(R.string.msg_comment_update), Toast.LENGTH_SHORT).show();
                                adapterCategory.resetListData();
                                requestAction();
                                hideKeyboard();
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.msg_update_comment_failed), Toast.LENGTH_SHORT).show();
                            }
                        }, Constant.DELAY_REFRESH);
                    }

                    @Override
                    public void onFailure(Call<Value> call, Throwable t) {
                        t.printStackTrace();
                        progress.dismiss();
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_no_network), Toast.LENGTH_SHORT).show();
                    }
                });

            }

        });
        alert.setNegativeButton(getString(R.string.dialog_cancel), (dialog, which) -> {
            dialog.dismiss();
            hideKeyboard();
        });
        alert.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                if (edt_comment_message.length() > 0) {
                    edt_comment_message.setText("");
                } else {
                    onBackPressed();
                }
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        adapterCategory.resetListData();
        requestAction();
    }

    @Override
    public void onBackPressed() {
        if (edt_comment_message.length() > 0) {
            edt_comment_message.setText("");
        } else {
            super.onBackPressed();
        }
    }

}