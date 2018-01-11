package com.marknkamau.unipool.utils

import android.os.Handler
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


fun extractNameFromEmail(email: String): String {
    val username = email.split("@")[0]
    return if (username.contains(".")) {
        "${username.split(".")[0].capitalize()} ${username.split(".")[1].capitalize()}"
    } else {
        username.capitalize()
    }
}

fun <T> applySingleSchedulers(): SingleTransformer<T, T> {
    return SingleTransformer<T, T> { upstream ->
        upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun applyCompletableSchedulers(): CompletableTransformer {
    return CompletableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }
}

fun runAsync(func: () -> Unit) {
    Thread(Runnable { func() }).start()
}

fun runAfter(delay: Long, func: () -> Unit) {
    Handler().postDelayed(func, delay)
}