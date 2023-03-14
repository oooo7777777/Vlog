package com.android.jesse.log.stragety;

import android.util.Log;

public class LogcatLogStrategy implements LogStrategy {

  @Override
  public void log(int priority, String tag, String message) {
    //不打印日志
    Log.println(priority, tag, message);
//    Logger.i(message);
  }

  @Override
  public void flush() {

  }

}
