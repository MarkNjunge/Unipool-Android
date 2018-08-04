package com.marknkamau.unipool.ui.pastRides

import com.marknkamau.unipool.domain.PastRide
import com.marknkamau.unipool.ui.BaseView

interface PastRidesView : BaseView {
    fun setRides(rides: MutableList<PastRide>)
    fun noRides()
}