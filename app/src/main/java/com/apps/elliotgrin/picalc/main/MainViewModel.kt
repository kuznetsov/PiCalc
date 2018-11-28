package com.apps.elliotgrin.picalc.main

import android.arch.lifecycle.MutableLiveData
import com.apps.elliotgrin.chudnovsky.ChudnovskyAlgorithm
import com.apps.elliotgrin.picalc.algorithm.piObservable
import com.apps.elliotgrin.picalc.base.BaseViewModel
import io.reactivex.schedulers.Schedulers

class MainViewModel : BaseViewModel() {

    val piNumberData: MutableLiveData<String?> by lazy { MutableLiveData<String?>() }

    fun run() {
        disposables.clear()

        launch {
            piObservable(ChudnovskyAlgorithm())
                .subscribeOn(Schedulers.computation())
                .doOnNext { piNumberData.postValue(it) }
                .doOnError { piNumberData.postValue(null) }
                .subscribe()
        }
    }

}

private data class State(val k: Int, val pi: String)
