package com.example.backgroundvoicerecord;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.RandomAccessFile;

public class AudioRecorder {
    public enum State {
        INITIALIZING,
        READY,
        RECORDING,
        ERROR,
        STOPPED
    }

    ;
    private byte[] audioBuffer = null;
    private int source = MediaRecorder.AudioSource.MIC;
    private int sampleRate = 0;
    private int encoder = 0;
    private int nChannels = 0;
    private int bufferRead = 0;
    private int bufferSize = 0;
    private RandomAccessFile tempAudioFile = null;
    public AudioRecord audioRecorder = null;
    private State state;
    private short bSamples = 16;
    private int framePeriod;

    // The interval in which the recorded samples are output to the file
    // Used only in uncompressed mode
    private static final int TIMER_INTERVAL = 120;
    volatile Thread t = null;
    public int TimeStamp = 0, count = 0, preTimeStamp = 0;

    public AudioRecorder(Context c) {
        this.sampleRate = 11025;
        this.encoder = AudioFormat.ENCODING_PCM_16BIT;
        this.nChannels = AudioFormat.CHANNEL_CONFIGURATION_MONO;
        this.preTimeStamp = (int) System.currentTimeMillis();


        try {
                /*
                    String fileName = "/sdcard/XYZ/11025.wav";
                    tempAudioFile = new RandomAccessFile(fileName,"rw");
                */

            framePeriod = sampleRate * TIMER_INTERVAL / 1000;
            bufferSize = framePeriod * 2 * bSamples * nChannels / 8;

            if (bufferSize < AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder)) {
                bufferSize = AudioRecord.getMinBufferSize(sampleRate, nChannels, encoder);

                // Set frame period and timer interval accordingly
                framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
                Log.w(AudioRecorder.class.getName(), "Increasing buffer size to " + Integer.toString(bufferSize));
            }

            if (ActivityCompat.checkSelfPermission(c, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            audioRecorder = new AudioRecord(source, sampleRate, nChannels, encoder, bufferSize);
            audioBuffer = new byte[2048];
            audioRecorder.setRecordPositionUpdateListener(updateListener);
            audioRecorder.setPositionNotificationPeriod(framePeriod);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        @Override
        public void onMarkerReached(AudioRecord audioRecord) {

        }

        @Override
        public void onPeriodicNotification(AudioRecord audioRecord) {

        }
    };

    public void start() {
        if (state == State.INITIALIZING) {
            audioRecorder.startRecording();
            state = State.RECORDING;

            t = new Thread() {
                public void run() {
                    //Here You can read your Audio Buffers
                    audioRecorder.read(audioBuffer, 0, 2048);
                }
            };

            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
        } else {
            Log.e(AudioRecorder.class.getName(), "start() called on illegal state");
            state = State.ERROR;
        }
    }

    public void stop() {
        if (state == State.RECORDING) {
            audioRecorder.stop();
            Thread t1 = t;
            t = null;
            t1.interrupt();
            count = 0;
            state = State.STOPPED;
        } else {
            Log.e(AudioRecorder.class.getName(), "stop() called on illegal state");
            state = State.ERROR;
        }
    }

    public void release() {
        if (state == State.RECORDING) {
            stop();
        }

        if (audioRecorder != null) {
            audioRecorder.release();
        }
    }

    public void reset() {
        try {
            if (state != State.ERROR) {
                release();
            }
        } catch (Exception e) {
            Log.e(AudioRecorder.class.getName(), e.getMessage());

            state = State.ERROR;
        }
    }

    public State getState() {
        return state;
    }
}
