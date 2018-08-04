package com.marknkamau.unipool.ui.pastRides

import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applySingleSchedulers
import io.reactivex.rxkotlin.subscribeBy
import timber.log.Timber

class PastRidesPresenter(val view: PastRidesView, val localStorageService: LocalStorageService, val apiRepository: ApiRepository) : BasePresenter() {

    fun getRides() {
        val user = localStorageService.getUser()
        user?.let {
            disposables.add(apiRepository.getUserPastRides(user.id)
                    .compose(applySingleSchedulers())
                    .subscribeBy(
                            onSuccess = { rides ->
                                if (rides.isEmpty()) {
                                    view.noRides()
                                } else {
                                    rides.sortBy { it.arrivalTime?.asTimestamp() }
                                    view.setRides(rides)
                                }
                            },
                            onError = { throwable ->
                                Timber.e(throwable)
                                view.displayMessage(throwable.message ?: "Unable to get past rides")
                            }

                    ))
        }
    }
}