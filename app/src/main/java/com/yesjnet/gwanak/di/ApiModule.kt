package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.data.api.EtcAPI
import com.yesjnet.gwanak.data.api.MemberAPI
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * api 모듈
 */
val apiModule = module {
    single(createdAtStart = false) { get<Retrofit>().create(EtcAPI::class.java) }
    single(createdAtStart = false) { get<Retrofit>().create(MemberAPI::class.java) }
}