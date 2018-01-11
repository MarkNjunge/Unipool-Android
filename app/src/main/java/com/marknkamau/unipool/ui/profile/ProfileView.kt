package com.marknkamau.unipool.ui.profile

import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.domain.Vehicle
import com.marknkamau.unipool.ui.BaseView

interface ProfileView : BaseView {
    fun userRetrieved(user: User)
    fun userDoesNotExist()
    fun onVehicleSaved(vehicle: Vehicle)
    fun onVehicleDeleted(vehicle: Vehicle)
    fun onVehicleUpdated(vehicle: Vehicle)
}