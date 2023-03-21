package com.v.log.stragety;


public interface DiskLogStrategy extends LogStrategy {
    String getLogPath();

    void writeCommonInfo();
}
