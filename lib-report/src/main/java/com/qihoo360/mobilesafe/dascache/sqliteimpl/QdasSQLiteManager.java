package com.qihoo360.mobilesafe.dascache.sqliteimpl;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.qihoo360.mobilesafe.support.log.Logger;

/** QdasSqliteManager */
public class QdasSQLiteManager extends MySqliteHelper {
    public static final int version = 6;
    public static final String DATA_BASE_NAME = "qdas_data_base";

    private static final String SQL_CREATE_ENTRIES_V2 =
            "CREATE TABLE "
                    + SQLiteStoreImpl.QDASEntry.QDAS_TABLE_NAME
                    + " ("
                    + SQLiteStoreImpl.QDASEntry._ID
                    + " INTEGER PRIMARY KEY, "
                    + SQLiteStoreImpl.QDASEntry.EVENT_POS_ID
                    + " TEXT,"
                    + SQLiteStoreImpl.QDASEntry.EVENT_TS
                    + " INTEGER,"
                    + SQLiteStoreImpl.QDASEntry.EVENT_REQUESTID
                    + " TEXT,"
                    + SQLiteStoreImpl.QDASEntry.EVENT_COUNT
                    + " INTEGER,"
                    + SQLiteStoreImpl.QDASEntry.EVENT_ID
                    + " TEXT,"
                    + SQLiteStoreImpl.QDASEntry.EVENT_ATTRS
                    + " TEXT)";

    private static QdasSQLiteManager instance;

    public static QdasSQLiteManager getInstance(Context context) {
        if (instance == null) {
            synchronized (QdasSQLiteManager.class) {
                if (instance == null) {
                    instance = new QdasSQLiteManager(context);
                }
            }
        }
        return instance;
    }

    public QdasSQLiteManager(Context context) {
        super(context, DATA_BASE_NAME, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建table
        db.execSQL(SQL_CREATE_ENTRIES_V2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("drop table if exists " + SQLiteStoreImpl.QDASEntry.QDAS_TABLE_NAME);
            onCreate(db);
        } catch (Exception e) {
            if (Logger.sDebug) {
                e.printStackTrace();
                Logger.e("QdasSQLiteManager", "onUpgrade e: " + e);
            }
        }
    }
}
