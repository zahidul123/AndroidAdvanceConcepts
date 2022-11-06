package com.example.shekingacceleration;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyBroadCastReceiver extends BroadcastReceiver {

    MainActivity contexts;
    public MyBroadCastReceiver(MainActivity context){
        this.contexts=context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
            contexts.getPowerButtonClick();
        }

    }
}
