package com.v.demo

import android.app.Application
import com.v.log.VLogConfig
import com.v.log.VLog

/**
 * author  : ww
 * desc    :
 * time    : 2024/3/21 11:19
 */
class MyApplication : Application() {

    companion object {
        private lateinit var context: MyApplication
        fun getApplication(): Application = context
    }

    override fun onCreate() {
        super.onCreate()
        context = this
        VLog.init(
            VLogConfig(this, BuildConfig.DEBUG, true)
                .setEnableLogInspector(true)
        )
    }
}
