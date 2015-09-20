package net.devwiki.playmode;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;

/**
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

    public interface PlayCallback{

        void onPrepared();

        void onComplete();

        void onStop();
    }

    private String filePath;

    public void play(String path, final PlayCallback callback){
        this.filePath = path;
        this.callback = callback;
        if (isWiredHeadsetOn()){
            changeToEarphoneForEarphone();
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
                    changeToSpeaker();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void changeToEarphoneForEarphone(){
        audioManager.setSpeakerphoneOn(false);
    }

    public boolean isWiredHeadsetOn(){
        return audioManager.isWiredHeadsetOn();
    }

    public void changeToEarphoneForSensor(){
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

    public void changeToSpeaker(){
        audioManager.setMode(AudioManager.MODE_NORMAL);
        audioManager.setSpeakerphoneOn(true);
    }

    public void raiseVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

    public void lowerVolume(){
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (currentVolume > 0) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
                    AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
        }
    }

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

    public boolean isPlaying(){
        if(mediaPlayer == null){
            return false;
        }
        return mediaPlayer.isPlaying();
    }

}
