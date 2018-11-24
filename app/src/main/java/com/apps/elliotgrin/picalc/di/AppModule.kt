package com.apps.elliotgrin.picalc.di

import com.apps.elliotgrin.picalc.main.MainViewModel
import org.koin.android.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

val appModule = module {

    viewModel { MainViewModel() }

}

val piCalcApp = listOf(appModule)