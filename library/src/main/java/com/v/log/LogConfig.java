package com.v.log;

import android.content.Context;

import com.v.log.encrypt.LogEncrypt;


public class LogConfig {

    /**
     * 上下文(*必须)
     */
    private Context mContext;

    /**
     * 日志最大容量，默认70m
     */
    private double maxLogSizeMb = 70;

    /**
     * 日志保存日期。默认近7天
     */
    private int maxKeepDaily = 7;

    /**
     * 日志加密方式
     */
    private LogEncrypt logEncrypt;

    /**
     * 日志保存路径
     */
    private String logPath;

    /**
     * 日志缓存路径
     */
    private String cachePath;

    /**
     * 是否打印日志
     */
    private Boolean showLog = true;

    /**
     * 是否打印保存日志
     */
    private Boolean showSaveLog = false;

    /**
     * 是否保存所有日志
     */
    private Boolean saveLog = true;


    public LogConfig(Context context) {
        mContext = context;
    }

    public LogConfig(Context context, Boolean showLog) {
        mContext = context;
        this.showLog = showLog;
    }

    public LogConfig setLogEncrypt(LogEncrypt logEncrypt) {
        this.logEncrypt = logEncrypt;
        return this;
    }

    public LogConfig maxLogSizeMb(double maxLogSizeMb) {
        this.maxLogSizeMb = maxLogSizeMb;
        return this;
    }

    public LogConfig maxKeepDaily(int maxKeepDaily) {
        this.maxKeepDaily = maxKeepDaily;
        return this;
    }

    public String getLogPath() {
        return logPath;
    }

    public LogConfig setLogPath(String mLogPath) {
        this.logPath = mLogPath;
        return this;
    }

    public String getCachePath() {
        return cachePath;
    }

    public LogConfig setCachePath(String mCachePath) {
        this.cachePath = mCachePath;
        return this;
    }

    public LogEncrypt getLogEncrypt() {
        return logEncrypt;
    }

    public Context getContext() {
        return mContext;
    }

    public double getMaxLogSizeMb() {
        return maxLogSizeMb;
    }

    public int getMaxKeepDaily() {
        return maxKeepDaily;
    }

    public Boolean getShowLog() {
        return showLog;
    }

    public void setShowLog(Boolean mShowLog) {
        this.showLog = mShowLog;
    }

    public Boolean getShowSaveLog() {
        return showSaveLog;
    }

    public void setShowSaveLog(Boolean showSaveLog) {
        this.showSaveLog = showSaveLog;
    }

    public Boolean getSaveLog() {
        return saveLog;
    }

    public void setSaveLog(Boolean saveLog) {
        this.saveLog = saveLog;
    }
}
