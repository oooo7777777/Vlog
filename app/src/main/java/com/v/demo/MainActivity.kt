package com.v.demo

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.EasyPermissions.PermissionCallbacks
import pub.devrel.easypermissions.EasyPermissions.RationaleCallbacks
import android.os.Bundle
import android.view.View
import com.v.log.VLog
import com.v.log.LogConfig
import pub.devrel.easypermissions.EasyPermissions
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

    fun writeFile(view: View?) {
        if (hasWriteAndReadPermissions()) {
            "hello VLog".logI()
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