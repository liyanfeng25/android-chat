package cn.wildfire.chat.app.utils;

import android.util.Log;

public class LogHelper {

    private static boolean isLog = true;

    public static void d(String msg) {

        if (isLog) {

            Log.d("YuJianChat", msg);
        }
    }

    public static void isOpen(boolean isOpen) {

        isLog = isOpen;
    }

    public static void debug(String msg) {

        if (isLog) {

            Log.d("YuJianChat", msg);
        }
    }
}
