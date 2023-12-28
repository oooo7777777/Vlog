package com.v.demo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.v.log.LogConfig
import com.v.log.VLog
import com.v.log.util.ZipUtils
import com.v.log.util.log
import com.v.log.util.logD
import com.v.log.util.logE
import com.v.log.util.logI
import com.v.log.util.logSave
import com.v.log.util.logW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化日志系统
        VLog.init(LogConfig(this, true))
        VLog.getDefaultLogPath().log("获取当前日志文件夹目录")//获取当前日志文件夹目录
        VLog.getTodayFilePath().log("获取今日日志")//获取今日日志

        writeFile(null)
        //获取所有日志文件
        VLog.getFilesAll().forEach {
            it.log()
        }

        //把日志文件打包成zip 并且返回zip地址
        CoroutineScope(Dispatchers.IO).launch {
            "ZIP 文件路径为${ZipUtils.zip(this@MainActivity)}".log()
        }

    }

    fun writeFile(view: View?) {

        "logD".logD()
        Throwable("测试").logE()
        "错误".logE()
        "logE".logE("hahhahha")
        "logW".logW()
        "logI".logI()
        "只打印3333333333".log()
        "只保存444444444444".logSave()

        JavaDemo().test()

    }

    fun flush(view: View?) {
        VLog.flush()
    }


}