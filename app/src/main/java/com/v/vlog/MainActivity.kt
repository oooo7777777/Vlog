package com.v.vlog

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks
import android.os.Bundle
import android.view.View
import com.v.vlog.R
import com.v.log.VLog
import com.v.log.LogConfig
import pub.devrel.easypermissions.EasyPermissions
import com.v.vlog.MainActivity
import android.widget.Toast
import com.v.log.util.*

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
    }

    fun write2file(view: View?) {
        if (hasWriteAndReadPermissions()) {
            val sb = StringBuffer()
            for (i in 0..9) {
                sb.append("测试11111111111111111111111111:$i")
            }
            sb.logI()
            Toast.makeText(this@MainActivity, "success", Toast.LENGTH_LONG).show()
        }
    }

    fun writeSave(view: View?) {
        if (hasWriteAndReadPermissions()) {
            val sb = StringBuffer()
            for (i in 0..9) {
                sb.append("测试22222222222222222222222$i")
            }
            sb.logI(save = false)
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