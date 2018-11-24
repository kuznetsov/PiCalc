package com.apps.elliotgrin.picalc

import android.app.Application
import com.apps.elliotgrin.picalc.di.piCalcApp
import org.koin.android.ext.android.startKoin

class PiCalcApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // start Koin context
        startKoin(this, piCalcApp)
    }
}