package com.v.vlog;

import android.Manifest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.v.log.LogConfig;
import com.v.log.VLog;

import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks,
        EasyPermissions.RationaleCallbacks {

    private static final int RC_STORAGE_PERM = 123;
    private static final String[] WRITE_AND_READ_STORAGE =
            {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化日志系统
        VLog.init(new LogConfig(this));
    }

    public void write2file(View view) {
        write2file();
    }

    public void write2file() {
        if (hasWriteAndReadPermissions()) {

            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < 10; i++) {
                sb.append("压夜费第一反应神樱大祓:" + i);
            }
            VLog.i("PRETTY_LOGGER", sb.toString());
            Toast.makeText(MainActivity.this, "success", Toast.LENGTH_LONG).show();
        } else {
            // Ask for both permissions
            EasyPermissions.requestPermissions(
                    MainActivity.this,
                    "申请存储卡选项用于存储",
                    RC_STORAGE_PERM,
                    WRITE_AND_READ_STORAGE);
        }
    }

    private boolean hasWriteAndReadPermissions() {
        return EasyPermissions.hasPermissions(this, WRITE_AND_READ_STORAGE);
    }

    public void flush(View view) {
        VLog.flush();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {

    }

    @Override
    public void onRationaleAccepted(int requestCode) {

    }

    @Override
    public void onRationaleDenied(int requestCode) {

    }

}
