package com.qihoo360.mobilesafe.report.qdas.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

@SuppressLint("SimpleDateFormat")
public class CommonUtil {
    public static final String TAG = "CommonUtil";

    /**
     * checkPermissions
     *
     * @param context the context
     * @param permission permission
     * @return true or false
     */
    public static boolean checkPermissions(Context context, String permission) {
        PackageManager localPackageManager = context.getPackageManager();
        return localPackageManager.checkPermission(permission, context.getPackageName())
                == PackageManager.PERMISSION_GRANTED;
    }

    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    // private static final int NETWORK_TYPE_MOBILE = -100;
    private static final int NETWORK_TYPE_WIFI = -101;

    public static final int NETWORK_CLASS_WIFI = -101;
    public static final int NETWORK_CLASS_UNAVAILABLE = -1;
    /** Unknown network class. */
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    /** Class of broadly defined "2G" networks. */
    public static final int NETWORK_CLASS_2_G = 1;
    /** Class of broadly defined "3G" networks. */
    public static final int NETWORK_CLASS_3_G = 2;
    /** Class of broadly defined "4G" networks. */
    public static final int NETWORK_CLASS_4_G = 3;

    //    public static String getOperatorName(String operator){
    //    	if(operator!=null){
    //            if(operator.equalsIgnoreCase("46000") || operator.equalsIgnoreCase("46002") ||
    // operator.equalsIgnoreCase("46007")){
    //                return "中国移动";
    //            }else if (operator.equalsIgnoreCase("46001")){
    //                return "中国联通";
    //            }else if (operator.equalsIgnoreCase("46003")){
    //                return "中国电信";
    //            }
    //    	}
    //    	return operator;
    //    }

    @SuppressLint("MissingPermission")
    public static int getNetworkType(Context context) {
        int networkType = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        try {
            final NetworkInfo network =
                    ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                            .getActiveNetworkInfo();
            if (network != null && network.isAvailable() && network.isConnected()) {
                int type = network.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {
                    networkType = NETWORK_TYPE_WIFI;
                } else if (type == ConnectivityManager.TYPE_MOBILE) {
                    TelephonyManager telephonyManager =
                            (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    networkType = telephonyManager.getNetworkType();
                }
            } else {
                networkType = NETWORK_TYPE_UNAVAILABLE;
            }

        } catch (Throwable e) {
            CommonUtil.printError(TAG, "", e);
        }
        return networkType;
    }

    /**
     * 获取WIFI的BSSID
     *
     * @param context the context
     * @return 如果取不到则为Null，否则为当前已连接的WIFI的BSSID
     */
    //	public static String getWifiBSSID(Context context){
    //		try {
    //			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    //			if (wifiManager == null)
    //				return null;
    //			WifiInfo connectionInfo = wifiManager.getConnectionInfo();
    //			if (connectionInfo != null) {
    //				return connectionInfo.getBSSID();
    //			}
    //		}catch (Throwable e){
    //			printError(TAG, "", e);
    //		}
    //		return null;
    //	}

    /**
     * 压缩字符串，然后返回字节数组
     *
     * @param str 要压缩的字符串
     * @return 压缩后的字节数组
     * @throws Exception 压缩过程发生的异常
     */
    public static byte[] compressToBytes(String str) throws Exception {
        try {
            if (str == null || str.length() == 0) {
                return new byte[0];
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(getBytes(str));
            gzip.finish();
            gzip.close();
            // out.flush();
            byte[] bytes = out.toByteArray();
            out.close();
            return bytes;
        } catch (OutOfMemoryError e) {
            throw new Exception("OutOfMemoryError");
        } catch (InternalError e) {
            throw new Exception("InternalError");
        } catch (StackOverflowError e) {
            throw new Exception("StackOverflowError");
        }
    }

    /**
     * 压缩字符串，然后返回Base64格式的字节数组
     *
     * @param str 要压缩的字符串
     * @return 压纹后的Base64字节数组
     * @throws Exception 压缩过程发生的异常
     */
    public static byte[] compressToBase64(String str) throws Exception {
        return Base64.encode(compressToBytes(str), Base64.NO_WRAP);
    }

    /**
     * 压缩字符串，然后返回Base64格式的字符串
     *
     * @param str 要压缩的字符串
     * @return 压纹后的Base64字符串
     * @throws Exception 压缩过程发生的异常
     */
    public static String compress(String str) throws Exception {
        return CommonUtil.getString(compressToBase64(str));
    }

    private static long seqId = 0;
    /**
     * get Transfer Id
     *
     * @return TransferId
     */
    public static String getTransferId() {
        return (String.valueOf(System.currentTimeMillis()) + "" + String.valueOf(seqId++));
    }

    /**
     * Get the Application Label of the current program
     *
     * @param context the context
     * @return applicationName
     */
    public static String getAppName(Context context) {
        try {
            // ---get the package info---
            PackageManager pm = context.getPackageManager();
            ApplicationInfo ai = pm.getApplicationInfo(context.getPackageName(), 0);
            return (String) pm.getApplicationLabel(ai);
        } catch (Throwable e) {
            CommonUtil.printError(TAG, "AppName", e);
        }
        return "";
    }

    /**
     * Set the output log
     *
     * @param tag the tag
     * @param log the log
     */
    public static void printLog(String tag, String log) {
        try {
            //			if (QHStatLite.isLoggingEnabled()) {
            //				Log.d("QHStatLite", "[" + tag + "] " + log);
            //			}
        } catch (Throwable e) {

        }
    }

    public static void printError(String tag, String msg, Throwable throwable) {
        try {
            if (!tag.startsWith("QHStatLite")) tag = "QHStatLite_" + tag;
            //			Log.e(tag, QHStatLite.sdkVersion + " " + msg, throwable);
            throwable.printStackTrace();
        } catch (Throwable e) {

        }
    }

    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException ioe) {
            // ignore
        }
    }

    /**
     * 获取标志位是否为真(1)。
     *
     * @param flagName 从右数的位数，从0开始。如最右边的标志为0
     * @return 如果大于63，则直接返回false
     */
    public static boolean getFlag(long number, int flagName) {
        if (flagName > 63) {
            return false;
        }
        long num = 1;
        num = num << flagName;
        return (number & num) > 0;
    }

    public static String getString(byte[] bytes) {
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return new String(bytes);
        }
    }

    public static byte[] getBytes(String str) {
        try {
            return str.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return str.getBytes();
        }
    }
}
