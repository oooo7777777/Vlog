package com.v.log.config;

import android.content.Context;
import android.text.TextUtils;

import com.v.log.util.DateUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
     * 是否打印详情日志
     */
    private Boolean showDetailedLog = false;

    /**
     * 是否美化日志格式
     */
    private Boolean beautifyLog = true;

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

    public Boolean getShowDetailedLog() {
        return showDetailedLog;
    }

    public void setShowDetailedLog(Boolean showDetailedLog) {
        this.showDetailedLog = showDetailedLog;
    }

    public Boolean getBeautifyLog() {
        return beautifyLog;
    }

    public void setBeautifyLog(Boolean beautifyLog) {
        this.beautifyLog = beautifyLog;
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

    /**
     * 获取目录下面的所有文件日志
     */
    public List<String> getFilesAll() {
        File file = new File(getDefaultLogPath());
        File[] files = file.listFiles();
        if (files == null) {
            return null;
        }
        List<String> s = new ArrayList<>();
        for (int i = 0; i < files.length; i++) {
            s.add(files[i].getAbsolutePath());
        }
        return s;
    }

    /**
     * 获取今天日志
     */
    public String getTodayFilePath() {
        String path = getDefaultLogPath() + "/" + DateUtil.getCurrentDate() + ".log";
        File logFile = new File(path);
        if (logFile.exists()) {
            return path;
        } else {
            return "";
        }
    }


}
