package com.example.backgroundvoicerecord;

import static android.app.AlarmManager.ELAPSED_REALTIME;
import static android.os.SystemClock.elapsedRealtime;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class RecorderService extends Service {

    public Context context = this;
    public Handler handler = null;
    int count = 0;
    public static Runnable runnable = null;
    private BroadcastReceiver mReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Service created!", Toast.LENGTH_LONG).show();
      /*  final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mReceiver = new BroadcustReceiverRecord();
        registerReceiver(mReceiver, filter);*/

       // setneverEndService();


    }

    public void setneverEndService() {
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_APP_ERROR);
        mReceiver = new BroadcustReceiverRecord();
        registerReceiver(mReceiver, filter);


        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                count++;

                Log.e("running ", count + "");

                Toast.makeText(context, "Service is still running", Toast.LENGTH_SHORT).show();
                handler.postDelayed(runnable, 10000);
            }
        };

        handler.postDelayed(runnable, 15000);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        setneverEndService();

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        if (mReceiver != null){

            unregisterReceiver(mReceiver);
        }
       /* final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(Intent.ACTION_APP_ERROR);
        mReceiver = new BroadcustReceiverRecord();
        registerReceiver(mReceiver, filter);*/
       // startService(new Intent(this, RecorderService.class));
        /*Intent broadcastIntent = new Intent("ac.in.ActivityRecognition.RestartSensor");
        sendBroadcast(broadcastIntent);*/
        super.onDestroy();
    }
/*
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());

        PendingIntent restartServicePendingIntent = PendingIntent.getService(
                getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmService.set(ELAPSED_REALTIME, elapsedRealtime() + 1000, restartServicePendingIntent);
        super.onTaskRemoved(rootIntent);


    }*/
}
