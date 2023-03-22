package com.v.log.stragety;

public interface LogStrategy {

    void log(int priority, String tag, String message, Boolean save, Boolean show);

    void flush();
}
