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

    private boolean isHandlingException = false; // 标志位


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

        if (isHandlingException) {
            // 如果已经在处理异常，直接退出避免循环
            System.exit(2);
            return;
        }

        isHandlingException = true;

        try {
            // 记录异常信息
            LogExtKt.logE(Log.getStackTraceString(exception),"AppException");
        } catch (Exception e) {
            // 处理日志记录中的任何异常，避免递归调用
            e.printStackTrace();
        }

        // 将异常传递给默认处理器
        if (defaultExceptionHandler != null) {
            defaultExceptionHandler.uncaughtException(thread, exception);
        } else {
            // 如果没有默认处理器，手动退出应用
            System.exit(2);
        }
    }

    public void init(Context context) {
        mContext = context;
        defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
}