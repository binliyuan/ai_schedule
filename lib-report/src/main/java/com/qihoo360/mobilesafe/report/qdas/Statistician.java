package com.qihoo360.mobilesafe.report.qdas;

import android.content.Context;
import com.qihoo360.mobilesafe.dascache.StoreInterfaceLayer;
import com.qihoo360.mobilesafe.dascache.sqliteimpl.QdasEventModel;
import com.qihoo360.mobilesafe.dascache.sqliteimpl.SQLiteStoreImpl;
import java.util.List;

public class Statistician {

    public static void logDirectly(Context context, QdasEventModel model) {
        getStore(context).store(model);
    }

    private static StoreInterfaceLayer getStore(Context context) {
        return SQLiteStoreImpl.getInstance(context);
    }

    public static void resetLog(Context context) {
        getStore(context).clean();
    }

    public static List<QdasEventModel> getCache(Context context) {
        return getStore(context).fetchAll();
    }

    public static void clearExpiredValue(Context context) {
        getStore(context).cleanExpiredValue();
    }
}
