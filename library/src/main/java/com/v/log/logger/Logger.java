package com.v.log.logger;


import com.v.log.Printer.Printer;

import java.util.List;

public interface Logger {

    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;

    void d(String tag, Boolean save, String message);

    void e(String tag, Boolean save, String message);

    void w(String tag, Boolean save, String message);

    void i(String tag, Boolean save, Boolean show, String message);

    void log(int priority, String tag, Boolean save, Boolean show, String message, Throwable throwable);

    void flush();

    void addPrinter(Printer printer);

    List<Printer> getPrinters();

    void clearLogPrinters();
}
