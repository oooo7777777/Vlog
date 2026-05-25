package com.v.log.io;

import com.v.log.util.DateUtil;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RunWith(RobolectricTestRunner.class)
public class LightLogTest {

    @Test
    public void writesLargeMessageAfterFlush() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-lightlog-test-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        LightLog lightLog = LightLog.newInstance();
        lightLog.init(cacheDir.getAbsolutePath(), logDir.getAbsolutePath(), 50, 7);
        lightLog.isCanWriteToSDCard = true;

        String message = repeat('x', 2048);
        lightLog.write(message.getBytes(StandardCharsets.UTF_8));
        lightLog.flush();

        File logFile = new File(logDir, DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains(message));
    }

    @Test
    public void writesVeryLargeMessageInChunks() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-lightlog-test-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        LightLog lightLog = LightLog.newInstance();
        lightLog.init(cacheDir.getAbsolutePath(), logDir.getAbsolutePath(), 50, 7);
        lightLog.isCanWriteToSDCard = true;

        String message = repeat('y', 200 * 1024);
        lightLog.write(message.getBytes(StandardCharsets.UTF_8));
        lightLog.flush();

        File logFile = new File(logDir, DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains(message));
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }
}
