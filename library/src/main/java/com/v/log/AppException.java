package com.v.log;

import android.content.Context;
import android.util.Log;

import com.v.log.util.LogExtKt;

/**
 * author  : ww
 * desc    :
 * time    : 2023/11/29 17:36
 */
public class AppException implements Thread.UncaughtExceptionHandler {

    private Context mContext;

    private Thread.UncaughtExceptionHandler defaultExceptionHandler;

    // 单例声明CustomException;
    private static AppException appException;

    private AppException() {
    }

    public static AppException getInstance() {
        if (appException == null) {
            appException = new AppException();
        }
        return appException;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {
        LogExtKt.logE(exception.getLocalizedMessage());
        // 将异常抛出，应用会弹出异常对话框
        defaultExceptionHandler.uncaughtException(thread, exception);

    }

    public void init(Context context) {
        mContext = context;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
}