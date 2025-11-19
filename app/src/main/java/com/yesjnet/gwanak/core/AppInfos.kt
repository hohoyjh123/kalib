package com.yesjnet.gwanak.core

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Build.VERSION
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.BuildConfig
import com.yesjnet.gwanak.data.model.LoginInfo
import com.yesjnet.gwanak.data.model.MemberInfo
import com.yesjnet.gwanak.storage.SecurePreference
import java.util.Locale

/**
 *  앱정보 유저정보 싱글톤 클래스
 */

class AppInfo(private val application: GAApplication, private val mSecPref: SecurePreference) {

    val appName: String by lazy {
        "galib"
    }

    val versionCode: Int by lazy {
        val pInfo: PackageInfo
        var code = 0
        try {
            val context = application.applicationContext
            pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            code = pInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        code
    }

    val versionName: String by lazy {
        val pInfo: PackageInfo
        var vname = "0"
        try {
            val context = application.applicationContext
            pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            vname = pInfo.versionName.toString()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        vname
    }

    val languageCode: String by lazy {
        val locale: Locale =
            application.resources.configuration.locale
        locale.language.lowercase(Locale.getDefault())
    }

    val countryCode: String by lazy {
        val locale: Locale =
            application.resources.configuration.locale
        locale.country
    }

    val userAgent: String by lazy {
        String.format(
            "%s/%s-%s (%s(%s); Android %s; %s-%s)",
            appName,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            Build.MODEL,
            Build.DEVICE,
            VERSION.RELEASE,
            languageCode,
            countryCode
        )
    }

    var adid: String? = null

    fun getFCMDeviceToken(): String {
        val token = mSecPref.getStrValue(ConstsData.PrefCode.FCM_DEVICE_TOKEN, "")
        Logger.d("[FCM] get deviceToken = $token")
        return token
    }

    fun storeFCMDeviceToken(deviceToken: String): String {
        // Log and toast
        val msg = "[FCM] save deviceToken = $deviceToken"
        Logger.d(msg)
        mSecPref.setStrValue(ConstsData.PrefCode.FCM_DEVICE_TOKEN, deviceToken)
        return deviceToken
    }

    /**
     * 자동 로그인 저장
     */
    fun setLoginInfo(userId: String, userPwd: String) {
        mSecPref.setConfigString(ConstsData.PrefCode.USER_ID, userId, true)
        mSecPref.setConfigString(ConstsData.PrefCode.USER_PWD, userPwd, true)
    }

    /**
     * 자동 로그인 정보 삭제
     */
    fun removeLoginInfo(){
        Logger.d("removeLoginInfo")
        mSecPref.setConfigString(ConstsData.PrefCode.USER_ID, "")
        mSecPref.setConfigString(ConstsData.PrefCode.USER_PWD, "")
    }

    /**
     * 자동 로그인 정보 가져오기
     */
    fun getLoginInfo(): LoginInfo? {
        val userId = mSecPref.getConfigString(ConstsData.PrefCode.USER_ID,"", true)
        val userPwd = mSecPref.getConfigString(ConstsData.PrefCode.USER_PWD,"", true)
        return if(userId.isNullOrBlank() || userPwd.isNullOrBlank()){
            Logger.i("LoginInfo is null ")
            null
        } else {
            val token = LoginInfo(userId, userPwd)
            Logger.i("LoginInfo is %s",token)
            token
        }
    }

    fun isAvailableGoogleAPi(): Boolean {
        return GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(application.applicationContext) == ConnectionResult.SUCCESS
    }

    fun isForegroundAppProcess(context: Context): Boolean {
        var isForegroundFlags = false
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        isForegroundFlags = true
                        break
                    }
                }
            }
        }
        Logger.d("[AppLifecycle] isForegroundAppProcess = $isForegroundFlags")
        return isForegroundFlags
    }
}

class UserInfo(
    private val application: GAApplication,
    private val mSecPref: SecurePreference,
    private val appInfo: AppInfo,
) {

    private var memberInfo: MemberInfo? = null

    init {
        Logger.d("")
    }

    fun getMember() : MemberInfo?{
        val jsonInfo = mSecPref.getConfigString(ConstsData.PrefCode.USER_MEMBER_INFO,"")
        if(memberInfo == null && jsonInfo.isNotBlank()){
            return try {
                Gson().fromJson(jsonInfo, MemberInfo::class.java)
            } catch (e: JsonSyntaxException){
                null
            }
        }
        return memberInfo
    }

    fun setMember(memberInfo: MemberInfo){
        this.memberInfo = memberInfo
        // Preference
        val jsonMemInfo = Gson().toJson(memberInfo)
        mSecPref.setConfigString(ConstsData.PrefCode.USER_MEMBER_INFO,jsonMemInfo)
    }

    fun clearUserInfo() {
        this.memberInfo = null
        setMember(MemberInfo())
    }

}