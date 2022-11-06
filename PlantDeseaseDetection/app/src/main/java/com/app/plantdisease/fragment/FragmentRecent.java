package com.app.plantdisease.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.plantdisease.R;
import com.app.plantdisease.activities.ActivityPostDetail;
import com.app.plantdisease.activities.MainActivity;
import com.app.plantdisease.adapter.AdapterRecent;
import com.app.plantdisease.callbacks.CallbackRecent;
import com.app.plantdisease.config.AppConfig;
import com.app.plantdisease.models.News;
import com.app.plantdisease.rests.ApiInterface;
import com.app.plantdisease.rests.RestAdapter;
import com.app.plantdisease.utils.Constant;
import com.app.plantdisease.utils.NetworkCheck;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentRecent extends Fragment {

    private View root_view, parent_view;
    private RecyclerView recyclerView;
    private AdapterRecent mAdapter;
    private SwipeRefreshLayout swipe_refresh;
    private Call<Object> callbackCall = null;
    private int post_total = 0;
    private int failed_page = 0;
    private ArrayList<Object> feedItems = new ArrayList<>();
    private ShimmerFrameLayout lyt_shimmer;
    View lyt_shimmer_head;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_home, null);
        parent_view = getActivity().findViewById(R.id.main_content);

        lyt_shimmer = root_view.findViewById(R.id.shimmer_view_container);
        lyt_shimmer_head = root_view.findViewById(R.id.lyt_shimmer_head);
        if (!AppConfig.DISPLAY_HEADER_VIEW) {
            lyt_shimmer_head.setVisibility(View.GONE);
        }

        swipe_refresh = root_view.findViewById(R.id.swipe_refresh_layout_home);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        recyclerView = root_view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(true);

        //set data and list adapter
        mAdapter = new AdapterRecent(getActivity(), recyclerView, feedItems);
        recyclerView.setAdapter(mAdapter);

        // on item list clicked
        mAdapter.setOnItemClickListener((v, obj, position) -> {
            Intent intent = new Intent(getActivity(), ActivityPostDetail.class);
            intent.putExtra(Constant.EXTRA_OBJC, obj);
            startActivity(intent);
            ((MainActivity) getActivity()).showInterstitialAd();
        });

        // detect when scroll reach bottom
        mAdapter.setOnLoadMoreListener(current_page -> {
            if (post_total > mAdapter.getItemCount() && current_page != 0) {
                int next_page = current_page + 1;
                requestAction(next_page);
            } else {
                mAdapter.setLoaded();
            }
        });

        // on swipe list
        swipe_refresh.setOnRefreshListener(() -> {
            if (callbackCall != null && callbackCall.isExecuted()) callbackCall.cancel();
            mAdapter.resetListData();
            requestAction(1);
        });

        requestAction(1);

        return root_view;
    }

    private void displayApiResult(final List<News> posts) {
        mAdapter.insertData(posts);
        swipeProgress(false);
        if (posts.size() == 0) {
            showNoItemView(true);
        }
    }

    private void requestListPostApi(final int page_no) {
        ApiInterface apiInterface = RestAdapter.getMainFord();
        callbackCall = apiInterface.getRecentPost(AppConfig.API_KEY, page_no, AppConfig.LOAD_MORE);
        callbackCall.enqueue(new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {

                Log.e("response",response.toString());
              /*  CallbackRecent resp = response.body();
                if (resp != null && resp.status.equals("ok")) {
                    post_total = resp.count_total;
                    displayApiResult(resp.posts);
                } else {
                    onFailRequest(page_no);
                }*/
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                if (!call.isCanceled()) onFailRequest(page_no);
            }

        });
    }

    private void onFailRequest(int page_no) {
        failed_page = page_no;
        mAdapter.setLoaded();
        swipeProgress(false);
        if (NetworkCheck.isConnect(getActivity())) {
            showFailedView(true, getString(R.string.msg_no_network));
        } else {
            showFailedView(true, getString(R.string.msg_offline));
        }
    }

    private void requestAction(final int page_no) {
        showFailedView(false, "");
        showNoItemView(false);
        if (page_no == 1) {
            swipeProgress(true);
        } else {
            mAdapter.setLoading();
        }
        new Handler().postDelayed(() -> requestListPostApi(page_no), Constant.DELAY_TIME);
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

    private void showFailedView(boolean show, String message) {
        View lyt_failed = root_view.findViewById(R.id.lyt_failed_home);
        ((TextView) root_view.findViewById(R.id.failed_message)).setText(message);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            lyt_failed.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            lyt_failed.setVisibility(View.GONE);
        }
        root_view.findViewById(R.id.failed_retry).setOnClickListener(view -> requestAction(failed_page));
    }

    private void showNoItemView(boolean show) {
        View lyt_no_item = root_view.findViewById(R.id.lyt_no_item_home);
        ((TextView) root_view.findViewById(R.id.no_item_message)).setText(R.string.msg_no_news);
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
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.GONE);
            lyt_shimmer.stopShimmer();
            return;
        }
        swipe_refresh.post(() -> {
            swipe_refresh.setRefreshing(show);
            lyt_shimmer.setVisibility(View.VISIBLE);
            lyt_shimmer.startShimmer();
        });
    }

}

