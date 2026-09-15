package com.qihoo360.mobilesafe.report.qdas;

import android.content.Context;

public interface IQdasLiteEventProxy {
    void onEvent(Context context, String posid, String requestid, ReportEvent adxReportEvent);
}
