package com.marknkamau.unipool.ui.login

import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.utils.applySingleSchedulers
import timber.log.Timber

class LoginPresenter(private val view: LogInView, private val authenticationService: AuthenticationService, private val apiRepository: ApiRepository, val localStorageService: LocalStorageService) : BasePresenter() {

    fun getCurrentUser() {
        val disposable = apiRepository.getUser(authenticationService.currentUserId())
                .compose(applySingleSchedulers())
                .subscribe(
                        { user: User ->
                            Timber.d("Retrieved user from API: $user")
                            view.userExists()
                            localStorageService.saveUser(user)
                        },
                        { t: Throwable? ->
                            Timber.e(t?.message)
                            if (t is NullPointerException) {
                                view.userDoesNotExist()
                            } else {
                                view.displayMessage(t?.message ?: "An error has occurred")
                            }
                        }
                )

        disposables.add(disposable)
    }
}
