package com.marknkamau.unipool.ui

import io.reactivex.disposables.CompositeDisposable

open class BasePresenter{
    protected val disposables = CompositeDisposable()

    fun dispose(){
        disposables.clear()
    }
}

