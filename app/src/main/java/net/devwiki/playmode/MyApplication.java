package net.devwiki.playmode;

import android.app.Application;
import android.content.Context;

/**
 * APP的Application
 * Created by DevWiki on 2015/9/16 0016.
 */
public class MyApplication extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }

    /**
     * 获取APP的Context方便其他地方调用
     * @return
     */
    public static Context getContext(){
        return context;
    }
}
