package com.apps.elliotgrin.picalc.algorithm

import com.apps.elliotgrin.chudnovsky.ChudnovskyAlgorithm
import io.reactivex.Observable
import java.lang.Exception

fun piObservable(alg: ChudnovskyAlgorithm): Observable<String> =
        Observable.create { emitter ->
            var k = 1L
            while (!emitter.isDisposed) {
                try {
                    val pi = alg.calculatePi(k, true).toString()
                    if (!emitter.isDisposed) emitter.onNext(pi)
                    k++
                } catch (e: Exception) {
                    if (!emitter.isDisposed) emitter.onError(e)
                    break
                }
            }
        }