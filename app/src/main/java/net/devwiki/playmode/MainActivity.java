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
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener, View.OnClickListener{

    public static final String TAG = "MainActivity";

    private static String PATH = "android.resource://";

    private TextView modelView;
    private TextView hintView;

    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;
    private SensorManager sensorManager;
    private Sensor sensor;
    private PlayerManager playerManager;
    private MyReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelView = (TextView) findViewById(R.id.model);
        hintView = (TextView) findViewById(R.id.hint);
        hintView.setMovementMethod(ScrollingMovementMethod.getInstance());

        PATH = PATH + getPackageName() + "/" + R.raw.alice;
        playerManager = PlayerManager.getManager();

        addHint("onCreate");
        modelView.setText("手机型号:" + PhoneModelUtil.getPhoneModel());
    }

    private void addHint(String hint){
        hintView.append(hint);
        hintView.append("\n");
    }

    @Override
    protected void onStart() {
        super.onStart();
        addHint("onStart");
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        receiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addHint("onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        addHint("onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        addHint("onStop");
        sensorManager.unregisterListener(this);
        unregisterReceiver(receiver);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float value = event.values[0];
        if (value == sensor.getMaximumRange()){
            addHint("远离距离感应器,传感器的值:" + value);
        } else {
            addHint("靠近距离感应器,传感器的值:" + value);
        }

        if (playerManager.isWiredHeadsetOn()){
            return;
        }

        if (playerManager.isPlaying()){
            if (value == sensor.getMaximumRange()) {
                playerManager.changeToSpeaker();
                setScreenOn();
            } else {
                playerManager.changeToReceiver();
                setScreenOff();
            }
        } else {
            if(value == sensor.getMaximumRange()){
                playerManager.changeToSpeaker();
                setScreenOn();
            }
        }
    }

    private void setScreenOff(){
        if (wakeLock == null){
            wakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, TAG);
        }
        wakeLock.acquire();
    }

    private void setScreenOn(){
        if (wakeLock != null){
            wakeLock.setReferenceCounted(false);
            wakeLock.release();
            wakeLock = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private PlayerManager.PlayCallback callback = new PlayerManager.PlayCallback() {
        @Override
        public void onPrepared() {
            DLog.d("----------音乐准备完毕----------");
            addHint("音乐准备完毕,开始播放");
        }

        @Override
        public void onComplete() {
            DLog.d("----------音乐播放完毕----------");
            addHint("音乐播放完毕");
        }

        @Override
        public void onStop() {
            DLog.d("----------音乐停止播放----------");
            addHint("音乐停止播放");
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play){
            playerManager.play(PATH, callback);
        }

        if (v.getId() == R.id.stop){
            playerManager.stop();
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
                //通过广播判断耳机是否插入
                case Intent.ACTION_HEADSET_PLUG:
                    int state = intent.getIntExtra("state", 0);
                    if (state == 0){
                        addHint("耳机已拔出");
                        hintView.append("耳机已拔出\n");
                    } else if (state == 1){
                        addHint("耳机已插入");
                        playerManager.changeToHeadset();
                    }
                    break;
                /*case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    DLog.d("ACTION_AUDIO_BECOMING_NOISY");
                    if (playerManager.isWiredHeadsetOn()){
                        playerManager.changeToHeadset();
                    } else {
                        playerManager.changeToSpeaker();
                    }
                    break;*/
                default:
                    break;
            }
        }
    }
}
