package com.marknkamau.unipool.ui.driverMap

import com.marknkamau.unipool.domain.RideRequest
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.ui.BaseView

interface DriverMapView : BaseView {
    fun requestReceived(rideRequests: MutableList<RideRequest>)
    fun noRequests()
    fun requestAccepted(offer: RequestOffer)
}