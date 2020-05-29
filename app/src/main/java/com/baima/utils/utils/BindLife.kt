package com.photo.utils

import android.util.Log
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber

interface BindLife {
    val compositeDisposable: CompositeDisposable

    fun Disposable.bindLife() = addDisposable(this)

    fun Single<*>.bindLife() = subscribe({  }, { Log.e("xia", "${it.message} Single has error") }).bindLife()

    fun Observable<*>.bindLife() = subscribe({  }, { Log.e("xia", "${it.message}Observable has error") }).bindLife()

    fun Completable.bindLife() = subscribe({  }, { Timber.e(it, "Completable has error") }).bindLife()

    fun Flowable<*>.bindLife() = subscribe({  }, { Timber.e(it, "Flowable has error") }).bindLife()

    fun addDisposable(disposable: Disposable) {
        compositeDisposable.add(disposable)
    }

    fun removeDisposable(disposable: Disposable?) {
        if (disposable != null)
            compositeDisposable.remove(disposable)
    }

    fun destroyDisposable() = compositeDisposable.clear()
}