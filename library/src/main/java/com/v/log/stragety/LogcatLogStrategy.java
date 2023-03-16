package com.v.log.stragety;

import android.util.Log;

import com.v.log.VLog;
import com.v.log.config.ConfigCenter;

public class LogcatLogStrategy implements LogStrategy {

    @Override
    public void log(int priority, String tag, String message, Boolean save) {
        //打印日志
        if (ConfigCenter.getInstance().getShowLog()) {
            Log.println(priority, tag, message);
        }
    }

    @Override
    public void flush() {

    }

}
