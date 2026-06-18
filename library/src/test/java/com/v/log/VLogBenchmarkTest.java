package com.v.log;

import android.app.Application;

import com.v.log.config.ConfigCenter;
import com.v.log.inspector.LogInspectorStore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "src/main/AndroidManifest.xml")
public class VLogBenchmarkTest {

    @Before
    public void setUp() throws Exception {
        resetVLogState();
        LogInspectorStore.INSTANCE.setEnabled(false);
        LogInspectorStore.INSTANCE.clear();
        ConfigCenter.getInstance().setLogPath(null);
    }

    @Test
    public void benchmarkMixedPayloadAtFiftyLogsPerSecond() throws Exception {
        File rootDir = new File(System.getProperty("java.io.tmpdir"), "vlog-benchmark-" + System.nanoTime());
        File cacheDir = new File(rootDir, "cache");
        File logDir = new File(rootDir, "log");
        Assert.assertTrue(cacheDir.mkdirs() || cacheDir.isDirectory());
        Assert.assertTrue(logDir.mkdirs() || logDir.isDirectory());

        Application application = RuntimeEnvironment.getApplication();
        VLogConfig config = new VLogConfig(application)
                .setLogPath(logDir.getAbsolutePath());
        config.setShowLog(false);
        config.setBeautifyLog(false);
        VLog.init(config);

        final int logsPerSecond = 50;
        final int durationSeconds = 10;
        final int totalLogs = logsPerSecond * durationSeconds;
        final long intervalNanos = 1_000_000_000L / logsPerSecond;

        List<Long> enqueueMicros = new ArrayList<>(totalLogs);
        long benchmarkStart = System.nanoTime();
        long nextTick = benchmarkStart;

        for (int i = 0; i < totalLogs; i++) {
            waitUntil(nextTick);
            String message = buildPayload(i);
            long callStart = System.nanoTime();
            VLog.i("BenchTag", true, false, false, false, message);
            long callCostMicros = (System.nanoTime() - callStart) / 1_000L;
            enqueueMicros.add(callCostMicros);
            nextTick += intervalNanos;
        }

        long flushStart = System.nanoTime();
        VLog.flush();
        long flushDurationMs = (System.nanoTime() - flushStart) / 1_000_000L;
        long totalDurationMs = (System.nanoTime() - benchmarkStart) / 1_000_000L;

        File logFile = new File(logDir, com.v.log.util.DateUtil.getDateStr(System.currentTimeMillis()) + ".log");
        Assert.assertTrue(logFile.exists());
        String content = new String(Files.readAllBytes(logFile.toPath()), StandardCharsets.UTF_8);
        Assert.assertTrue(content.contains("bench-0-"));
        Assert.assertTrue(content.contains("bench-" + (totalLogs - 1) + "-"));

        List<Long> sorted = new ArrayList<>(enqueueMicros);
        Collections.sort(sorted);
        long p50 = percentile(sorted, 0.50);
        long p95 = percentile(sorted, 0.95);
        long p99 = percentile(sorted, 0.99);
        long max = sorted.get(sorted.size() - 1);
        long avg = average(sorted);

        System.out.println("VLog benchmark result:");
        System.out.println("  rate=" + logsPerSecond + "/s duration=" + durationSeconds + "s totalLogs=" + totalLogs);
        System.out.println("  enqueue_us avg=" + avg + " p50=" + p50 + " p95=" + p95 + " p99=" + p99 + " max=" + max);
        System.out.println("  flush_ms=" + flushDurationMs + " total_ms=" + totalDurationMs + " file_bytes=" + logFile.length());
    }

    private static String buildPayload(int index) {
        String prefix = "bench-" + index + "-";
        if (index % 3 == 0) {
            return prefix + repeat('a', 256);
        }
        if (index % 3 == 1) {
            return prefix + repeat('b', 4 * 1024);
        }
        return prefix + repeat('c', 20 * 1024);
    }

    private static void waitUntil(long targetNanoTime) {
        while (true) {
            long remaining = targetNanoTime - System.nanoTime();
            if (remaining <= 0) {
                return;
            }
            if (remaining > 2_000_000L) {
                try {
                    Thread.sleep(1L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            } else {
                Thread.yield();
            }
        }
    }

    private static long percentile(List<Long> values, double ratio) {
        int index = (int) Math.ceil(values.size() * ratio) - 1;
        index = Math.max(0, Math.min(index, values.size() - 1));
        return values.get(index);
    }

    private static long average(List<Long> values) {
        long sum = 0;
        for (Long value : values) {
            sum += value;
        }
        return values.isEmpty() ? 0 : sum / values.size();
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
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
