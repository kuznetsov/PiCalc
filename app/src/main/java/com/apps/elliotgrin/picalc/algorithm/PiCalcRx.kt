package com.apps.elliotgrin.picalc.algorithm

import com.apps.elliotgrin.chudnovsky.calculatePi
import io.reactivex.Observable
import java.lang.Exception

val piObservable: Observable<String> =
        Observable.create { emitter ->
            var k = 1L
            while (true) {
                try {
                    val pi = calculatePi(k, 4).toString()
                    emitter.onNext(pi)
                    k++
                } catch (e: Exception) {
                    emitter.onError(e)
                    break
                }
            }
        }