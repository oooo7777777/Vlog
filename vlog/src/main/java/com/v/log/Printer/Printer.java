package com.v.log.Printer;

public interface Printer {

  boolean isLoggable(int priority, String tag);

  void log(int priority, String tag, String message, Boolean save);

  void flush();
}
