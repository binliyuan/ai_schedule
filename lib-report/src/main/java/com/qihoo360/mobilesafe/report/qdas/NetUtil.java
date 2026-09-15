package com.qihoo360.mobilesafe.report.qdas;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import com.qihoo360.mobilesafe.support.log.Logger;

public class NetUtil {

    // 下面是联网类型的定义
    public static final byte TYPE_2G = 1;
    public static final byte TYPE_3G = 2;
    public static final byte TYPE_4G = 3;
    public static final byte TYPE_WIFI = 4;
    public static final byte TYPE_UNKNOWN = 5;
    public static final byte TYPE_DISCONNECT = TYPE_UNKNOWN;

    /** Network type is unknown */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /** Current network is GPRS */
    public static final int NETWORK_TYPE_GPRS = 1;
    /** Current network is EDGE */
    public static final int NETWORK_TYPE_EDGE = 2;
    /** Current network is UMTS */
    public static final int NETWORK_TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B */
    public static final int NETWORK_TYPE_CDMA = 4;
    /** Current network is EVDO revision 0 */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is 1xRTT */
    public static final int NETWORK_TYPE_1XRTT = 7;
    /** Current network is HSDPA */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    public static final int NETWORK_TYPE_HSPA = 10;
    /** Current network is iDen */
    public static final int NETWORK_TYPE_IDEN = 11;
    /** Current network is EVDO revision B */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /** Current network is LTE */
    public static final int NETWORK_TYPE_LTE = 13;
    /** Current network is eHRPD */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    public static final int NETWORK_TYPE_HSPAP = 15;
    /** Current network is GSM {@hide} */
    public static final int NETWORK_TYPE_GSM = 16;

    /** 网络是否已经连接 */
    public static boolean isConnected(Context c) {
        try {
            ConnectivityManager cm =
                    (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm != null) {
                NetworkInfo[] infos = cm.getAllNetworkInfo();
                if (infos != null) {
                    for (NetworkInfo ni : infos) {
                        if (ni.isConnected()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Throwable e) {
            //
        }
        return false;
    }

    /** WiFi 是否已经连接 */
    public static boolean isWifiConnected(Context c) {
        if (c == null) {
            return false;
        }
        NetworkInfo networkInfo = null;
        try {
            ConnectivityManager connecManager =
                    (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
            networkInfo = connecManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        } catch (Throwable ex) {
        }
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            return false;
        }
    }

    /** 查询当前联网类型，是 WiFi, 2G 还是 3G */
    public static byte getConnectionType(Context c) {
        if (c == null) {
            return TYPE_UNKNOWN;
        }

        if (!isConnected(c)) {
            return TYPE_DISCONNECT;
        }

        if (isWifiConnected(c)) {
            return TYPE_WIFI;
        }

        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                int networkType = telephonyManager.getNetworkType();
                switch (networkType) {
                    case NETWORK_TYPE_GPRS:
                    case NETWORK_TYPE_GSM:
                    case NETWORK_TYPE_EDGE:
                    case NETWORK_TYPE_CDMA:
                    case NETWORK_TYPE_1XRTT:
                    case NETWORK_TYPE_IDEN:
                        return TYPE_2G;
                    case NETWORK_TYPE_UMTS:
                    case NETWORK_TYPE_EVDO_0:
                    case NETWORK_TYPE_EVDO_A:
                    case NETWORK_TYPE_HSDPA:
                    case NETWORK_TYPE_HSUPA:
                    case NETWORK_TYPE_HSPA:
                    case NETWORK_TYPE_EVDO_B:
                    case NETWORK_TYPE_EHRPD:
                    case NETWORK_TYPE_HSPAP:
                        return TYPE_3G;
                    case NETWORK_TYPE_LTE:
                        return TYPE_4G;
                    default:
                        return TYPE_UNKNOWN;
                }
            }
        } catch (Throwable e) {
            if (Logger.sDebug) {
                Logger.handleException(e);
            }
        }

        return TYPE_UNKNOWN;
    }

    public static String getNetTypeString(Context c) {
        int netType = getNetTypeInt(c);
        return String.valueOf(netType);
    }

    public static int getNetTypeInt(Context c) {
        int netType = 99;
        byte connectionType = NetUtil.getConnectionType(c);
        if (connectionType == NetUtil.TYPE_2G) {
            netType = 1;
        } else if (connectionType == NetUtil.TYPE_3G) {
            netType = 2;
        } else if (connectionType == NetUtil.TYPE_4G) {
            netType = 3;
        } else if (connectionType == NetUtil.TYPE_WIFI) {
            netType = 4;
        } else {
            netType = 5;
        }
        return netType;
    }

    public static int getNetTypeIntTT(Context c) {
        int netType = 0;
        byte connectionType = NetUtil.getConnectionType(c);
        if (connectionType == NetUtil.TYPE_2G) {
            netType = 1;
        } else if (connectionType == NetUtil.TYPE_3G) {
            netType = 2;
        } else if (connectionType == NetUtil.TYPE_4G) {
            netType = 3;
        } else if (connectionType == NetUtil.TYPE_WIFI) {
            netType = 4;
        } else {
            netType = 0;
        }
        return netType;
    }

    public static int getNetTypeIntTTV2(Context c) {
        int netType = 0;
        byte connectionType = NetUtil.getConnectionType(c);
        if (connectionType == NetUtil.TYPE_2G) {
            netType = 2;
        } else if (connectionType == NetUtil.TYPE_3G) {
            netType = 3;
        } else if (connectionType == NetUtil.TYPE_4G) {
            netType = 4;
        } else if (connectionType == NetUtil.TYPE_WIFI) {
            netType = 1;
        } else {
            netType = 0;
        }
        return netType;
    }

    /**
     * unknow、wifi、mobile、3g、4g
     *
     * @param c
     * @return
     */
    public static String getNetTypeName(Context c) {
        String netType = "unknow";
        byte connectionType = NetUtil.getConnectionType(c);
        if (connectionType == NetUtil.TYPE_2G) {
            netType = "2g";
        } else if (connectionType == NetUtil.TYPE_3G) {
            netType = "3g";
        } else if (connectionType == NetUtil.TYPE_4G) {
            netType = "4g";
        } else if (connectionType == NetUtil.TYPE_WIFI) {
            netType = "wifi";
        } else {
            netType = "unknow";
        }
        return netType;
    }

    public static int getCarrier() {
        return 4;
    }
}
