package com.yesjnet.gwanak.fcm

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.EnumApp
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.api.EtcAPI
import com.yesjnet.gwanak.data.model.DeviceInfo
import com.yesjnet.gwanak.data.model.PushData
import com.yesjnet.gwanak.data.net.APIResource
import com.yesjnet.gwanak.data.net.APIResult
import com.yesjnet.gwanak.data.net.ErrorResource
import com.yesjnet.gwanak.data.net.Response
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class MyFirebaseMessagingService: FirebaseMessagingService(), KoinComponent {
    private val etcAPI: EtcAPI by inject()
    private val userInfo: UserInfo by inject()
    private val appInfo: AppInfo by inject()
    private val localNotifyManager: LocalNotificationManager by inject()


    override fun onNewToken(token: String) {
        Logger.d("FCM onNetToken = $token")
        postPushKey(token)
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Logger.d("FCM onMessageReceived data = ${message.data}")

        try {
            var notification = message.notification
            var title: String? = null
            var body: String? = null
            var url: String? = null
            var notificationCount: Int = 0

            notification?.run {
                title = this.title
                body = this.body
                notificationCount = this.notificationCount ?: 0
                Logger.d("onMessageReceived notification.notificationCount = $notificationCount")
            } ?: run {
                title = message.data["title"]
                body = message.data["body"]
                url = message.data["url"]
                notificationCount = message.data["badge"]?.toInt() ?: 0
            }

            val pushData =
                PushData(title, body, url)

            localNotifyManager.showLocalNotification(pushData, notificationCount = notificationCount)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onMessageReceived(message)
    }

    /**
     * 디바이스 정보저장 api
     */
    private fun postPushKey(newToken: String) {
        if (newToken.isEmpty()) {
            Logger.w("[FCM] deviceToken Not postAPI $newToken}")
            return
        }
        val params = mutableMapOf<String, String>().apply {
            this[ConstsData.ReqParam.AGENT] = "1"
            this[ConstsData.ReqParam.USER_ID] = userInfo.getMember()?.userId ?: ""
            this[ConstsData.ReqParam.FCM] = newToken
            Logger.d("jihoon MyFirebaseMessagingService.postMemberDeviceInfos fcm_device_token = $newToken")

        }
        val call = etcAPI.postPushKey(params)
        val response = Response.create(call, object : APIResult<DeviceInfo> {
            override fun onLoading(isLoading: Boolean) {}
            override fun onSuccess(resource: APIResource<DeviceInfo>) {
                if (EnumApp.FlagYN.booleanByStatus(resource.resBase.flag)) {
                    appInfo.storeFCMDeviceToken(newToken)
                }
            }
            override fun onError(errorResource: ErrorResource) {}
        })
        call.enqueue(response)
    }
}