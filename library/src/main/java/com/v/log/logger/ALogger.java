package com.v.log.logger;

import android.text.TextUtils;

import com.v.log.Printer.Printer;
import com.v.log.util.LogExtKt;
import com.v.log.util.LogUtils;

import java.util.ArrayList;
import java.util.List;


public class ALogger implements Logger {


    private final List<Printer> logPrinters = new ArrayList<>();

    public ALogger() {
    }

    @Override
    public void d(String tag, Boolean save, String message) {
        log(DEBUG, null, tag, save, true, message);
    }

    @Override
    public void e(String tag, Boolean save, String message) {
        log(ERROR, null, tag, save, true, message);
    }

    @Override
    public void w(String tag, Boolean save, String message) {
        log(WARN, null, tag, save, true, message);
    }

    @Override
    public void i(String tag, Boolean save, Boolean show, String message) {
        log(INFO, null, tag, save, show, message);
    }


    @Override
    public synchronized void log(int priority, String tag, Boolean save, Boolean show, String message, Throwable throwable) {
        if (throwable != null && message != null) {
            message += " : " + LogUtils.getStackTraceString(throwable);
        }
        if (throwable != null && message == null) {
            message = LogUtils.getStackTraceString(throwable);
        }
        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }

        for (Printer printer : logPrinters) {
            printer.log(priority, tag, message, save, show);
        }
    }

    @Override
    public void flush() {
        for (Printer printer : logPrinters) {
            printer.flush();
        }
    }

    @Override
    public void addPrinter(Printer printer) {
        logPrinters.add(printer);
    }

    @Override
    public List<Printer> getPrinters() {
        return logPrinters;
    }

    @Override
    public void clearLogPrinters() {

    }

    private synchronized void log(int priority, Throwable throwable, String tag, Boolean save, Boolean show, String message) {
        String tagFormat = tag;
        if (!LogExtKt.TAG.equals(tag)) {
            tagFormat = tag;
        }
        if (tag.length() > 50) {
            tagFormat = tag.substring(0, 50);
        }
        tagFormat = LogExtKt.TAG + " [" + tagFormat + "]";
        log(priority, tagFormat, save, show, message, throwable);
    }


}
