package com.yesjnet.gwanak.di

import com.yesjnet.gwanak.core.GAApplication
import com.yesjnet.gwanak.ui.IntroViewModel
import com.yesjnet.gwanak.ui.main.LoginViewModel
import com.yesjnet.gwanak.ui.main.MainViewModel
import com.yesjnet.gwanak.ui.main.SettingViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * 뷰모델 모듈
 */
val viewModelModule = module {
    // LoginAuth
    viewModel { IntroViewModel(androidApplication() as GAApplication, get(), get(), get(), get(), get()) }

    // main
    viewModel { MainViewModel(androidApplication() as GAApplication, get(), get(), get(), get(), get()) }
    viewModel { LoginViewModel(androidApplication() as GAApplication, get(), get(), get(), get(), get()) }
    viewModel { SettingViewModel(androidApplication() as GAApplication, get(), get(), get(), get(), get()) }

}