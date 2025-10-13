package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.KJApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.fcm.LocalNotificationManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * 앱정보 모듈
 */
val appInfoModule = module {
    single { AppInfo(androidApplication() as KJApplication, get()) }
    single { UserInfo(androidApplication() as KJApplication, get(), get()) }
    single { KJApplication() }
    single { LocalNotificationManager(androidApplication() as KJApplication,get()) }
}