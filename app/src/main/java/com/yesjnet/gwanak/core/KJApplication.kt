package com.yesjnet.gwanak.core

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.orhanobut.logger.AndroidLogAdapter
import com.orhanobut.logger.FormatStrategy
import com.orhanobut.logger.PrettyFormatStrategy
import com.yesjnet.gwanak.di.apiModule
import com.yesjnet.gwanak.di.appInfoModule
import com.yesjnet.gwanak.di.networkModule
import com.yesjnet.gwanak.di.repoModule
import com.yesjnet.gwanak.di.utilModule
import com.yesjnet.gwanak.di.viewModelModule
import com.yesjnet.gwanak.util.RSACryptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidFileProperties
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class KJApplication : MultiDexApplication() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    override fun onCreate() {
        super.onCreate()
        app = this
        initLogger()

        startKoin {
            modules(
                apiModule,
                networkModule,
                repoModule,
                appInfoModule,
                viewModelModule,
                utilModule
            )
            androidLogger()
            androidContext(this@KJApplication)
            // load properties from assets/koin.properties file
            androidFileProperties()
        }

        Handler(Looper.getMainLooper()).post {
            RSACryptor.instance.init(this)
            initFirebase()
        }



    }

    private fun initFirebase(){
        FirebaseApp.initializeApp(this)
    }

    private fun initLogger() {
        val formatStrategy: FormatStrategy = PrettyFormatStrategy.newBuilder()
            .showThreadInfo(false)
            .methodCount(1)
            .methodOffset(0)
//            .logStrategy(customLog)
            .tag("galog")
            .build()
        com.orhanobut.logger.Logger.addLogAdapter(object : AndroidLogAdapter(formatStrategy) {
            override fun isLoggable(priority: Int, tag: String?): Boolean {
                return ConstsData.isDevMode()
            }
        })
    }

    companion object {
        lateinit var app: Application
    }

}
