package com.marknkamau.unipool.domain.data.api

import com.marknkamau.type.Gender
import com.marknkamau.unipool.domain.*
import com.marknkamau.unipool.utils.mapping.GeoLocation
import io.reactivex.Completable
import io.reactivex.Single

interface ApiRepository {
    fun addUser(user: User): Completable
    fun getUser(userId: String): Single<User>
    fun updateUser(userId: String, fullName: String, gender: Gender, phone: Int): Completable

    fun addVehicle(userId: String, vehicle: Vehicle): Completable
    fun deleteVehicle(registrationNumber: String): Completable
    fun updateVehicle(registrationNumber: String, make: String, color: String, capacity: Int): Completable

    fun addRequest(userId: String, startLocation: GeoLocation, endLocation: GeoLocation): Completable
    fun removeRequest(userId: String): Completable
    fun getRequest(userId: String): Single<Requesting>
    fun getAllRequests(): Single<MutableList<RideRequest>>

    fun startRide(ride: LocalRide, driverId: String, vehicleRegNo: String): Completable
    fun addPickup(rideId: String, pickUp: PickUp): Completable
    fun removePickUp(rideId: String, pickUp: PickUp): Completable
    fun setPickUpCompleted(rideId: String, pickUp: PickUp): Completable
    fun setRideCompleted(ride: LocalRide): Completable

    fun addScheduledRide(scheduledRide: ScheduledRide, userId: String): Completable
    fun removeScheduledRide(scheduledRide: ScheduledRide): Completable
    fun getScheduledRidesForUser(userId: String): Single<MutableList<ScheduledRide>>
    fun deleteScheduledRide(scheduledRide: ScheduledRide): Completable
    fun getAllScheduledRides(): Single<MutableList<ScheduledRide>>

    fun getUserPastRides(userId: String): Single<MutableList<PastRide>>

}