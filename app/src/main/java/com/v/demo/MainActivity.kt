package com.v.demo

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.v.log.LogConfig
import com.v.log.VLog
import com.v.log.util.*
import pub.devrel.easypermissions.EasyPermissions
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks

class MainActivity : AppCompatActivity(), PermissionCallbacks, RationaleCallbacks {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //初始化日志系统
        VLog.init(LogConfig(this, true))
        EasyPermissions.requestPermissions(
            this@MainActivity,
            "申请存储卡选项用于存储",
            RC_STORAGE_PERM,
            *WRITE_AND_READ_STORAGE
        )
        VLog.getDefaultLogPath().log()//获取当前日志文件夹目录
        VLog.getTodayFilePath().log()//获取今日日志
        //获取所有日志文件
        VLog.getFilesAll().forEach {
            it.log()
        }
    }

    fun writeFile(view: View?) {
        if (hasWriteAndReadPermissions()) {
            "logD".logD()
            Throwable("测试").logE()
            "错误".logE()
            "logE".logE("hahhahha")
            "logW".logW()
            "logI".logI()
            "只打印3333333333".log()
            "只保存444444444444".logSave()

            JavaDemo().test()

            Toast.makeText(this@MainActivity, "success", Toast.LENGTH_LONG).show()

        }
    }

    private fun hasWriteAndReadPermissions(): Boolean {
        return EasyPermissions.hasPermissions(this, *WRITE_AND_READ_STORAGE)
    }

    fun flush(view: View?) {
        VLog.flush()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {}
    override fun onRationaleAccepted(requestCode: Int) {}
    override fun onRationaleDenied(requestCode: Int) {}

    companion object {
        private const val RC_STORAGE_PERM = 123
        private val WRITE_AND_READ_STORAGE = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}