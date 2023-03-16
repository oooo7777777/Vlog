package com.v.log.config;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

public class ConfigCenter {
    /**
     * 默认保存7天日志
     */
    private static final int DEFAULT_DAILY_SIZE = 7;

    /**
     * 默认保存70m
     */
    private static final double DEFAULT_LOG_FILE_SIZE_MB = 70;


    /**
     * 日志最大保存天数
     */
    private int mMaxKeepDaily = DEFAULT_DAILY_SIZE;

    private double mMaxLogSizeMb = DEFAULT_LOG_FILE_SIZE_MB;

    /**
     * 日志保存路径
     */
    private String mLogPath;

    /**
     * 日志缓存路径
     */
    private String mCachePath;

    private Context mContext;


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

    private static class ConfigHolder {
        private static final ConfigCenter instance = new ConfigCenter();
    }

    public static ConfigCenter getInstance() {
        return ConfigHolder.instance;
    }

    private ConfigCenter() {

    }

    public int getMaxKeepDaily() {
        return mMaxKeepDaily;
    }

    public void setMaxKeepDaily(int maxKeepDaily) {
        this.mMaxKeepDaily = maxKeepDaily;
    }

    public double getMaxLogSizeMb() {
        return mMaxLogSizeMb;
    }

    public void setMaxLogSizeMb(double maxLogSizeMb) {
        mMaxLogSizeMb = maxLogSizeMb;
    }

    public Boolean getShowLog() {
        return showLog;
    }

    public void setShowLog(Boolean showLog) {
        this.showLog = showLog;
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

    public String getLogPath() {
        if (TextUtils.isEmpty(mLogPath)) {
            mLogPath = getDefaultLogPath();
        }
        return mLogPath;
    }

    public String getmCachePath() {
        if (TextUtils.isEmpty(mCachePath)) {
            mCachePath = getDefaultCachePath();
        }
        return mCachePath;
    }

    public void setmLogPath(String mLogPath) {
        this.mLogPath = mLogPath;
    }

    public void setmCachePath(String mCachePath) {
        this.mCachePath = mCachePath;
    }

    public Context getContext() {
        if (mContext == null) throw new RuntimeException("ConfigCenter context can not be null");
        return mContext;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    /**
     * /sdcard/Android/data/xx.xx.xx/files/log
     *
     * @return 返回默认目录
     */
    private String getDefaultLogPath() {
        String mPath = new File(getContext().getExternalFilesDir(null), "log").getAbsolutePath();
        File logFile = new File(mPath);
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        return mPath;
    }

    private String getDefaultCachePath() {
        String mPath = new File(getContext().getExternalFilesDir(null), "cache").getAbsolutePath();
        File logFile = new File(mPath);
        if (!logFile.exists()) {
            logFile.mkdirs();
        }
        return mPath;
    }

}
