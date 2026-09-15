package com.qihoo360.mobilesafe.dascache;

import com.qihoo360.mobilesafe.dascache.sqliteimpl.QdasEventModel;
import java.util.List;

/** 中间层 key格式：functionCode + "|" + type + "|" + value; value格式: long */
public interface StoreInterfaceLayer {

    void store(QdasEventModel model);

    // 获取所有
    List<QdasEventModel> fetchAll();

    // 删除过期数据
    void cleanExpiredValue();

    // 置空
    void clean();
}
