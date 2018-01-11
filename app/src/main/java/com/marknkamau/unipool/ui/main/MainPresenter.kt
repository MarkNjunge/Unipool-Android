package com.marknkamau.unipool.ui.main

import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applySingleSchedulers
import timber.log.Timber

class MainPresenter(private val view: MainView,
                    private val authenticationService: AuthenticationService,
                    private val apiRepository: ApiRepository,
                    private val localStorageService: LocalStorageService)
    : BasePresenter() {

    private var viewCalled = false

    fun getCurrentUser() {
        checkLocalStorage()
    }

    private fun checkLocalStorage() {
        val user = localStorageService.getUser()

        Timber.d("User from local storage: $user")

        if (user != null) {
            view.onUserRetrieved(user)
            viewCalled = true
        }

        checkApi()
    }

    private fun checkApi() {
        val disposable = apiRepository.getUser(authenticationService.currentUserId())
                .compose(applySingleSchedulers())
                .subscribe(
                        { user: User ->
                            Timber.d("Retrieved user from API: $user")
                            updateLocal(user)
                            if (!viewCalled) {
                                view.onUserRetrieved(user)
                            }
                        },
                        { t: Throwable? ->
                            Timber.e(t?.message)
                            Timber.e(t)
                            if (t is NullPointerException) {
                                view.userDoesNotExist()
                            } else {
                                view.displayMessage(t?.message ?: "An error has occurred")
                            }
                        }
                )

        disposables.add(disposable)
    }

    private fun updateLocal(user: User) {
        localStorageService.saveUser(user)
        Timber.d("Saved user to local storage")
    }
}