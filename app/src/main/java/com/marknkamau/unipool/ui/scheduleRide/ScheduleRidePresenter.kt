package com.marknkamau.unipool.ui.scheduleRide

import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.UserSimple
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.applyCompletableSchedulers
import com.marknkamau.unipool.utils.mapping.GeoLocation
import io.reactivex.rxkotlin.subscribeBy
import java.util.*

class ScheduleRidePresenter(val view: ScheduleRideView,
                            val auth: AuthenticationService,
                            val localStorageService: LocalStorageService,
                            val apiRepository: ApiRepository)
    : BasePresenter() {

    fun scheduleRide(startLocation: GeoLocation, endLocation: GeoLocation, time: DateTime) {
        val rideId = UUID.randomUUID().toString()
        val user: User = localStorageService.getUser()!!
        val ride = ScheduledRide(rideId, UserSimple(user.id, user.fullName, user.phone), startLocation, endLocation, time)
        apiRepository.addScheduledRide(ride, localStorageService.getUser()!!.id)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            view.onRideScheduled()
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "Unable to schedule ride")
                        }
                )
    }
}