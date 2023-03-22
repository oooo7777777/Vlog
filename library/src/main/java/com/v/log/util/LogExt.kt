package com.v.log.util

import com.v.log.VLog

const val TAG = "V_LOG"

fun Any.logD() = run {
    this.toString().showLogD(
        TAG,
        true
    )
}

fun Any.logD(tag: String = TAG) = run {
    this.toString().showLogD(
        tag,
        true
    )
}

fun Any.logD(tag: String = TAG, save: Boolean = true) = run {
    this.toString().showLogD(
        tag,
        save
    )
}

private fun Any.showLogD(tag: String = TAG, save: Boolean = true) = run {
    VLog.d(
        tag,
        save,
        this.toString()
    )
}


/**
 * 此方法会打印日志不会保存日志
 */
fun Any.log() = run {
    this.toString().showLogI(
        TAG,
        save = false,
        show = true
    )
}

/**
 * 此方法保存日志不会打印日志
 */
fun Any.logSave() = run {
    this.toString().showLogI(
        TAG,
        save = true,
        show = false
    )
}


fun Any.logI() = run {
    this.toString().showLogI(
        TAG,
        save = true,
        show = true
    )
}

fun Any.logI(tag: String = TAG) = run {
    this.toString().showLogI(
        tag,
        save = true,
        show = true
    )
}

fun Any.logI(tag: String = TAG, save: Boolean = true) = run {
    this.toString().showLogI(
        tag,
        save
    )
}


private fun Any.showLogI(tag: String = TAG, save: Boolean = true, show: Boolean = true) = run {
    VLog.i(
        tag,
        save,
        show,
        this.toString()
    )
}


fun Any.logW() = run {
    this.toString().showLogW(
        TAG,
        true
    )
}

fun Any.logW(tag: String = TAG) = run {
    this.toString().showLogW(
        tag,
        true
    )
}

fun Any.logW(tag: String = TAG, save: Boolean = true) = run {
    this.toString().showLogW(
        tag,
        save
    )
}


private fun Any.showLogW(tag: String = TAG, save: Boolean = true) = run {
    VLog.w(
        tag,
        save,
        this.toString()
    )
}


fun Any.logE() = run {
    this.toString().showLogE(
        TAG,
        true
    )
}

fun Any.logE(tag: String = TAG) = run {
    this.toString().showLogE(
        tag,
        true
    )
}

fun Any.logE(tag: String = TAG, save: Boolean = true) = run {
    this.toString().showLogE(
        tag,
        save
    )
}

private fun Any.showLogE(tag: String = TAG, save: Boolean = true) = run {
    VLog.e(
        tag,
        save,
        this.toString()
    )
}

