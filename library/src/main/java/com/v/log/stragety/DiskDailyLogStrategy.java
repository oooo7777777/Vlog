package com.v.log.stragety;

import android.content.Context;
import android.os.Build;

import com.v.log.ALogThreadPool;
import com.v.log.config.ConfigCenter;
import com.v.log.encrypt.LogEncrypt;
import com.v.log.io.LightLog;
import com.v.log.util.LogExtKt;
import com.v.log.util.LogUtils;
import com.v.log.util.NetworkManager;

public class DiskDailyLogStrategy implements DiskLogStrategy {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String SEPARATOR = ",";
    private Context context;
    private LogEncrypt logEncrypt;

    private DiskDailyLogStrategy(Builder builder) {
        context = builder.context;
        logEncrypt = builder.logEncrypt;
        initNativeLogger();
    }

    private void initNativeLogger() {
        LightLog.newInstance().init(ConfigCenter.getInstance().getmCachePath(),
                ConfigCenter.getInstance().getLogPath(),
                ConfigCenter.getInstance().getMaxLogSizeMb(),
                ConfigCenter.getInstance().getMaxKeepDaily());
    }

    @Override
    public void writeCommonInfo() {
        ALogThreadPool.getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                String commonInfo = getCommonInfo();
                writeLog(commonInfo);
            }
        });
    }

    private String getCommonInfo() {
        StringBuilder builder = new StringBuilder();
        builder.append(Build.BRAND);
        builder.append(SEPARATOR);
        builder.append(Build.MODEL);
        builder.append(SEPARATOR);
        builder.append(android.os.Build.VERSION.SDK_INT);
        builder.append(SEPARATOR);
        builder.append(NetworkManager.getInstance().getNetworkType(context));
        builder.append(SEPARATOR);
        builder.append(LogUtils.getVersionName(context));
        builder.append(NEW_LINE);
        return builder.toString();
    }

    @Override
    public void log(int level, String tag, final String message, Boolean save, Boolean show) {
        //保存在本地
        ALogThreadPool.getFixedThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                writeLog(message);
            }
        });

    }

    @Override
    public void flush() {
        LightLog.newInstance().flush();
    }


    private void writeLog(String message) {
        if (null != logEncrypt) {
            message = logEncrypt.encrypt(message);
        }
        try {
            LightLog.newInstance().write(message.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public String getLogPath() {
        return ConfigCenter.getInstance().getLogPath();
    }


    public static final class Builder {

        private Context context;
        private LogEncrypt logEncrypt;

        public Builder() {
        }

        public Builder setLogEncrypt(LogEncrypt logEncrypt) {
            this.logEncrypt = logEncrypt;
            return this;
        }

        public Builder context(Context context) {
            this.context = context.getApplicationContext();
            return this;
        }

        public DiskDailyLogStrategy build() {
            return new DiskDailyLogStrategy(this);
        }
    }

}
