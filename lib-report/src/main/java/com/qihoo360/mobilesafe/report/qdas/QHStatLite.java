package com.qihoo360.mobilesafe.report.qdas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import com.qihoo360.mobilesafe.dascache.sqliteimpl.QdasEventModel;
import com.qihoo360.mobilesafe.report.qdas.common.CommonUtil;
import com.qihoo360.mobilesafe.report.qdas.common.JSONUtil;
import com.qihoo360.mobilesafe.stat.ReportEnv;
import com.qihoo360.mobilesafe.support.HttpConnectionHelper;
import com.qihoo360.mobilesafe.support.Response;
import com.qihoo360.mobilesafe.support.log.Logger;
import java.io.Reader;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

public class QHStatLite {
    /** 当前SDK版本 */
    public static final String sdkVersion = "2.4.14lite";
    /** Log输出Tag */
    public static final String TAG = "QHStatLite";

    private static final boolean DEBUG = ReportEnv.DEBUG;

    public String serverUrl = "https://testtk.shouji.360.cn/v1/api/sdk/track";

    public String serverUpdateUrl = "https://testtk.shouji.360.cn/v1/api/sdk/track";

    private final String _appkey, _versionName, sharedPrefsName;
    private final Map<String, String> _header, _headerExt;
    private final WeakReference<Context> contextWeakReference;
    private JSONArray arrayEvent = new JSONArray();

    private static String HW_UUID = null;
    private static String SP_NAME = "360sp";
    private static String SP_UUID = "uuid";
    private static String DEFAULT_UUID = "default_hw_uuid";
    private String uploadAndroidId =
            null; // 本次上报的AndroidID，仅当满足上传间隔，从设备标识SDK获取的才会保存。另外该值也影响SharedPreferences-LastSendTime的写入
    private boolean isFirstAtSession = true;

    /**
     * 初始化统计SDK
     *
     * @param context the context
     * @param appkey appkey
     * @param versionName 应用的版本号
     * @param header header的补充信息，使用前请与数据中心李宠波联系确认
     * @param headerExt header_ext的补充信息，使用前请与数据中心李宠波联系确认
     */
    public QHStatLite(
            Context context,
            String appkey,
            String versionName,
            Map<String, String> header,
            Map<String, String> headerExt) {
        contextWeakReference = new WeakReference<Context>(context.getApplicationContext());

        this._appkey = appkey;
        this._versionName = versionName;
        this.sharedPrefsName = "QH_SDK_UserData" + appkey;
        this._header = header;
        this._headerExt = headerExt;
    }

    public void onEvent(final QdasEventModel model) {
        if (DEBUG) {
            Log.d(TAG, "onEvent: event_id " + model.event_id);
        }
        JSONObject jo = getEventObject(model, System.currentTimeMillis());
        if (jo != null) {
            //            synchronized (QHStatLite.class) {
            arrayEvent.put(jo);
            //            }
        }
    }

    private static JSONObject getEventObject(QdasEventModel model, long currentTime) {
        JSONObject localJSONObject = new JSONObject();
        try {
            localJSONObject.put("time", currentTime);
            localJSONObject.put("pos_id", model.event_id);
            localJSONObject.put("count", model.count);
            localJSONObject.put("request_id", model.requestId);
            localJSONObject.put("event_id", model.event_id);
            localJSONObject.put("ext", model.exts);
        } catch (Throwable ignored) {
            if (Logger.sDebug) {
                ignored.printStackTrace();
            }
            return null;
        }
        return localJSONObject;
    }

    /**
     * 立即上报数据
     *
     * @param callback the callback
     */
    public synchronized void upload(final UploadCallback callback) {
        if (DEBUG) {
            Log.d(TAG, "upload:");
        }

        final Context ctx = contextWeakReference.get();

        try {
            if (arrayEvent.length() <= 0) {
                callback.onCompleted(false);
                return;
            }
            // 生成最终JSON对象
            final JSONObject object = new JSONObject();
            object.put("event", arrayEvent);
            try {
                // 生成Header
                JSONObject header = getHeader(ctx);
                if (DEBUG) {
                    Log.d(TAG, "upload, header:" + header.toString());
                }
                object.put("header", header);

                // 立即上传
                callback.onCompleted(send(ctx, object));
            } catch (Throwable e) {
                e.printStackTrace();
                callback.onCompleted(false);
            }
        } catch (Throwable e) {
            if (Logger.sDebug) {
                e.printStackTrace();
            }
            callback.onCompleted(false);
        } finally {
            clearCache();
        }
    }

    public void clearCache() {
        //        synchronized (QHStatLite.class) {
        arrayEvent = new JSONArray();
        //        }
    }

    private boolean send(Context context, JSONObject data) {
        try {
            String sendData = data.toString();
            Log.d(TAG, "QHStatLite upload data: " + sendData);
            if (DEBUG) {
                Log.d(TAG, "sendData" + sendData);
            }
            String path = isFirstAtSession ? serverUpdateUrl : serverUrl;

            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                headerMap.put("Connection", "close");
            }

            Response response =
                    HttpConnectionHelper.doPostRawBody(
                            context, path, headerMap, sendData, 45 * 1000, 60 * 1000);
            boolean result = response != null && response.isSuccessful();

            if (DEBUG) {
                Log.d(TAG, "responseCode" + (response != null ? response.getCode() : -1));
            }

            if (result) {
                isFirstAtSession = false;
                if (uploadAndroidId != null) {
                    setCurrentTime(context, "LastSendTime");
                    put(context, sharedPrefsName, "LastAID", uploadAndroidId);
                }
            }
            return result;
        } catch (Throwable e) {
            if (DEBUG) {
                Log.d(TAG, "e", e);
            }
        }
        return false;
    }

    /**
     * Returns true if {@code e} is due to a firmware bug fixed after Android 4.2.2.
     * https://code.google.com/p/android/issues/detail?id=54072
     * https://github.com/square/okhttp/issues/1684 https://github.com/square/okhttp/pull/1817
     */
    private static boolean isAndroidGetsocknameError(AssertionError e) {
        return e.getCause() != null
                && e.getMessage() != null
                && e.getMessage().contains("getsockname failed");
    }

    private JSONObject headerCache;

    /**
     * 生成Header数据
     *
     * @param context the context
     * @return JSONObject格式的Header对象
     */
    public JSONObject getHeader(Context context) {
        try {
            if (headerCache != null) {
                // 更新参数
                if (TextUtils.isEmpty(headerCache.optString("m1", "")))
                    headerCache.put("m1", "0123456789012345678912");
                // TI
                headerCache.put("ti", CommonUtil.getTransferId());
                // CT
                headerCache.put("ct", System.currentTimeMillis());
            } else {
                headerCache = new JSONObject();
                JSONUtil.setData(headerCache, "mo", PrivacyDataHelper.model());
                JSONUtil.setData(headerCache, "sv", QHStatLite.sdkVersion);
                headerCache.put("ti", CommonUtil.getTransferId()); // tid   TODO
                headerCache.put("os", "android");
                headerCache.put("ov", PrivacyDataHelper.os_ver());
                headerCache.put("ct", System.currentTimeMillis());
                headerCache.put("co", PrivacyDataHelper.getCountry());
                headerCache.put("n", CommonUtil.getAppName(context));
                headerCache.put("ne", CommonUtil.getNetworkType(context));
                headerCache.put("mf", PrivacyDataHelper.manufacturer());
                headerCache.put("br", PrivacyDataHelper.brand());
                headerCache.put("la", PrivacyDataHelper.getLanguage());
                headerCache.put("pa", context.getPackageName());
                headerCache.put("k", _appkey);

                headerCache.put("vn", _versionName);

                JSONUtil.setExtData(headerCache, "p", sdkVersion);

                if (_header != null) {
                    for (String key : _header.keySet()) {
                        headerCache.put(key, _header.get(key));
                    }
                }
                if (_headerExt != null) {
                    for (String key : _headerExt.keySet()) {
                        JSONUtil.setExtData(headerCache, key, _headerExt.get(key));
                    }
                }
            }
        } catch (Throwable e) {
            if (DEBUG) {
                Log.d(TAG, "e", e);
            }
        }

        uploadAndroidId = null;
        return headerCache;
    }

    /**
     * 保存当前时间
     *
     * @param context the context
     * @param key 类型
     */
    private void setCurrentTime(Context context, String key) {
        long currentTimeMillis = System.currentTimeMillis();
        put(context, sharedPrefsName, key, currentTimeMillis);
    }

    @SuppressLint("InlinedApi")
    private static SharedPreferences getSharedPreferences(Context context, String fileName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return context.getApplicationContext()
                    .getSharedPreferences(fileName, Context.MODE_MULTI_PROCESS);
        } else {
            return context.getApplicationContext()
                    .getSharedPreferences(fileName, Context.MODE_PRIVATE);
        }
    }

    /**
     * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
     *
     * @param context the context
     * @param key 要保存的Key
     * @param value 要保存的值
     */
    private static void put(Context context, String fileName, String key, Object value) {
        try {
            SharedPreferences.Editor editor = getSharedPreferences(context, fileName).edit();
            if (value == null) {
                editor.putString(key, null);
            } else if (value instanceof Long) editor.putLong(key, (Long) value);
            else editor.putString(key, value.toString());
            editor.commit();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     *
     * @param context the context
     * @param key 要获取的Key
     * @param defaultObject 默认值
     * @return 取到的值，如果Key不存在，则返回defaultObject
     */
    private static long getLong(Context context, String fileName, String key, long defaultObject) {
        try {
            return getSharedPreferences(context, fileName).getLong(key, defaultObject);
        } catch (Throwable e) {
            e.printStackTrace();
            return defaultObject;
        }
    }

    private static String loadReaderAsString(Reader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        char[] buffer = new char[4096];
        int readLength = reader.read(buffer);
        while (readLength >= 0) {
            builder.append(buffer, 0, readLength);
            readLength = reader.read(buffer);
        }
        return builder.toString();
    }

    public interface UploadCallback {
        void onCompleted(boolean successful);
    }

    /**
     * 获取UUID
     *
     * @param context
     * @return
     */
    public static synchronized String getHwUUID(Context context) {
        if (HW_UUID == null) {
            try {
                String saveUid = readUUID(context);
                if (TextUtils.isEmpty(saveUid)) {
                    UUID uuid = UUID.randomUUID();
                    saveUid = uuid.toString();
                    writeUUID(context, saveUid);
                }
                HW_UUID = saveUid;

            } catch (Throwable e) {
                HW_UUID = DEFAULT_UUID;
            }
        }

        return HW_UUID;
    }

    private static synchronized String readUUID(Context context) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            if (sp != null) {
                return sp.getString(SP_UUID, "");
            }
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        return DEFAULT_UUID;
    }

    private static synchronized boolean writeUUID(Context context, String uuid) {
        try {
            SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
            if (sp != null) {
                sp.edit().putString(SP_UUID, uuid).apply();
                return true;
            }
        } catch (Throwable e) {
            if (DEBUG) {
                Log.e(TAG, "", e);
            }
        }

        return false;
    }
}
