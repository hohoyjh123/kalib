package com.yesjnet.gwanak.fcm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsApp
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.ui.IntroActivity
import com.yesjnet.gwanak.ui.main.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * fcm 리시버
 */

class PushNotifyReceiver : BroadcastReceiver(), KoinComponent {

    private val appInfo: AppInfo by inject()
    private val userInfo: UserInfo by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val data = intent.data
        val pushData = intent.getSerializableExtra(ConstsApp.IntentCode.PUSHDATA)
        val landing = intent.getStringExtra("landing") ?: ""
        Logger.d("[FCM] onReceive intent.pushData = $pushData / $data / $landing")

        var isForegroundFlags = appInfo.isForegroundAppProcess(context)

        if (!isForegroundFlags) {
            val actIntent = Intent(context, IntroActivity::class.java)
            actIntent.putExtra(ConstsApp.IntentCode.PUSHDATA, pushData)
            actIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(actIntent)
        } else {
            val actIntent = Intent(context, MainActivity::class.java)
            actIntent.putExtra(ConstsApp.IntentCode.PUSHDATA, pushData)
            actIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(actIntent)
        }

        Logger.d("pref.getConfigString(ConstsData.PrefCode.IS_PUSH_APP_LAUNCHER) = push")
    }

}