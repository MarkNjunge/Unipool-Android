package com.marknkamau.unipool.ui.main.rider

import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applyCompletableSchedulers
import com.marknkamau.unipool.utils.applySingleSchedulers
import io.reactivex.rxkotlin.subscribeBy

class RiderMainPresenter(private val view: RiderMainView, private val localStorageService: LocalStorageService, private val apiRepository: ApiRepository) : BasePresenter() {
    fun getScheduledRides() {
        val scheduledRides = localStorageService.getScheduledRides()
        view.displayScheduledRides(scheduledRides)

        val user = localStorageService.getUser()

        user?.let {
            disposables.add(apiRepository.getScheduledRidesForUser(it.id)
                    .compose(applySingleSchedulers())
                    .subscribeBy(
                            onSuccess = { rides ->
                                view.displayScheduledRides(rides)
                                localStorageService.setScheduledRides(rides)
                            },
                            onError = { throwable ->
                                view.displayMessage(throwable.message
                                        ?: "Unable to get scheduled rides")
                            }
                    ))
        }
    }

    fun deleteRide(ride: ScheduledRide) {
        disposables.add(apiRepository.deleteScheduledRide(ride)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            getScheduledRides()
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message
                                    ?: "Unable to get scheduled rides")
                        }
                ))
    }
}