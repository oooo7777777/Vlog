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

import me.weishu.reflection.Reflection;

public final class VLog {

    private static Logger sLogger = new ALogger();
    private static DiskLogPrinter sDiskLogPrinter = null;
    private static AndroidLogPrinter sAndroidLogPrinter = null;
    private static boolean showLog;
    private static boolean showSaveLog;

    private VLog() {
        //no instance
    }

    public static void init(LogConfig logConfig) {
        if (null == logConfig) {
            throw new RuntimeException("LogConfig can't be null");
        }
        setShowLog(logConfig.getShowLog());
        setShowSaveLog(logConfig.getShowSaveLog());
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
        configCenter.setmLogPath(logConfig.getLogPath());
        configCenter.setmCachePath(logConfig.getCachePath());

        try {
            Reflection.unseal(logConfig.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isShowLog() {
        return showLog;
    }

    public static void setShowLog(boolean showLog) {
        VLog.showLog = showLog;
    }

    public static boolean isShowSaveLog() {
        return showSaveLog;
    }

    public static void setShowSaveLog(boolean showSaveLog) {
        VLog.showSaveLog = showSaveLog;
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


    public static void log(int priority, String tag, Boolean save, String message, Throwable throwable) {
        sLogger.log(priority, tag, save, message, throwable);
    }

    public static void d(String tag, Boolean save, String message, Object... args) {
        sLogger.d(tag, save, message, args);
    }


    public static void e(String tag, Boolean save, String message, Object... args) {
        sLogger.e(tag, save, message, args);
    }

    public static void e(String tag, Boolean save, Throwable throwable, String message, Object... args) {
        sLogger.e(tag, save, throwable, message, args);
    }

    public static void i(String tag, Boolean save, String message, Object... args) {
        sLogger.i(tag, save, message, args);
    }

    public static void v(String tag, Boolean save, String message, Object... args) {
        sLogger.v(tag, save, message, args);
    }

    public static void w(String tag, Boolean save, String message, Object... args) {
        sLogger.w(tag, save, message, args);
    }


    public static void json(String tag, Boolean save, String json) {
        sLogger.json(tag, save, json);
    }

    public static void xml(String tag, Boolean save, String xml) {
        sLogger.xml(tag, save, xml);
    }


    /**
     * 立即写入到文件，在上传日志的时候调用
     */
    public static void flush() {
        sLogger.flush();
    }


}
