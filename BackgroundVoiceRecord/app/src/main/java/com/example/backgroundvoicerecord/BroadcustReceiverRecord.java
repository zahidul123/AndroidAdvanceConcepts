package com.example.backgroundvoicerecord;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.io.IOException;

public class BroadcustReceiverRecord extends BroadcastReceiver {
    private MediaRecorder recorder = null;
    private MediaPlayer player = null;

    private static String fileName = null;


    @Override
    public void onReceive(Context context, Intent intent) {
        //if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

        //}


        vibrateToRecordAudio(context,intent);
    }
    private void vibrateToRecordAudio(Context context,Intent intent) {

        try {
            Log.e("fdjhj,", intent.toString());
            if (intent !=null){
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                    long[] pattern = {0, 250, 250, 250/*, 300, 200, 100, 500, 200, 100*/};
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1),
                                new AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                                        .setUsage(AudioAttributes.USAGE_ALARM)
                                        .build());
                    } else {
                        vibrator.vibrate(pattern, 1);
                    }

                    recordNowInBackground(context);
                }
            }
        }catch (Exception e){
            Log.e("error,",e.toString());
            e.printStackTrace();
        }
    }

    private void recordNowInBackground(Context context) {

        player = new MediaPlayer();
        recorder = new MediaRecorder();


        // Record to the external cache directory for visibility
        fileName = context.getExternalCacheDir().getAbsolutePath();
        fileName += "/audiorecordtest.3gp";

        Thread recordInBackGround= new Thread(new Runnable() {
            @Override
            public void run() {
                recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                recorder.setOutputFile(fileName);
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                try {
                    recorder.prepare();
                } catch (IOException e) {
                    Log.e("LOG_TAG", "prepare() failed");
                }
                recorder.start();
            }
        });

        recordInBackGround.start();

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
               recorder.stop();
                try {
                    player.setDataSource(fileName);
                    player.prepare();
                    player.start();

                    player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            player.release();
                            recorder.release();
                        }
                    });

                   // context.startService(new Intent(context,RecorderService.class));
                } catch (IOException e) {
                    Log.e("LOG_TAG", "prepare() failed");
                }
            }
        }, 10000);
    }
}
