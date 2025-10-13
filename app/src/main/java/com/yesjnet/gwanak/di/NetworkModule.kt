package com.yesjnet.gwanak.di

import com.google.gson.GsonBuilder
import com.yesjnet.gwanak.core.ConstsData
import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.data.api.EnumConverterFactory
import com.yesjnet.gwanak.data.net.AppInterceptor
import com.yesjnet.gwanak.data.net.NetInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


/**
 * 네트워크 모듈
 */
private const val CONNECT_TIMEOUT = 15L
private const val WRITE_TIMEOUT = 30L
private const val READ_TIMEOUT = 30L

val networkModule = module {

    single {
        GsonBuilder().setDateFormat(ConstsData.API_DATE_PATTERN).create()
    }

    single {
        OkHttpClient.Builder().apply {
            connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            retryOnConnectionFailure(true)
            addInterceptor(AppInterceptor(get()))
            addInterceptor(NetInterceptor())
//            authenticator(get(named("tokenAthenticator")))
            addInterceptor(HttpLoggingInterceptor().apply {
                level = if (ConstsData.isDevMode()) {
                    HttpLoggingInterceptor.Level.BODY
                } else {
                    HttpLoggingInterceptor.Level.NONE
                }
            })
        }.build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(ConstsData.SERVER_URL_FULL)
            .addConverterFactory(EnumConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .client(get())
            .build()
    }

    single { AppInterceptor(androidApplication() as GAApplication) }
    single { NetInterceptor() }
//    single(named("tokenAthenticator")) { TokenAuthenticator() }

}
