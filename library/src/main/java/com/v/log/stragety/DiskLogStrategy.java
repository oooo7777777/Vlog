package com.v.log.stragety;


public interface DiskLogStrategy extends LogStrategy {
    String getLogPath();

    void writeCommonInfo();

    void log(int priority, String onceOnlyTag, String message, Boolean save, Boolean show, Boolean beautify, Boolean detailed);
}
