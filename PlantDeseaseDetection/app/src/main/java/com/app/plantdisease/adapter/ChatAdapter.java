package com.app.plantdisease.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.plantdisease.R;
import com.app.plantdisease.models.Category;
import com.app.plantdisease.models.MessageChatting;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<MessageChatting> items;

    private Context ctx;

    int VIEW_TYPE_SENT = 1;
    int VIEW_TYPE_RECEIVE = 2;


    // Provide a suitable constructor (depends on the kind of dataset)
    public ChatAdapter(Context context, ArrayList<MessageChatting> items) {
        this.items = items;
        ctx = context;
    }

    public class SentViewHolder extends RecyclerView.ViewHolder {
        TextView messageBody, messageTime;

        public SentViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.message_body);
            messageTime = itemView.findViewById(R.id.message_time);
        }

        public void setData(MessageChatting messageChatting) {

            messageBody.setText(messageChatting.getMessageBody());
            messageTime.setText(messageChatting.getMessageTime());

        }
    }

    public class ReceiveViewHolder extends RecyclerView.ViewHolder {
        TextView messageBody, messageTime;

        public ReceiveViewHolder(@NonNull View itemView) {
            super(itemView);
            messageBody = itemView.findViewById(R.id.message_body);
            messageTime = itemView.findViewById(R.id.message_time);
        }

        public void setData(MessageChatting messageChatting) {

            messageBody.setText(messageChatting.getMessageBody());
            messageTime.setText(messageChatting.getMessageTime());
        }
    }

   /* @Override
    public AdapterCategory.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lsv_item_category, parent, false);
        AdapterCategory.ViewHolder vh = new AdapterCategory.ViewHolder(v);
        return vh;
    }*/

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rcv_sent_message_layout, parent, false);

            return new SentViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rcv_receive_message_layout, parent, false);

            return new ReceiveViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentViewHolder)holder).setData(items.get(position));

        } else {
            ((ReceiveViewHolder)holder).setData(items.get(position));
        }


    }


    @Override
    public int getItemViewType(int position) {
        if (items.get(position).getMessageType().equals("send")) {
            return VIEW_TYPE_SENT;
        } else
            return VIEW_TYPE_RECEIVE;
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return items.size();
    }

}