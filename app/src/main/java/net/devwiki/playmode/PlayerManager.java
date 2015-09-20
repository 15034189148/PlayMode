package net.devwiki.playmode;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

/**
 * 音乐播放管理类
 * Created by Administrator on 2015/8/27 0027.
 */
public class PlayerManager {

    private static PlayerManager playerManager;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private PlayCallback callback;
    private Context context;

    public static PlayerManager getManager(){
        if (playerManager == null){
            synchronized (PlayerManager.class){
                playerManager = new PlayerManager();
            }
        }
        return playerManager;
    }

    private PlayerManager(){
        this.context = MyApplication.getContext();
        mediaPlayer = new MediaPlayer();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * 播放回调接口
     */
    public interface PlayCallback{

        /**
         * 音乐准备完毕
         */
        void onPrepared();

        /**
         * 音乐播放完成
         */
        void onComplete();

        /**
         * 音乐停止播放
         */
        void onStop();
    }

    private String filePath;

    /**
     * 播放音乐
     * @param path 音乐文件路径
     * @param callback 播放回调函数
     */
    public void play(String path, final PlayCallback callback){
        this.filePath = path;
        this.callback = callback;
        if (isWiredHeadsetOn()){
            changeToHeadset();
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(path));
            mediaPlayer.prepare();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    callback.onPrepared();
                    mediaPlayer.start();
                }
            });
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    changeToSpeakerNotStop();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 切换到耳机模式
     */
    public void changeToHeadset(){
        audioManager.setSpeakerphoneOn(false);
    }

    /**
     * 耳机是否插入
     * @return 插入耳机返回true,否则返回false
     */
    @SuppressWarnings("deprecation")
    public boolean isWiredHeadsetOn(){
        return audioManager.isWiredHeadsetOn();
    }

    /**
     * 切换到听筒
     */
    public void changeToReceiver(){
        if (isPlaying()){
            stop();
            audioManager.setSpeakerphoneOn(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
            play(filePath, callback);
        } else {
            audioManager.setSpeakerphoneOn(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
            }
        }
    }

    /**
     * 切换到外放
     */
    public void changeToSpeaker(){
        if (PhoneModelUtil.isSamsungPhone() || PhoneModelUtil.isHuaweiPhone()){
            stop();
            changeToSpeakerNotStop();
            play(filePath, callback);
        } else {
            changeToSpeakerNotStop();
        }
    }

    public void changeToSpeakerNotStop(){
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    /**
     * 调大音量
     */
    public void raiseVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 调小音量
     */
    public void lowerVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    /**
     * 停止播放
     */
    public void stop(){
        if (isPlaying()){
            try {
                mediaPlayer.stop();
                callback.onStop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 是否正在播放
     * @return 正在播放返回true,否则返回false
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

}
