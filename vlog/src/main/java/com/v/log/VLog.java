package com.v.log;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.v.log.Printer.AndroidLogPrinter;
import com.v.log.Printer.DiskLogPrinter;
import com.v.log.Printer.Printer;
import com.v.log.config.ConfigCenter;
import com.v.log.logger.ALogger;
import com.v.log.logger.Logger;
import com.v.log.util.NetworkManager;
import com.orhanobut.logger.AndroidLogAdapter;

import me.weishu.reflection.Reflection;

public final class VLog {

    private static Logger sLogger = new ALogger();
    private static DiskLogPrinter sDiskLogPrinter = null;
    private static AndroidLogPrinter sAndroidLogPrinter = null;


    private VLog() {
        //no instance
    }

    public static void init(LogConfig logConfig) {
        if (null == logConfig) {
            throw new RuntimeException("LogConfig can't be null");
        }

        com.orhanobut.logger.Logger.addLogAdapter(new AndroidLogAdapter());
        ConfigCenter configCenter = ConfigCenter.getInstance();
        if (sDiskLogPrinter == null && sAndroidLogPrinter == null) {
            final Context applicationContext = logConfig.getContext().getApplicationContext();
            configCenter.setContext(applicationContext);
            sDiskLogPrinter = new DiskLogPrinter(logConfig.getContext(), logConfig.getLogEncrypt());
            sAndroidLogPrinter = new AndroidLogPrinter() {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    return 0 != (applicationContext.getApplicationInfo().flags
                            & ApplicationInfo.FLAG_DEBUGGABLE);
                }
            };
            sLogger.addPrinter(sDiskLogPrinter);
            sLogger.addPrinter(sAndroidLogPrinter);
            NetworkManager.getInstance().registerNetworChangeListener(applicationContext);
        }
        configCenter.setMaxKeepDaily(logConfig.getMaxKeepDaily());
        configCenter.setMaxLogSizeMb(logConfig.getMaxLogSizeMb());
        configCenter.setmLogPath(logConfig.getmLogPath());
        configCenter.setmCachePath(logConfig.getmCachePath());

        try {
            Reflection.unseal(logConfig.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setLogger(Logger logger) {
        sLogger = logger;
    }

    public static Logger getLogger() {
        return sLogger;
    }

    public static void addLogPrinter(Printer printer) {
        sLogger.addPrinter(printer);
    }

    public static String getDefaultLogPath() {
        if (sDiskLogPrinter != null) {
            return sDiskLogPrinter.getLogPath();
        }
        return "";
    }

    public static void clearLogPrinters() {
        sLogger.clearLogPrinters();
        sAndroidLogPrinter = null;
        sDiskLogPrinter = null;
    }


    public static void log(int priority, String tag, String message, Throwable throwable) {
        sLogger.log(priority, tag, message, throwable);
    }

    public static void d(String tag, String message, Object... args) {
        sLogger.d(message, args);
    }

    public static void d(String tag, Object object) {
        sLogger.d(tag, object);
    }

    public static void e(String tag, String message, Object... args) {
        sLogger.e(tag, message, args);
    }

    public static void e(String tag, Throwable throwable, String message, Object... args) {
        sLogger.e(tag, throwable, message, args);
    }

    public static void i(String tag, String message, Object... args) {
        sLogger.i(tag, message, args);
    }

    public static void v(String tag, String message, Object... args) {
        sLogger.v(tag, message, args);
    }

    public static void w(String tag, String message, Object... args) {
        sLogger.w(tag, message, args);
    }

    public static void wtf(String tag, String message, Object... args) {
        sLogger.wtf(tag, message, args);
    }

    public static void json(String tag, String json) {
        sLogger.json(tag, json);
    }

    public static void xml(String tag, String xml) {
        sLogger.xml(tag, xml);
    }

    public static void net(String tag, String message, Object... args) {
        sLogger.net(tag, message, args);
    }

    /**
     * 立即写入到文件，在上传日志的时候调用
     */
    public static void flush() {
        sLogger.flush();
    }


}
