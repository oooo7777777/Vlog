package com.v.log.Printer;


import com.v.log.stragety.LogStrategy;
import com.v.log.stragety.PrettyFormatStrategy;

public class AndroidLogPrinter implements Printer {

    private final LogStrategy formatStrategy;

    public AndroidLogPrinter() {
        this.formatStrategy = PrettyFormatStrategy.newBuilder().build();
    }

    public AndroidLogPrinter(LogStrategy formatStrategy) {
        this.formatStrategy = formatStrategy;
    }

    @Override
    public boolean isLoggable(int priority, String tag) {
        return true;
    }

    @Override
    public void log(int priority, String tag, String message, Boolean save) {
        formatStrategy.log(priority, tag, message, save);
    }

    @Override
    public void flush() {
        formatStrategy.flush();
    }

}
