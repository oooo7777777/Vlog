package com.v.log.stragety;

import android.util.Log;

import com.v.log.VLog;

public class LogcatLogStrategy implements LogStrategy {

    @Override
    public void log(int priority, String tag, String message, Boolean save) {
        //打印日志
        if (VLog.isShowLog()) {
            Log.println(priority, tag, message);
        }
    }

    @Override
    public void flush() {

    }

}
