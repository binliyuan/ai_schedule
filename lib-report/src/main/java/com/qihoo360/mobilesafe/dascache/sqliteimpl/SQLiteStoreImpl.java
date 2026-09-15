package com.qihoo360.mobilesafe.dascache.sqliteimpl;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.text.TextUtils;
import com.qihoo360.mobilesafe.dascache.StoreInterfaceLayer;
import com.qihoo360.mobilesafe.support.log.Logger;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/** 使用SQLite存储数据 */
public class SQLiteStoreImpl implements StoreInterfaceLayer {
    private static final String TAG = "SQLiteStoreImpl";
    private SQLiteDatabase sqLiteDatabase;
    private static volatile SQLiteStoreImpl instance;
    private QdasSQLiteManager qdasSQLiteManager;
    private Context mContext;

    public static SQLiteStoreImpl getInstance(Context context) {
        if (instance == null) {
            synchronized (SQLiteStoreImpl.class) {
                if (instance == null) {
                    instance = new SQLiteStoreImpl(context);
                }
            }
        }
        return instance;
    }

    private SQLiteStoreImpl(Context context) {
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        qdasSQLiteManager = QdasSQLiteManager.getInstance(context);
        try {
            sqLiteDatabase = qdasSQLiteManager.getWritableDatabase();
        } catch (Throwable e) {
            if (Logger.sDebug) {
                e.printStackTrace();
                Logger.d(TAG, "init sqLiteDatabase error=" + e);
            }
            // 重新获取
            try {
                sqLiteDatabase = qdasSQLiteManager.getWritableDatabase();
            } catch (Exception e1) {
                if (Logger.sDebug) {
                    e.printStackTrace();
                    Logger.d(TAG, "init sqLiteDatabase final error=" + e);
                }
            }
        }
    }

    @Override
    public void store(QdasEventModel model) {
        try {
            if (Logger.sDebug) {
                Logger.d(
                        TAG,
                        "store event_id="
                                + model.event_id
                                + " posid="
                                + model.posId
                                + ",ts="
                                + model.ts
                                + ",requestid="
                                + model.requestId
                                + ",count="
                                + model.count
                                + ",exts="
                                + model.exts);
            }
            if (model == null || TextUtils.isEmpty(model.posId)) {
                return;
            }

            ContentValues contentValues = new ContentValues();
            contentValues.put(QDASEntry.EVENT_POS_ID, model.posId);
            contentValues.put(QDASEntry.EVENT_TS, model.ts);
            if (!TextUtils.isEmpty(model.requestId)) {
                contentValues.put(QDASEntry.EVENT_REQUESTID, model.requestId);
            }
            contentValues.put(QDASEntry.EVENT_COUNT, model.count >= 1 ? model.count : 1);
            contentValues.put(QDASEntry.EVENT_ID, model.event_id);
            if (model.exts != null) {
                contentValues.put(QDASEntry.EVENT_ATTRS, model.exts.toString());
            }
            sqLiteDatabase.insert(QDASEntry.QDAS_TABLE_NAME, null, contentValues);

        } catch (Exception e) {
            if (Logger.sDebug) {
                e.printStackTrace();
                Logger.e(TAG, "store e=" + e);
            }
        }
    }

    // 获取当前所有
    @Override
    public List<QdasEventModel> fetchAll() {
        List<QdasEventModel> resultList = new ArrayList<>();

        // 记录非重复的
        List<String> eventList = new ArrayList<>();
        List<Integer> containsIndexList = new ArrayList<>();
        try {
            Cursor query =
                    sqLiteDatabase.query(
                            QDASEntry.QDAS_TABLE_NAME, null, null, null, null, null, null);
            if (query != null) {
                while (query.moveToNext()) {
                    String posid = query.getString(query.getColumnIndex(QDASEntry.EVENT_POS_ID));
                    long ts = query.getLong(query.getColumnIndex(QDASEntry.EVENT_TS));
                    String requestid =
                            query.getString(query.getColumnIndex(QDASEntry.EVENT_REQUESTID));
                    int count = query.getInt(query.getColumnIndex(QDASEntry.EVENT_COUNT));
                    int eventId = query.getInt(query.getColumnIndex(QDASEntry.EVENT_ID));
                    String exts = query.getString(query.getColumnIndex(QDASEntry.EVENT_ATTRS));

                    containsIndexList = new ArrayList<>();
                    if (eventList.contains(posid)) {
                        for (int i = 0; i < eventList.size(); i++) {
                            String id = eventList.get(i);
                            if (id.equals(posid)) {
                                containsIndexList.add(i);
                            }
                        }
                    }
                    if (!TextUtils.isEmpty(posid)) {
                        QdasEventModel model = new QdasEventModel();
                        model.posId = posid;
                        model.ts = ts;
                        model.requestId = requestid;
                        model.count = count;
                        model.event_id = eventId;
                        model.exts = new JSONObject(exts);
                        boolean bSetted = false;
                        if (containsIndexList.size() != 0) {
                            for (int i = 0; i < containsIndexList.size(); i++) {
                                int containsIndex = containsIndexList.get(i);
                                String containsExts = resultList.get(containsIndex).exts.toString();
                                int containsCount = resultList.get(containsIndex).count;
                                if (containsExts != null
                                        && exts != null
                                        && containsExts.equals(exts)) {
                                    QdasEventModel modelChanged = new QdasEventModel();
                                    modelChanged.posId = posid;
                                    modelChanged.ts = ts;
                                    modelChanged.requestId = requestid;
                                    modelChanged.count = (containsCount + count);
                                    modelChanged.exts = new JSONObject(exts);
                                    resultList.set(containsIndex, modelChanged);
                                    bSetted = true;
                                }
                            }
                        }
                        if (!bSetted) {
                            resultList.add(model);
                            eventList.add(posid);
                        }
                    }
                }
                query.close();
            }

            if (Logger.sDebug) {
                if (resultList != null) {
                    for (int i = 0; i < resultList.size(); i++) {
                        Logger.d(
                                TAG,
                                "get posid="
                                        + resultList.get(i).posId
                                        + ",ts="
                                        + resultList.get(i).ts
                                        + ",requestid="
                                        + resultList.get(i).requestId
                                        + ",count="
                                        + resultList.get(i).count
                                        + ",exts="
                                        + resultList.get(i).exts);
                    }
                }
                Logger.d(TAG, "fetchAll success resultList=" + resultList);
            }

        } catch (Exception e) {
            if (Logger.sDebug) {
                e.printStackTrace();
                Logger.e(TAG, "fetchAll error=" + e);
            }
        }

        return resultList;
    }

    @Override
    public void cleanExpiredValue() {
        long expireTime = 172800 * 1000L; // 2天(毫秒)
        if (Logger.sDebug) {
            Logger.d(TAG, "cleanExpiredValue");
        }
        long timeCurrent = System.currentTimeMillis();
        long timeTwoDaysBefore = timeCurrent - expireTime;
        sqLiteDatabase.delete(
                QDASEntry.QDAS_TABLE_NAME,
                QDASEntry.EVENT_TS + " < ?",
                new String[] {timeTwoDaysBefore + ""});
        sqLiteDatabase.delete(
                QDASEntry.QDAS_TABLE_NAME,
                QDASEntry.EVENT_TS + " > ?",
                new String[] {timeCurrent + ""});
        if (Logger.sDebug) {
            Logger.d(TAG, "cleanExpiredValue finish.");
        }
    }

    /** 清空数据 */
    @Override
    public void clean() {
        if (Logger.sDebug) {
            Logger.d(TAG, "clean");
        }

        try {
            sqLiteDatabase.execSQL("delete from " + QDASEntry.QDAS_TABLE_NAME);

            if (Logger.sDebug) {
                Logger.e(TAG, "clean() success");
            }
        } catch (Exception e) {
            if (Logger.sDebug) {
                e.printStackTrace();
                Logger.e(TAG, "clean error=" + e);
            }
        }
    }

    public static class QDASEntry implements BaseColumns {
        public static String QDAS_TABLE_NAME = "qdas_table";
        public static String EVENT_POS_ID = "posid";
        public static String EVENT_TS = "ts";
        public static String EVENT_REQUESTID = "requestid";
        public static String EVENT_COUNT = "count";
        public static String EVENT_ID = "event_id";
        public static String EVENT_ATTRS = "exts";
    }
}
