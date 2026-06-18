package com.v.log.io;

import android.text.TextUtils;

import com.v.log.util.DateUtil;
import com.v.log.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class LightLog {
    private static final long KB = 1024;
    private static final long MB = 1024 * KB;
    private static final int MAX_WRITE_CHUNK_SIZE = 64 * 1024;
    private static final long FORCE_INTERVAL_MS = 250;
    private static final long FORCE_THRESHOLD_BYTES = 16 * KB;
    private static final int MINUTE = 60 * 1000;
    private static final long DAY = 24 * 60 * 60 * 1000;
    private static final double DEFAULT_MAX_LOG_SIZE = 50;
    private static final int DEFAULT_KEEP_DAILY = 7;

    private static volatile LightLog sLightLog;
    private RandomAccessFile mLogWriter;
    private FileChannel mLogChannel;
    private long mPendingForceBytes;
    private long mLastForceTime;
    private String mActiveLogDate;

    private String mPath;      //log目录
    private double mMaxLogSizeMb = DEFAULT_MAX_LOG_SIZE;
    private int mMaxKeepDaily = DEFAULT_KEEP_DAILY;

    boolean isCanWriteToSDCard = false;
    private long mCurrentDay;
    private long mLastTime;

    private LightLog() {

    }

    public static LightLog newInstance() {
        if (sLightLog == null) {
            synchronized (LightLog.class) {
                sLightLog = new LightLog();
            }
        }
        return sLightLog;
    }

    public void init(String path, double maxLogSizeMb, int maxKeepDaily) {
        mPath = path;
        mMaxLogSizeMb = maxLogSizeMb;
        mMaxKeepDaily = maxKeepDaily;
        mPendingForceBytes = 0;
        mLastForceTime = 0;
        mActiveLogDate = null;
        closeLogWriter();
        if (TextUtils.isEmpty(mPath)) {
            throw new RuntimeException("init method is not invoked");
        }
        try {
            migrateLegacyCacheIfNeeded(DateUtil.getDateStr(System.currentTimeMillis()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void flush() {
        String currentDate = DateUtil.getDateStr(System.currentTimeMillis());
        flush(currentDate);
    }

    public synchronized void flush(String date) {
        if (TextUtils.isEmpty(date)) {
            return;
        }
        try {
            if (date.equals(mActiveLogDate)) {
                forceCurrentLogWrites(true);
            } else {
                closeLogWriter();
                migrateLegacyCacheIfNeeded(date);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void write(byte[] log) {

        if (!isDay()) {
            long tempCurrentDay = DateUtil.getCurrentTime();
            //save时间
            long deleteTime = tempCurrentDay - mMaxKeepDaily * DAY;
            deleteExpiredFile(deleteTime);
            mCurrentDay = tempCurrentDay;
        }

        long currentTime = System.currentTimeMillis(); //每隔1分钟判断一次
        if (currentTime - mLastTime > MINUTE) {
            isCanWriteToSDCard = isCanWriteSDCard();
        }
        mLastTime = System.currentTimeMillis();

        if (!isCanWriteToSDCard) {
            return;
        }

        if (null == log) {
            return;
        }

        try {
            String currentDate = DateUtil.getDateStr(System.currentTimeMillis());
            ensureLogWriter(currentDate);
            appendToCurrentLog(log);
            forceCurrentLogWrites(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isDay() {
        long currentTime = System.currentTimeMillis();
        return mCurrentDay < currentTime && mCurrentDay + DAY > currentTime;
    }


    private boolean isCanWriteSDCard() {
        boolean item = false;
        try {
            long total = FileUtils.getFileSizes(new File(mPath));
            if (total < mMaxLogSizeMb * MB) {
                item = true;
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return item;
    }

    /**
     * 删除过期的文件
     *
     * @param deleteTime 此时间之前的
     */
    private void deleteExpiredFile(long deleteTime) {
        File dir = new File(mPath);
        if (dir.isDirectory()) {
            String[] files = dir.list();
            if (files != null) {
                for (String item : files) {
                    try {
                        if (TextUtils.isEmpty(item)) {
                            continue;
                        }
                        String[] longStrArray = item.split("\\.");
                        if (longStrArray.length > 0) {  //小于时间就删除
                            long longItem = DateUtil.getDateTime(longStrArray[0]);
                            if (longItem <= deleteTime) {
                                new File(mPath, item).delete(); //删除文件
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void ensureLogWriter(String date) throws IOException {
        if (date.equals(mActiveLogDate) && mLogWriter != null && mLogChannel != null && mLogChannel.isOpen()) {
            return;
        }
        forceCurrentLogWrites(true);
        closeLogWriter();
        File logFile = new File(mPath, date + ".log");
        File parent = logFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!logFile.exists()) {
            logFile.createNewFile();
        }
        mLogWriter = new RandomAccessFile(logFile, "rw");
        mLogChannel = mLogWriter.getChannel();
        mLogChannel.position(mLogChannel.size());
        mActiveLogDate = date;
        mLastForceTime = System.currentTimeMillis();
    }

    private void appendToCurrentLog(byte[] log) throws IOException {
        if (mLogChannel == null || !mLogChannel.isOpen()) {
            return;
        }
        int offset = 0;
        while (offset < log.length) {
            int writable = Math.min(MAX_WRITE_CHUNK_SIZE, log.length - offset);
            int written = 0;
            while (written < writable) {
                written += mLogChannel.write(java.nio.ByteBuffer.wrap(log, offset + written, writable - written));
            }
            offset += written;
            mPendingForceBytes += written;
        }
    }

    private void forceCurrentLogWrites(boolean forceAll) throws IOException {
        if (mLogChannel == null || !mLogChannel.isOpen()) {
            return;
        }
        long now = System.currentTimeMillis();
        if (!forceAll
                && mPendingForceBytes < FORCE_THRESHOLD_BYTES
                && now - mLastForceTime < FORCE_INTERVAL_MS) {
            return;
        }
        mLogChannel.force(false);
        mPendingForceBytes = 0;
        mLastForceTime = now;
    }

    private void closeLogWriter() {
        try {
            if (mLogChannel != null) {
                mLogChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mLogChannel = null;
        }
        try {
            if (mLogWriter != null) {
                mLogWriter.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            mLogWriter = null;
            mActiveLogDate = null;
        }
    }

    private void migrateLegacyCacheIfNeeded(String date) throws IOException {
        File cacheFile = getLegacyCacheFile();
        if (!cacheFile.exists() || cacheFile.length() <= 0) {
            return;
        }
        ensureLogWriter(date);
        RandomAccessFile cacheReader = null;
        FileChannel cacheChannel = null;
        try {
            cacheReader = new RandomAccessFile(cacheFile, "rw");
            cacheChannel = cacheReader.getChannel();
            long cacheSize = cacheChannel.size();
            transferFully(cacheChannel, mLogChannel, cacheSize);
            cacheChannel.truncate(0);
            cacheChannel.force(true);
            mPendingForceBytes += cacheSize;
            forceCurrentLogWrites(true);
        } finally {
            if (cacheChannel != null) {
                cacheChannel.close();
            }
            if (cacheReader != null) {
                cacheReader.close();
            }
        }
    }

    private File getLegacyCacheFile() {
        if (TextUtils.isEmpty(mPath)) {
            throw new RuntimeException("init method is not invoked");
        }
        File baseDir = new File(mPath).getParentFile();
        if (baseDir == null) {
            return new File("cache.log");
        }
        return new File(new File(baseDir, "cache"), "cache.log");
    }

    private void transferFully(FileChannel input, WritableByteChannel output, long bytes) throws IOException {
        long position = 0;
        while (position < bytes) {
            long transferred = input.transferTo(position, bytes - position, output);
            if (transferred <= 0) {
                break;
            }
            position += transferred;
        }
        if (position < bytes) {
            throw new IOException("Failed to transfer complete cache file. expected=" + bytes + ", actual=" + position);
        }
    }
}
