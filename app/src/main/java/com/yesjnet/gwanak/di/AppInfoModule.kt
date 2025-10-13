package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.core.AppInfo
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.core.UserInfo
import com.yesjnet.gwanak.fcm.LocalNotificationManager
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

/**
 * 앱정보 모듈
 */
val appInfoModule = module {
    single { AppInfo(androidApplication() as GAApplication, get()) }
    single { UserInfo(androidApplication() as GAApplication, get(), get()) }
    single { GAApplication() }
    single { LocalNotificationManager(androidApplication() as GAApplication,get()) }
}