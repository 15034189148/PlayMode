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
    private HeadsetReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        modelView = (TextView) findViewById(R.id.model);
        hintView = (TextView) findViewById(R.id.hint);
        hintView.setMovementMethod(ScrollingMovementMethod.getInstance());

        PATH = PATH + getPackageName() + "/" + R.raw.littlestarts;
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

        receiver = new HeadsetReceiver();
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
        //耳机模式下直接返回
        if (playerManager.getCurrentMode() == PlayerManager.MODE_HEADSET){
            return;
        }

        float value = event.values[0];
        if (value == sensor.getMaximumRange()){
            addHint("远离距离感应器,传感器的值:" + value);
        } else {
            addHint("靠近距离感应器,传感器的值:" + value);
        }

        if (playerManager.isPlaying()){
            if (value == sensor.getMaximumRange()) {
                playerManager.changeToSpeakerMode();
                setScreenOn();
            } else {
                playerManager.changeToEarpieceMode();
                setScreenOff();
            }
        } else {
            if(value == sensor.getMaximumRange()){
                playerManager.changeToSpeakerMode();
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
            addHint("音乐准备完毕,开始播放");
        }

        @Override
        public void onComplete() {
            addHint("音乐播放完毕");
        }

        @Override
        public void onStop() {
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
                PlayerManager.getManager().raiseVolume();
                break;
            case KeyEvent.ACTION_DOWN:
                PlayerManager.getManager().lowerVolume();
                break;
            default:
                break;
        }
        return true;
    }

    class HeadsetReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action){
                //插入和拔出耳机会触发此广播
                case Intent.ACTION_HEADSET_PLUG:
                    addHint(Intent.ACTION_HEADSET_PLUG);
                    int state = intent.getIntExtra("state", 0);
                    if (state == 1){
                        addHint("耳机已插入");
                        playerManager.changeToHeadsetMode();
                    } else if (state == 0){
                        playerManager.resume();
                        if (playerManager.isPlaying()){
                            addHint("音乐恢复播放");
                        }
                    }
                    break;
                //拔出耳机会触发此广播,拔出不会触发,且此广播比上一个早,故可在此暂停播放,收到上一个广播时在恢复播放
                case AudioManager.ACTION_AUDIO_BECOMING_NOISY:
                    addHint(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
                    addHint("耳机已拔出");
                    playerManager.pause();
                    if (playerManager.isPause()){
                        addHint("音乐已暂停");
                    }
                    playerManager.changeToSpeakerMode();
                    break;
                default:
                    break;
            }
        }
    }
}
