package com.v.log.util

import android.util.Log
import com.v.log.VLog

const val TAG = "V_LOG"

fun Any.logD() = run {
    this.showLog(
        TAG,
        Log.DEBUG
    )
}

fun Any.logD(tag: String = TAG) = run {
    this.showLog(
        tag,
        Log.DEBUG
    )
}

fun Any.logD(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag,
        Log.DEBUG,
        save
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


fun Any.logI() = run {
    this.showLog(
        TAG,
        Log.INFO
    )
}

fun Any.logI(tag: String = TAG) = run {
    this.showLog(
        tag,
        Log.INFO
    )
}

fun Any.logI(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag,
        Log.INFO,
        save
    )
}


fun Any.logW() = run {
    this.showLog(
        TAG,
        Log.WARN
    )
}

fun Any.logW(tag: String = TAG) = run {
    this.showLog(
        tag,
        Log.WARN
    )
}

fun Any.logW(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag,
        Log.WARN,
        save
    )
}


fun Any.logE() = run {
    this.showLog(
        TAG,
        Log.ERROR

    )
}

fun Any.logE(tag: String = TAG) = run {
    this.showLog(
        tag,
        Log.ERROR
    )
}

fun Any.logE(tag: String = TAG, save: Boolean = true) = run {
    this.showLog(
        tag,
        Log.ERROR,
        save
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

