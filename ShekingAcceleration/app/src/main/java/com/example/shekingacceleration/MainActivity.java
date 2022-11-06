package com.example.shekingacceleration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.example.shekingacceleration.databinding.ActivityMainBinding;
import com.skydoves.progressview.OnProgressChangeListener;

public class MainActivity extends AppCompatActivity {


    ActivityMainBinding mBinding;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    int shaek = 0;
    double accelerateCurrent = 0, acceleratePrevious;
    private long lastClickEvent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        initVariables();

        mBinding.progressView1.setProgress(10);
        mBinding.progressView1.setLabelText(String.valueOf(10) + "%");


        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        MyBroadCastReceiver mReceiver = new MyBroadCastReceiver(this);
        registerReceiver(mReceiver, filter);
    }

    private void initVariables() {

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mSensorListener, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mSensorListener);
    }

    private SensorEventListener mSensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            accelerateCurrent = (float) Math.sqrt((double) (x * x) + (y * y) + (z * z));
            double delta = accelerateCurrent - acceleratePrevious;
            shaek = (int) (shaek * 0.9f + delta);
            acceleratePrevious = accelerateCurrent;
            mBinding.txtvAcceleratinDiff.setText("Acceleration Difference: " + (int) delta);
            mBinding.txtvAcceleratinCurr.setText("Current Acceleration: " + (int) accelerateCurrent);
            mBinding.txtvAcceleratinPrev.setText("Previous Acceleration: " + (int) acceleratePrevious);


            if (shaek > 12) {

                mBinding.progressView1.setProgress((float) (delta * 10));
                mBinding.progressView1.setLabelText(shaek + "%");
                Toast t = Toast.makeText(getApplicationContext(), "Dont Shake phone", Toast.LENGTH_LONG);
                t.show();
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };


    private static final long MIN_DELAY_MS = 10000;
    int count = 0;

    public void getPowerButtonClick() {

        long swapLastClick = lastClickEvent;
        long currentTime = System.currentTimeMillis();
        lastClickEvent = currentTime;

        long timeDifference = currentTime - swapLastClick;

        if (timeDifference <= MIN_DELAY_MS) {
            count++;
        }

        if (count == 3) {
            count = 0;
            Log.e("power Button clicked", "now");
            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
           // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                v.vibrate(500);
            }
        }
    }
}