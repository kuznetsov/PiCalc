package com.apps.elliotgrin.picalc.algorithm

import com.apps.elliotgrin.chudnovsky.ChudnovskyAlgorithm
import io.reactivex.Observable
import java.lang.Exception

fun piObservable(alg: ChudnovskyAlgorithm): Observable<String> =
        Observable.create { emitter ->
            var k = 1L
            while (true) {
                try {
                    val pi = alg.calculatePi(k, 4, true).toString()
                    emitter.onNext(pi)
                    k++
                } catch (e: Exception) {
                    emitter.onError(e)
                    break
                }
            }
        }