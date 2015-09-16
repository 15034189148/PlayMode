package net.devwiki.playmode;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by Administrator on 2015/8/27 0027.
 */
public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener{

    public static final String TAG = "MainActivity";

    private static String PATH = "android.resource://";

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private PlayerManager playerManager;
    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PATH = PATH + getPackageName() + "/" + R.raw.alice;
        playerManager = PlayerManager.getManager(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, TAG);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);

        unregisterReceiver(receiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (playerManager.isWiredHeadsetOn()){
            return;
        }

        if (playerManager.isPlaying()){
            float value = event.values[0];
            if (value == 0.0) {
                playerManager.changeToEarphoneForSensor();
                if (wakeLock.isHeld()){
                    return;
                } else {
                    wakeLock.acquire();
                }
            } else {
                playerManager.changeToSpeaker();
                if (wakeLock.isHeld()){
                    return;
                } else {
                    wakeLock.setReferenceCounted(false);
                    wakeLock.release();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private PlayerManager.PlayCallback callback = new PlayerManager.PlayCallback() {
        @Override
        public void onPrepared() {

        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onStop() {

        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play){
            playerManager.play(PATH, callback);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_VOLUME_UP:
                playerManager.raiseVolume();
                break;
            case KeyEvent.ACTION_DOWN:
                playerManager.lowerVolume();
                break;

        }
        return super.onKeyDown(keyCode, event);
    }

    class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case Intent.ACTION_HEADSET_PLUG:
                    DLog.d("ACTION_HEADSET_PLUG");
                    break;
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    DLog.d("ACTION_AUDIO_BECOMING_NOISY");
                    if (playerManager.isWiredHeadsetOn()){
                        playerManager.changeToEarphoneForEarphone();
                    } else {
                        playerManager.changeToSpeaker();
                    }
                    break;
            }
        }
    }
}
