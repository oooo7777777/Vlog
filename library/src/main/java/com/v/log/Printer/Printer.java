package com.v.log.Printer;

public interface Printer {

    void log(int priority, String tag, String message, Boolean save, Boolean show);

    void flush();
}
