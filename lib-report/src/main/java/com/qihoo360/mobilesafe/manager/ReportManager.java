package com.qihoo360.mobilesafe.manager;

import android.util.Log;
import java.util.Random;

/** 中台打点特殊逻辑管理功能设计 */
public class ReportManager {

    private static final String TAG = "ReportManager";

    private static boolean sDebug = false;

    // 二十分之一取样
    private static final int sValuePercentage = 20;

    private static boolean bInit = false;

    private static int sRandomKey = -1;

    // 是否命中抽样
    private static boolean bHitted = false;

    /** 设置一些初始化参数(取样等) */
    public static void init(boolean isDebug) {
        int averageValue = 0;
        if (!bInit) {
            sDebug = isDebug;
            // 初始化抽样统计Key
            sRandomKey = new Random(System.currentTimeMillis()).nextInt(sValuePercentage);
            averageValue = (int) (sValuePercentage / 2);
            if (sRandomKey == averageValue) {
                bHitted = true;
            } else {
                bHitted = false;
            }
        }
        bInit = true;
        if (sDebug) {
            Log.d(
                    TAG,
                    "ReportManager init "
                            + " sRandomKey = "
                            + sRandomKey
                            + " value = "
                            + averageValue);
            Log.d(TAG, "ReportManager init bHitted: " + bHitted);
        }
    }

    public static boolean isSamplingHit() {
        return bHitted;
    }
}
