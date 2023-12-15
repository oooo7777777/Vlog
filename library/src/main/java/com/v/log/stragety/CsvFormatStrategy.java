package com.v.log.stragety;

import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.v.log.config.ConfigCenter;
import com.v.log.util.LogUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CsvFormatStrategy implements DiskLogStrategy {

    private static final String NEW_LINE = System.getProperty("line.separator");
    private static final String SEPARATOR = ",";
    private final Date date;
    private final SimpleDateFormat dateFormat;
    private final DiskLogStrategy logStrategy;

    private static final int CHUNK_SIZE = 4000;

    private static final char TOP_LEFT_CORNER = '┌';
    private static final char BOTTOM_LEFT_CORNER = '└';
    private static final char HORIZONTAL_LINE = '│';
    private static final String DOUBLE_DIVIDER = "────────────────────────────────────────────────────────";
    private static final String TOP_BORDER = TOP_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;
    private static final String BOTTOM_BORDER = BOTTOM_LEFT_CORNER + DOUBLE_DIVIDER + DOUBLE_DIVIDER;

    private CsvFormatStrategy(Builder builder) {
        date = new Date();
        dateFormat = builder.dateFormat;
        logStrategy = builder.logStrategy;
    }

    public static Builder newBuilder() {
        return new Builder();
    }


    @Override
    public void writeCommonInfo() {
        logStrategy.writeCommonInfo();
    }

    @Override
    public void log(int priority, String onceOnlyTag, String message, Boolean save, Boolean show) {

        date.setTime(System.currentTimeMillis());

        StringBuilder builder = new StringBuilder();
        // time
        builder.append(dateFormat.format(date));
        builder.append(SEPARATOR);
        builder.append(Thread.currentThread().getId());
        builder.append(SEPARATOR);
        String threadName = Thread.currentThread().getName();
        if (threadName.contains("OkHttp")) {
            threadName = "OkHttp";
        }
        builder.append(threadName);
        builder.append(SEPARATOR);
        builder.append(LogUtils.logLevel(priority));
        builder.append(SEPARATOR);
        builder.append(onceOnlyTag);
        Pair<String, String> classAndMethodName = LogUtils.getClassAndMethodName();
        if (classAndMethodName.first != null) {
            builder.append(SEPARATOR);
            builder.append(classAndMethodName.first);

        }
        if (classAndMethodName.second != null) {
            builder.append(SEPARATOR);
            builder.append(classAndMethodName.second);
        }
        builder.append("\n");
        builder.append(message);
        builder.append(NEW_LINE);


        //打印日志
        if (ConfigCenter.getInstance().getShowLog() && show) {
            //打印详细日志
            if (ConfigCenter.getInstance().getShowDetailedLog()) {
                logFormat(priority, onceOnlyTag, builder.toString());
            } else {
                if (ConfigCenter.getInstance().getBeautifyLog()) {
                    logFormat(priority, onceOnlyTag, message);
                } else {
                    Log.println(priority, onceOnlyTag, message);
                }
            }
        }

        //是否需要保存在本地
        if (ConfigCenter.getInstance().getSaveLog() && save) {
            builder.append("============================================================================================================================>\n");
            logStrategy.log(priority, onceOnlyTag, builder.toString(), true, show);
        }
    }

    //日志美化
    private void logFormat(int priority, String onceOnlyTag, String message) {
        logTopBorder(priority, onceOnlyTag);

        byte[] bytes = message.getBytes();
        int length = bytes.length;
        if (length <= CHUNK_SIZE) {
            logContent(priority, onceOnlyTag, message);
            logBottomBorder(priority, onceOnlyTag);
            return;
        }
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int count = Math.min(length - i, CHUNK_SIZE);
            logContent(priority, onceOnlyTag, new String(bytes, i, count));
        }
        logBottomBorder(priority, onceOnlyTag);
    }

    private void logTopBorder(int logType, String tag) {
        logChunk(logType, tag, TOP_BORDER);
    }

    private void logBottomBorder(int logType, String tag) {
        logChunk(logType, tag, BOTTOM_BORDER);
    }


    private void logContent(int logType, String tag, String chunk) {
        String[] lines = chunk.split(System.getProperty("line.separator"));
        for (String line : lines) {
            logChunk(logType, tag, HORIZONTAL_LINE + " " + line);
        }
    }

    private void logChunk(int priority, String tag, String chunk) {
        Log.println(priority, tag, chunk);
    }


    @Override
    public void flush() {
        logStrategy.flush();
    }

    /**
     * csv格式如果有逗号或者换行的话整体用双引号括起来；如果里面还有双引号就替换成两个双引号，
     *
     * @param message
     * @return
     */
    private String csvFormatHandle(String message) {
        if (TextUtils.isEmpty(message)) return message;
        String messageCSV = message;
        Matcher matcher = Pattern.compile("[\r\n\t]").matcher(messageCSV);
        // 如果包含逗号或者换行的话就在前后添加双引号
        if (message.contains(",") || matcher.find()) {
            // 先将双引号转义，避免两边加了双引号后转义错误
            if (message.contains("\"")) {
                messageCSV = message.replace("\"", "\"\"");
            }
            messageCSV = "\"" + messageCSV + "\"";
        }
        return messageCSV;
    }

    @Override
    public String getLogPath() {
        return logStrategy.getLogPath();
    }

    public static final class Builder {
        SimpleDateFormat dateFormat;
        DiskLogStrategy logStrategy;

        private Builder() {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.CHINA);
            logStrategy = new DiskDailyLogStrategy.Builder().build();
        }

        public Builder dateFormat(SimpleDateFormat val) {
            dateFormat = val;
            return this;
        }

        public Builder logStrategy(DiskLogStrategy val) {
            logStrategy = val;
            return this;
        }

        public CsvFormatStrategy build() {
            return new CsvFormatStrategy(this);
        }
    }
}