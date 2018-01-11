package com.marknkamau.unipool.ui.profile

import com.marknkamau.type.Gender
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.Vehicle
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applyCompletableSchedulers
import com.marknkamau.unipool.utils.applySingleSchedulers
import io.reactivex.rxkotlin.subscribeBy

class ProfilePresenter(
        private val view: ProfileView,
        private val localStorageService: LocalStorageService,
        private val apiRepository: ApiRepository,
        private val authenticationService: AuthenticationService)
    : BasePresenter() {

    fun getUser() {
        val user = localStorageService.getUser()

        if (user != null) {
            view.userRetrieved(user)
        } else {
            checkApi()
        }
    }

    fun updateUser(fullName: String, gender: String, phone: Int) {
        val genderProper = when (gender) {
            "Male" -> Gender.M
            else -> Gender.F
        }
        apiRepository.updateUser(authenticationService.currentUserId(), fullName, genderProper, phone)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.displayMessage("You profile has been updated")
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "An error has occurred.")
                        })

    }

    fun saveVehicle(vehicle: Vehicle) {
        apiRepository.addVehicle(authenticationService.currentUserId(), vehicle)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.onVehicleSaved(vehicle)
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "An error has occurred.")
                        })
    }

    fun deleteVehicle(vehicle: Vehicle) {
        apiRepository.deleteVehicle(vehicle.registrationNumber)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.onVehicleDeleted(vehicle)
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "An error has occurred.")
                        })
    }

    fun updateVehicle(vehicle: Vehicle){
        apiRepository.updateVehicle(vehicle.registrationNumber, vehicle.make, vehicle.color, vehicle.capacity)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.onVehicleUpdated(vehicle)
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "An error has occurred.")
                        })
    }

    private fun checkApi() {
        val disposable = apiRepository.getUser(authenticationService.currentUserId())
                .compose(applySingleSchedulers())
                .subscribe(
                        { user: User ->
                            view.userRetrieved(user)
                            localStorageService.saveUser(user)
                        },
                        { t: Throwable? ->
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
