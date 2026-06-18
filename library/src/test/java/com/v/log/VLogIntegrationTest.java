package com.v.log;

import android.app.Application;

import com.v.log.config.ConfigCenter;
import com.v.log.inspector.LogInspectorStore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.junit.runner.RunWith;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class VLogIntegrationTest {

    @Before
    public void setUp() throws Exception {
        resetVLogState();
        LogInspectorStore.INSTANCE.setEnabled(false);
        LogInspectorStore.INSTANCE.clear();
        ConfigCenter.getInstance().setLogPath(null);
    }

    @Test
    public void initAndFlush_writeLogToDailyFile() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-integration-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLog.init(new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath()));

        VLog.i("InitTag", true, false, false, false, "hello integration");
        drainAsyncWrites();
        VLog.flush();

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("hello integration"));
        Assert.assertTrue(content.contains("InitTag"));
    }

    @Test
    public void initWithEncryptor_persistsEncryptedMessage() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-encrypt-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLog.init(new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath())
                .setLogEncrypt(message -> "ENC(" + message + ")"));

        VLog.w("EncryptTag", true, false, false, false, "plain-text");
        drainAsyncWrites();
        VLog.flush();

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("ENC("));
        Assert.assertTrue(content.contains("plain-text"));
    }

    @Test
    public void flushWaitsForQueuedAsyncWrites() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-flush-barrier-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLog.init(new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath()));

        for (int i = 0; i < 60; i++) {
            VLog.i("FlushTag", true, false, false, false, "msg-" + i);
        }

        VLog.flush();

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("msg-0"));
        Assert.assertTrue(content.contains("msg-59"));
    }

    @Test
    public void enableInspector_collectsEntries_andDisableClearsThem() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-inspector-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLog.init(new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath()));

        VLog.setLogInspectorEnabled(true);

        VLog.d("InspectorTag", false, false, false, false, "memory only");

        Assert.assertEquals(1, LogInspectorStore.INSTANCE.snapshot().size());
        Assert.assertEquals("InspectorTag", LogInspectorStore.INSTANCE.snapshot().get(0).getTag());

        VLog.setLogInspectorEnabled(false);

        Assert.assertTrue(LogInspectorStore.INSTANCE.snapshot().isEmpty());
    }

    @Test
    public void configDefaultSaveLog_persistsToDisk() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-default-save-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLogConfig config = new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath());
        config.setShowLog(false);
        VLog.init(config);

        VLog.i("DefaultSaveTag", ConfigCenter.getInstance().getSaveLog(), false, false, false, "default-save-message");
        VLog.flush();

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("default-save-message"));
    }

    @Test
    public void configSaveLogFalse_skipsDiskWriteByDefault() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-no-save-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLogConfig config = new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath());
        config.setShowLog(false);
        config.setSaveLog(false);
        VLog.init(config);

        VLog.i("NoSaveTag", ConfigCenter.getInstance().getSaveLog(), false, false, false, "no-save-message");
        VLog.flush();

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        if (logFile.exists()) {
            String content = new String(java.nio.file.Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
            Assert.assertFalse(content.contains("no-save-message"));
        }
    }

    private static void drainAsyncWrites() throws InterruptedException {
        Thread.sleep(200L);
    }

    private static void resetVLogState() throws Exception {
        VLog.getLogger().getPrinters().clear();

        Field diskPrinterField = VLog.class.getDeclaredField("sDiskLogPrinter");
        diskPrinterField.setAccessible(true);
        diskPrinterField.set(null, null);

        ConfigCenter configCenter = ConfigCenter.getInstance();
        configCenter.setContext(RuntimeEnvironment.getApplication());
        configCenter.setShowLog(true);
        configCenter.setShowDetailedLog(false);
        configCenter.setBeautifyLog(true);
        configCenter.setSaveLog(true);
        configCenter.setTag("V_LOG");
    }
}
