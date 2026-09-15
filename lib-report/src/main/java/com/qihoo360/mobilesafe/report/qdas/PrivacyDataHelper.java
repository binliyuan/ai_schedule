package com.qihoo360.mobilesafe.report.qdas;

import android.content.Context;
import android.os.Build;
import com.qihoo360.mobilesafe.stat.ReportEnv;
import java.util.Locale;

public class PrivacyDataHelper {
    private static final boolean HW_COMPILE = false;

    public static final boolean DEBUG = ReportEnv.DEBUG;

    private static final String TAG = "PrivacyDataHelper";

    private static byte[] mWid = null;

    private static final byte[] wid_hexArray = {
        0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e,
        0x0f
    };

    private static final String DEFAULT_IMEI = "360_DEFAULT_IMEI";

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    static synchronized byte[] getWid(Context ctx) {
        return getWidImpl(ctx);
    }

    public static int sdk_int() {
        return Build.VERSION.SDK_INT;
    }

    static String os_ver() {
        return Build.VERSION.RELEASE;
    }

    static String manufacturer() {
        return Build.MANUFACTURER;
    }

    public static String model() {
        return Build.MODEL;
    }

    static String brand() {
        return Build.BRAND;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    private static synchronized byte[] getWidImpl(Context ctx) {
        if (HW_COMPILE) {
            return wid_hexArray;
        }
        if (mWid != null) {
            return mWid;
        }
        mWid = wid_hexArray;
        return mWid;
    }

    static String getCountry() {
        return Locale.getDefault().getCountry();
    }

    public static String getLanguage() {
        return Locale.getDefault().getLanguage();
    }

    private static byte[] md5_impl(byte[] bytes) {
        return Utils.MD5(bytes);
    }

    /**
     * 获取m1
     *
     * @param context
     * @return m1=md5(imei)
     */
    public static String getM1(Context context) {
        return "";
    }
}
