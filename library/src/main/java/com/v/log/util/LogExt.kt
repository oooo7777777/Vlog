package com.v.log.util

import android.util.Log
import com.v.log.VLog

const val TAG = "V_LOG"

@JvmOverloads
fun Any.logD(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag = tag,
        level = Log.DEBUG,
        saveLog = save
    )
}

/**
 * 此方法会打印日志不会保存日志
 */
fun Any.log() = run {
    this.showLog(
        TAG,
        Log.INFO,
        saveLog = false
    )
}

fun Any.log(tag: String = TAG) = run {
    this.showLog(
        tag,
        Log.INFO,
        saveLog = false
    )
}

/**
 * 此方法保存日志不会打印日志
 */
fun Any.logSave() = run {
    this.showLog(
        TAG,
        Log.INFO,
        saveLog = true,
        showLog = false
    )
}

@JvmOverloads
fun Any.logI(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag = tag,
        level = Log.INFO,
        saveLog = save
    )
}

@JvmOverloads
fun Any.logW(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag = tag,
        level = Log.WARN,
        saveLog = save
    )
}

@JvmOverloads
fun Any.logE(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag = tag,
        level = Log.ERROR,
        saveLog = save
    )
}

private fun Any.showLog(
    tag: String = TAG,
    level: Int = Log.INFO,
    saveLog: Boolean = true,
    showLog: Boolean = true
) = run {

    var msg = this.toString()
    if (this is Throwable) {
        msg = Log.getStackTraceString(this)
    }

    when (level) {
        Log.DEBUG -> {
            VLog.d(
                tag,
                saveLog,
                msg
            )
        }

        Log.WARN -> {
            VLog.w(
                tag,
                saveLog,
                msg
            )
        }

        Log.ERROR -> {
            VLog.e(
                tag,
                saveLog,
                msg
            )
        }

        else -> {
            VLog.i(
                tag,
                saveLog,
                showLog,
                msg
            )
        }

    }
}
