package com.marknkamau.unipool.ui.riderMap

import com.marknkamau.unipool.domain.LocalRideRequest
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.UserSimple
import com.marknkamau.unipool.ui.BaseView

interface RiderMapView : BaseView {
    fun requestExists(request: LocalRideRequest)
    fun noRequestExists()
    fun displayOffer(offer: RequestOffer)
    fun setRideStarted(driver: UserSimple)
    fun setRideCancelled()
    fun setRideCompleted()
}