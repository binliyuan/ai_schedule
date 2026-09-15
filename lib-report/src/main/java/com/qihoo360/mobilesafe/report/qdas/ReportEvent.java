package com.qihoo360.mobilesafe.report.qdas;

public class ReportEvent {
    private final int code;
    private final String name;

    public ReportEvent(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
