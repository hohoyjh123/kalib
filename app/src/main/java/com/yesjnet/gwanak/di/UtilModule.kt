package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.storage.SecurePreference
import org.koin.dsl.module

/**
 * 유틸리티 DI
 */
val utilModule = module {
    single { SecurePreference(get()) }

}