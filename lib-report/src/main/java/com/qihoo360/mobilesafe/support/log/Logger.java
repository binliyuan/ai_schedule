package com.qihoo360.mobilesafe.support.log;

import android.util.Log;

public class Logger {

    public static boolean sDebug = false;

    public static void d(String tag, String msg) {
        if (sDebug) Log.d(tag, msg);
    }

    public static void e(String tag, String msg) {
        Log.e(tag, msg);
    }

    public static void handleException(Throwable t) {
        if (sDebug) Log.e("Logger", "Exception", t);
    }
}
