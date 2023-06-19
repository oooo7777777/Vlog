package com.v.log;

import android.content.Context;

import com.v.log.Printer.DiskLogPrinter;
import com.v.log.config.ConfigCenter;
import com.v.log.logger.ALogger;
import com.v.log.logger.Logger;
import com.v.log.util.NetworkManager;

import java.util.List;

public final class VLog {

    private static Logger sLogger = new ALogger();
    private static DiskLogPrinter sDiskLogPrinter = null;


    private VLog() {
    }

    public static void init(LogConfig logConfig) {
        if (null == logConfig) {
            throw new RuntimeException("LogConfig can't be null");
        }
        ConfigCenter configCenter = ConfigCenter.getInstance();
        if (sDiskLogPrinter == null) {
            final Context applicationContext = logConfig.getContext().getApplicationContext();
            configCenter.setContext(applicationContext);
            sDiskLogPrinter = new DiskLogPrinter(logConfig.getContext(), logConfig.getLogEncrypt());
            sLogger.addPrinter(sDiskLogPrinter);
            NetworkManager.getInstance().registerNetworChangeListener(applicationContext);
        }
        configCenter.setMaxKeepDaily(logConfig.getMaxKeepDaily());
        configCenter.setMaxLogSizeMb(logConfig.getMaxLogSizeMb());
        configCenter.setmLogPath(logConfig.getLogPath());
        configCenter.setmCachePath(logConfig.getCachePath());
        configCenter.setSaveLog(logConfig.getSaveLog());
        configCenter.setShowLog(logConfig.getShowLog());
        configCenter.setShowDetailedLog(logConfig.getShowDetailedLog());
        configCenter.setBeautifyLog(logConfig.getBeautifyLog());

//        try {
//            Reflection.unseal(logConfig.getContext());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }


    public static Logger getLogger() {
        return sLogger;
    }


    public static String getDefaultLogPath() {
        if (sDiskLogPrinter != null) {
            return sDiskLogPrinter.getLogPath();
        }
        return "";
    }

    public static List<String> getFilesAll() {
        return ConfigCenter.getInstance().getFilesAll();
    }

    public static String getTodayFilePath() {
        return ConfigCenter.getInstance().getTodayFilePath();
    }

    public static void clearLogPrinters() {
        sLogger.clearLogPrinters();
        sDiskLogPrinter = null;
    }


    public static void d(String tag, Boolean save, String message) {
        sLogger.d(tag, save, message);
    }


    public static void e(String tag, Boolean save, String message) {
        sLogger.e(tag, save, message);
    }

    public static void i(String tag, Boolean save, Boolean show, String message) {
        sLogger.i(tag, save, show, message);
    }


    public static void w(String tag, Boolean save, String message) {
        sLogger.w(tag, save, message);
    }

    /**
     * 立即写入到文件，在上传日志的时候调用
     */
    public static void flush() {
        sLogger.flush();
    }

}
