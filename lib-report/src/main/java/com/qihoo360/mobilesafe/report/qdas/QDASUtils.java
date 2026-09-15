package com.qihoo360.mobilesafe.report.qdas;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.qihoo360.mobilesafe.dascache.sqliteimpl.QdasEventModel;
import com.qihoo360.mobilesafe.stat.ReportEnv;
import com.qihoo360.mobilesafe.support.log.Logger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

public class QDASUtils {
    private static final boolean DEBUG = ReportEnv.DEBUG;

    private static final String TAG = DEBUG ? "QDASUtils" : QDASUtils.class.getSimpleName();
    private static final String WX_APP_Key = "16232056792344c3a07ddd747af8cf80";

    public static String version;

    public static void reportNow(final Context context) {
        if (-1 == getCID()) return;
        upload(context);
    }

    public static void upload(final Context context) {
        if (DEBUG) {
            Log.d(TAG, "start upload data");
        }

        if (!NetUtil.isConnected(context)) {
            if (DEBUG) {
                Log.d(TAG, "not connected.");
            }
            return;
        }

        try {
            Statistician.clearExpiredValue(context);

            List<QdasEventModel> all = Statistician.getCache(context);

            if (all == null || all.size() == 0) {
                if (DEBUG) {
                    Log.d(TAG, "local data is empty");
                }
                return;
            }

            String versionName = QdasLiteInit.getNewApkVersion();
            String cidInt = String.valueOf(getCID());
            Map<String, String> header = null;
            Map<String, String> headerExt = null;

            QHStatLite lite = new QHStatLite(context, WX_APP_Key, versionName, header, headerExt);
            try {
                for (QdasEventModel model : all) {
                    if (model != null && !TextUtils.isEmpty(model.posId)) {
                        if (model != null) {
                            lite.onEvent(model);
                        } else {
                            HashMap<String, String> attrs = new HashMap<>();
                            JSONObject attrsJson = model.exts;
                            Iterator<String> ks = attrsJson.keys();
                            while (ks.hasNext()) {
                                String k = ks.next();
                                String v = attrsJson.optString(k);
                                if (!TextUtils.isEmpty(k) && !TextUtils.isEmpty(v)) {
                                    attrs.put(k, v);
                                }
                            }
                            lite.onEvent(model);
                        }
                    }
                }
            } catch (Exception e) {
                Statistician.resetLog(context);
                if (Logger.sDebug) {
                    Log.e(TAG, "", e);
                }
            }

            lite.upload(
                    new QHStatLite.UploadCallback() {
                        @Override
                        public void onCompleted(boolean successful) {
                            if (DEBUG) {
                                Log.d(TAG, "upload successful: " + successful);
                            }
                            if (successful) {
                                Statistician.resetLog(context);
                            }
                        }
                    });
        } catch (Exception e) {
            if (Logger.sDebug) {
                Logger.e(TAG, "upload error=" + e);
            }
        }
    }

    public static Map<String, String> getQdasLiteHeader(Context context) {
        Map<String, String> header = new HashMap<>();
        String wid = Utils.bytesToHexString(PrivacyDataHelper.getWid(context));
        header.put("m1", PrivacyDataHelper.getM1(context));
        header.put("m2", wid);
        return header;
    }

    public static Map<String, String> getQdasLiteHeaderEx(Context context) {
        String wid = Utils.bytesToHexString(PrivacyDataHelper.getWid(context));
        Map<String, String> headerExt = new HashMap<>();
        headerExt.put("uuID", QHStatLite.getHwUUID(context));
        headerExt.put("UniqueId", wid);
        headerExt.put("mid", wid);
        return headerExt;
    }

    private static int sCid = 20200317;

    private static int getCID() {
        return sCid;
    }
}
