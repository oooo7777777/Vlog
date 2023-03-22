package com.v.log.Printer;

import android.content.Context;

import com.v.log.encrypt.LogEncrypt;
import com.v.log.stragety.CsvFormatStrategy;
import com.v.log.stragety.DiskDailyLogStrategy;
import com.v.log.stragety.DiskLogStrategy;


public class DiskLogPrinter implements DiskLogStrategy, Printer {

    private DiskLogStrategy formatStrategy;


    public DiskLogPrinter(Context context, LogEncrypt logEncrypt) {
        formatStrategy = CsvFormatStrategy.newBuilder()
                .logStrategy(DiskDailyLogStrategy
                        .newBuilder()
                        .setLogEncrypt(logEncrypt)
                        .context(context.getApplicationContext())
                        .build())
                .build();
    }

    public DiskLogPrinter(DiskLogStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
    }

    @Override
    public String getLogPath() {
        return this.formatStrategy.getLogPath();
    }

    @Override
    public void writeCommonInfo() {
        formatStrategy.writeCommonInfo();
    }


    @Override
    public void log(int priority, String tag, String message, Boolean save, Boolean show) {
        formatStrategy.log(priority, tag, message, save, show);
    }


    @Override
    public void flush() {
        formatStrategy.flush();
    }


}
