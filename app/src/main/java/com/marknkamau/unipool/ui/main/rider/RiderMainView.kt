package com.marknkamau.unipool.ui.main.rider

import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.ui.BaseView

interface RiderMainView : BaseView {
    fun displayScheduledRides(rides: MutableList<ScheduledRide>)
}