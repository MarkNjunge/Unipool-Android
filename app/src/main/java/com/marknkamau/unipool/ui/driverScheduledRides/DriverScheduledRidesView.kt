package com.marknkamau.unipool.ui.driverScheduledRides

import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.ui.BaseView

interface DriverScheduledRidesView : BaseView {
    fun displayScheduledRides(rides: MutableList<ScheduledRide>)
    fun noScheduledRides()
}