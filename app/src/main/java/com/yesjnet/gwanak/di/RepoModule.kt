package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.data.repository.EtcRepository
import com.yesjnet.gwanak.data.repository.MemberRepository
import org.koin.dsl.module

/**
 * 저장소 모듈
 */
val repoModule = module {
    single { EtcRepository(get()) }
    single { MemberRepository(get()) }
}
