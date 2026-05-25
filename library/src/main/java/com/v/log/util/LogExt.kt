package com.v.log.util

import android.util.Log
import com.v.log.VLog
import com.v.log.config.ConfigCenter
import com.v.log.logger.Logger


@JvmOverloads
fun Any.logD(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.DEBUG,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

@JvmOverloads
fun Any.logV(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.VERBOSE,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

/**
 * 此方法会打印日志不会保存日志
 */
fun Any.log() = run {
    this.showLog(
        ConfigCenter.getInstance().tag,
        Logger.DEFAULT,
        saveLog = false,
        beautify = false,
        detailed = false,
    )
}

fun Any.log(tag: String = ConfigCenter.getInstance().tag) = run {
    this.showLog(
        tag,
        Logger.DEFAULT,
        saveLog = false,
        beautify = false,
        detailed = false,
    )
}

/**
 * 此方法保存日志不会打印日志
 */
fun Any.logSave(
    tag: String = ConfigCenter.getInstance().tag,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag,
        Log.INFO,
        saveLog = true,
        showLog = false,
        beautify = beautify,
        detailed = detailed
    )
}


@JvmOverloads
fun Any.logI(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.INFO,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

@JvmOverloads
fun Any.logW(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.WARN,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

@JvmOverloads
fun Any.logE(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.ERROR,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

@JvmOverloads
fun Any.logA(
    tag: String = ConfigCenter.getInstance().tag,
    save: Boolean = ConfigCenter.getInstance().saveLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {
    this.showLog(
        tag = tag,
        level = Log.ASSERT,
        saveLog = save,
        beautify = beautify,
        detailed = detailed
    )
}

private fun Any.showLog(
    tag: String=ConfigCenter.getInstance().tag,
    level: Int = Log.INFO,
    saveLog: Boolean = ConfigCenter.getInstance().saveLog,
    showLog: Boolean = ConfigCenter.getInstance().showLog,
    beautify: Boolean = ConfigCenter.getInstance().beautifyLog,
    detailed: Boolean = ConfigCenter.getInstance().showDetailedLog
) = run {

    var msg = this.toString()
    if (this is Throwable) {
        msg = Log.getStackTraceString(this)
    }

    when (level) {
        Log.VERBOSE -> {
            VLog.v(
                tag,
                saveLog,
                beautify,
                detailed,
                msg
            )
        }

        Log.DEBUG -> {
            VLog.d(
                tag,
                saveLog,
                beautify,
                detailed,
                msg
            )
        }

        Log.WARN -> {
            VLog.w(
                tag,
                saveLog,
                beautify,
                detailed,
                msg
            )
        }

        Log.ERROR -> {
            VLog.e(
                tag,
                saveLog,
                beautify,
                detailed,
                msg
            )
        }

        Log.ASSERT -> {
            VLog.a(
                tag,
                saveLog,
                beautify,
                detailed,
                msg
            )
        }
        Log.INFO -> {
            VLog.i(
                tag,
                saveLog,
                showLog,
                beautify,
                detailed,
                msg
            )
        }

        else -> {
            VLog.logDefault(tag, msg)
        }

    }
}
