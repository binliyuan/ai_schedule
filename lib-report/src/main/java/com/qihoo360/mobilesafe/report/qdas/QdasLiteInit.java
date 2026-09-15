package com.qihoo360.mobilesafe.report.qdas;

public class QdasLiteInit {
    private static IQdasLiteEventProxy qdasLiteEventProxy;
    private static String newApkVersion = "unknown";

    public static String getNewApkVersion() {
        return newApkVersion;
    }

    public static void init(String newsApkVersion) {
        QdasLiteInit.newApkVersion = newsApkVersion;
    }

    // 委托
    public static void injectDelegator(IQdasLiteEventProxy qdasLiteEventProxy) {
        QdasLiteInit.qdasLiteEventProxy = qdasLiteEventProxy;
    }

    public static IQdasLiteEventProxy getQdasLiteEventProxy() {
        return qdasLiteEventProxy;
    }
}
