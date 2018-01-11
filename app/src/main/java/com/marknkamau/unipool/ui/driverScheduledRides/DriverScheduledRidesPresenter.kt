package com.marknkamau.unipool.ui.driverScheduledRides

import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applySingleSchedulers
import io.reactivex.rxkotlin.subscribeBy

class DriverScheduledRidesPresenter(private val view: DriverScheduledRidesView, private val apiRepository: ApiRepository) : BasePresenter() {
    fun getRides() {
        apiRepository.getAllScheduledRides()
                .compose(applySingleSchedulers())
                .subscribeBy(
                        onSuccess = { rides ->
                            if (rides.isEmpty()) {
                                view.noScheduledRides()
                            } else {
                                view.displayScheduledRides(rides)
                            }
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "Unable to get ride requests")
                        })
    }
}