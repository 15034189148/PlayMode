package net.devwiki.playmode;

import android.os.Build;

/**
 * 手机型号工具类
 * Created by Administrator on 2015/9/20 0020.
 */
public class PhoneModelUtil {

    public static String getPhoneModel(){
        return Build.MODEL;
    }
}
