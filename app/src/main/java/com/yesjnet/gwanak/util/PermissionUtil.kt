package com.yesjnet.gwanak.util

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.KJApplication

object PermissionUtil {

    /**
     * 알림 권한 요청
     */
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestNotifications(logic: () -> Unit) {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }
                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Logger.d("jihoon onPermissionDenied")
                }
            })
            .setDeniedMessage(KJApplication.app.getString(R.string.permission_notification))
            .setDeniedCloseButtonText(KJApplication.app.getString(R.string.close))
            .setGotoSettingButtonText(KJApplication.app.getString(R.string.setting))
            .setPermissions(Manifest.permission.POST_NOTIFICATIONS)
            .check()
    }

    /**
     * 위치 권한 요청
     */
    fun requestLocation(logic: () -> Unit) {
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    logic()
                }
                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Logger.d("jihoon onPermissionDenied")
                }
            })
            .setDeniedMessage(KJApplication.app.getString(R.string.permission_location))
            .setDeniedCloseButtonText(KJApplication.app.getString(R.string.close))
            .setGotoSettingButtonText(KJApplication.app.getString(R.string.setting))
            .setPermissions(*permissions)
            .check()
    }

}