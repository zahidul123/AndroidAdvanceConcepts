package com.app.plantdisease.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.app.plantdisease.R;
import com.app.plantdisease.adapter.ChatAdapter;
import com.app.plantdisease.models.MessageChatting;

import java.util.ArrayList;


public class FragmentChat extends Fragment {


    private View root_view;

    ArrayList<MessageChatting> messageChattings = new ArrayList<>();
    ChatAdapter chatAdapter;
    RecyclerView recyclerView;
    ProgressBar progressBar;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.fragment_chat, null);
        initComponent();
        setDummyData();
        setRcvAdapter();


        return root_view;
    }

    private void initComponent() {

        recyclerView = root_view.findViewById(R.id.recyclerView);
        progressBar = root_view.findViewById(R.id.progressBar);


    }

    private void setRcvAdapter() {
        progressBar.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        chatAdapter = new ChatAdapter(getContext(), messageChattings);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(chatAdapter);
    }

    private void setDummyData() {
        messageChattings.add(new MessageChatting("01", "I am fetching a problem?", "11.20 PM", "2020", "2021", "send"));
        messageChattings.add(new MessageChatting("02", "What kind of problem?", "11.21 PM", "2021", "2020", "receive"));
        messageChattings.add(new MessageChatting("03", "I am fetching a problem", "11.22 PM", "2020", "2021", "send"));
        messageChattings.add(new MessageChatting("04", "I am fetching a problem", "11.23 PM", "2021", "2020", "receive"));
        messageChattings.add(new MessageChatting("05", "I am fetching a problem", "11.24 PM", "2020", "2021", "send"));
    }


}
