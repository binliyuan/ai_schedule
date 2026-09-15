package com.qihoo360.mobilesafe.stat;

import android.content.Context;

public class ReportSdkInit {

    public static int sBuketKey;
    private static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    public static void init(
            Context context,
            String pluginAppKey,
            String hostAppKey,
            String channel,
            String pluginPkgName,
            String appName,
            String versionName,
            String versionCode,
            int buketKey) {
        sBuketKey = buketKey;
        sContext = context;
    }

    public static Context getContext() {
        return sContext;
    }
}
