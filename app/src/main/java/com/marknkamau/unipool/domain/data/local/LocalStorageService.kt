package com.marknkamau.unipool.domain.data.local

import com.marknkamau.unipool.domain.*

interface LocalStorageService {
    fun saveUser(user: User)
    fun getUser(): User?
    fun deleteUser()

    fun updateRequestingStatus(requesting: Requesting)
    fun getRequesting(): Requesting

    fun saveRequest(request: LocalRideRequest)
    fun getRequest(): LocalRideRequest?
    fun clearRequest()

    fun getScheduledRides(): MutableList<ScheduledRide>
    fun saveScheduledRide(ride: ScheduledRide)
    fun deleteScheduledRide(ride: ScheduledRide)
    fun setScheduledRides(rides: MutableList<ScheduledRide>)

    fun getOngoingRide(): LocalRide?
    fun updateOngoingRide(ride: LocalRide)
    fun deleteOngoingRide()
}
