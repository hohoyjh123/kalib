package com.yesjnet.gwanak.fcm

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.orhanobut.logger.Logger
import com.yesjnet.gwanak.R
import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.ConstsApp
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.data.model.PushData
import com.yesjnet.gwanak.extension.getColorCompat
import com.yesjnet.gwanak.storage.SecurePreference
import com.yesjnet.gwanak.ui.IntroActivity
import me.leolin.shortcutbadger.ShortcutBadger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
/**
 * 알림 매니저
 */
class LocalNotificationManager(
    private val application: GAApplication,
    private val mSecPref: SecurePreference
): KoinComponent {

    private val appInfo: AppInfo by inject()
    private val userInfo: UserInfo by inject()

    init {
        Logger.d("[FCM] NotificationManager init")
    }

    fun showLocalNotification(data: PushData, notificationCount: Int = 0, chatMessageCount: Int = 0) {
        Logger.d("[FCM] showLocalNotification($data)")

        when {
            data.title.isNullOrBlank() && data.message.isNullOrBlank() -> {
                return
            } else -> {
                val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val notificationTrampolineActivityIntent =  Intent(application, IntroActivity::class.java).putExtra(ConstsApp.IntentCode.PUSHDATA, data).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

                    // Create the TaskStackBuilder
                    val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(application).run {
                        // Add the intent, which inflates the back stack
                        addNextIntentWithParentStack(notificationTrampolineActivityIntent)
                        // Get the PendingIntent containing the entire back stack
                        getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                    }

                    NotificationCompat.Builder(application, application.getString(R.string.noti_channel_id))
                        .setColor(application.getColorCompat(R.color.notify_color))
                        .setSmallIcon(R.mipmap.small_icon_w)
//                        .setLargeIcon(
//                            BitmapFactory.decodeResource(
//                                application.resources,
//                                R.drawable.ic_launcher
//                            )
//                        )
                        .setTicker(data.title)
                        .setContentTitle(data.title)
                        .setContentText(data.message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentIntent(resultPendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                } else {
                    val intent = Intent(application, PushNotifyReceiver::class.java)
                    intent.putExtra(ConstsApp.IntentCode.PUSHDATA, data)

                    val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        PendingIntent.getBroadcast(
                            application,
                            0,
                            intent,
                            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    } else {
                        PendingIntent.getBroadcast(
                            application,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                        )
                    }

                    NotificationCompat.Builder(application, application.getString(R.string.noti_channel_id))
                        .setColor(application.getColorCompat(R.color.notify_color))
                        .setSmallIcon(R.mipmap.small_icon_w)
//                        .setLargeIcon(
//                            BitmapFactory.decodeResource(
//                                application.resources,
//                                R.drawable.ic_launcher
//                            )
//                        )
                        .setTicker(data.title)
                        .setContentTitle(data.title)
                        .setContentText(data.message)
                        .setWhen(System.currentTimeMillis())
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                }
                ShortcutBadger.applyCount(application, notificationCount)

                val notificationManager =
                    application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        application.getString(R.string.noti_channel_id),
                        application.resources.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    notificationManager.createNotificationChannel(channel)
                }

                builder.setStyle(NotificationCompat.BigTextStyle().bigText(data.message))
                notificationManager.notify(
                    ConstsApp.IntentCode.NOTIFICATION_TAG,
                    System.currentTimeMillis().toInt(),
                    builder.build()
                )
            }
        }

    }

    fun cancelAllNotification() {
        val notificationManager =
            application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
        ShortcutBadger.applyCount(application, 0)
//        mSecPref.setConfigInt(ConstsData.PrefCode.APP_NEW_BADGE_CNT, 0)
    }
}