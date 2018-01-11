package com.marknkamau.unipool.ui.setUpUser

import com.marknkamau.type.Gender
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.utils.applyCompletableSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class SetUpUserPresenter(val view: SetUpUserView, val localStorageService: LocalStorageService, val authenticationService: AuthenticationService, val apiRepository: ApiRepository) {
    fun addUser(fullName: String, phone: Int, studentNumber: Int, gender: String) {
        val genderProper = when (gender) {
            "Male" -> Gender.M
            else -> Gender.F
        }

        val user = User(authenticationService.currentUserId(),
                studentNumber,
                authenticationService.currentUserEmail(),
                fullName,
                phone,
                genderProper,
                mutableListOf())

        apiRepository.addUser(user)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.onUserCreated()
                            localStorageService.saveUser(user)
                        },
                        onError = { t: Throwable ->
                            view.displayMessage(t.message.toString())
                            Timber.e(t)
                        })

    }
}