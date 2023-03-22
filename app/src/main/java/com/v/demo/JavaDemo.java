package com.v.demo;

import com.v.log.util.LogExtKt;

/**
 * author  : ww
 * desc    :
 * time    : 2023/3/22 11:16
 */
public class JavaDemo {
    public void test() {
        LogExtKt.log("java~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        LogExtKt.logD("logD");
        LogExtKt.logE(new Throwable("测试"));
        LogExtKt.logW("logW");
        LogExtKt.logI("logI");
        LogExtKt.log("只打印3333333333");
        LogExtKt.logSave("只保存444444444444");

    }
}
