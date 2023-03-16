package com.v.log.util

import com.v.log.VLog

const val TAG = "V_LOG"

enum class LEVEL {
    V, D, I, W, E, JSON, XML
}

fun Any.logV(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.V,
        tag,
        this.toString(),
        save
    )

fun Any.logD(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.D,
        tag,
        this.toString(),
        save
    )

fun Any.logI(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.I,
        tag,
        this.toString(),
        save
    )

fun Any.logW(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.W,
        tag,
        this.toString(),
        save
    )

fun Any.logE(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.E,
        tag,
        this.toString(),
        save
    )


fun Any.logJson(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.JSON,
        tag,
        this.toString(),
        save
    )

fun Any.logXml(tag: String = TAG, save: Boolean = true) =
    log(
        LEVEL.XML,
        tag,
        this.toString(),
        save
    )

//这两个方法为了java代码方便调用
fun Any.log() = run {
    this.log(TAG, LEVEL.I, true)
}
//这两个方法为了java代码方便调用
fun Any.log(tag: String = TAG) = run {
    this.log(tag, LEVEL.I, true)
}

fun Any.log(tag: String = TAG, level: LEVEL = LEVEL.I, save: Boolean = true) = run {
    log(level, tag, this.toString(), save)
}

/**
 * 当前线程
 */
fun logCurrentThreadName(tag: String = TAG, save: Boolean = true) = run {
    log(LEVEL.I, tag, Thread.currentThread().name, save)
}


/**
 * 打印日志
 * @param level 日志级别
 * @param tag 日志tag
 * @param message 日志内容
 * @param save 是否需要同步保存到日志文件(请不要保存过长文件)
 */
private fun log(level: LEVEL, tag: String, message: String, save: Boolean = true) {
    //tag最长为70 不然会打印不出来
    var tagFormat = tag
    if (tag.length > 70) {
        tagFormat = tag.substring(0, 70)
    }

    when (level) {
        LEVEL.V -> VLog.v(tagFormat, save, message)
        LEVEL.D -> VLog.d(tagFormat, save, message)
        LEVEL.I -> VLog.i(tagFormat, save, message)
        LEVEL.W -> VLog.w(tagFormat, save, message)
        LEVEL.E -> VLog.e(tagFormat, save, message)
        LEVEL.JSON -> VLog.json(tagFormat, save, message)
        LEVEL.XML -> VLog.xml(tagFormat, save, message)
    }
}