package com.marknkamau.unipool.ui.driverMap

import com.marknkamau.unipool.domain.LocalRide
import com.marknkamau.unipool.domain.PickUp
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.RideUpdateType
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.mapping.DirectionsHelper
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.mapping.MapHelper
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

class RideHelper(private val mapHelper: MapHelper,
                 private val directionsHelper: DirectionsHelper,
                 private val localStorageService: LocalStorageService,
                 private val repository: ApiRepository,
                 private var vehicleNumber: String,
                 private val endLocation: GeoLocation,
                 private val onError: (Throwable) -> Unit,
                 private val onRideUpdated: (LocalRide, RideUpdateType) -> Unit) {

    var ride: LocalRide? = null
        set(value) {
            value?.let {
                onRideUpdated(value, RideUpdateType.CHANGED)
            }
            field = value
        }

    fun startRide() {
        ride?.let {
            it.started = true
            onRideUpdated(it, RideUpdateType.STARTED)
        }
    }

    fun addStop(offer: RequestOffer) {
        if (ride == null) {
            initializeRide(offer)
        } else {
            // Create an list of using the new latlng as the first element
            val waypoints = mutableListOf(offer.riderLocation.latLng)

            // Add the existing latlngs to the list
            ride!!.pickUps.forEach { pickUp ->
                waypoints.add(pickUp.location.latLng)
            }

            directionsHelper.getDirections(mapHelper.currentLocation.latLng, endLocation.latLng,
                    { result ->
                        ride?.let {
                            val pickUp = PickUp(offer.rider, 0, offer.riderLocation, false)
                            it.pickUps.add(pickUp)
                            it.routeLine = result.routePolylineOpts
                            repository.addPickup(it.rideId, pickUp)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribeBy(
                                            onComplete = {
                                                localStorageService.updateOngoingRide(it)
                                                onRideUpdated(it, RideUpdateType.CHANGED)
                                            },
                                            onError = { throwable ->
                                                onError(throwable)
                                            })
                        }
                    },
                    { throwable ->
                        onError(throwable)
                    }, waypoints)
        }
    }

    fun removeStop(pickUp: PickUp) {
        ride?.let {
            if (it.started) {
                it.pickUps.remove(pickUp)
                repository.removePickUp(it.rideId, pickUp)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onComplete = {
                                    localStorageService.updateOngoingRide(it)
                                    onRideUpdated(it, RideUpdateType.CHANGED)
                                },
                                onError = { throwable ->
                                    onError(throwable)
                                })
            } else {
                onError(Throwable("The ride has not been started!"))
            }
        }
    }

    fun pickUpUser(pickUp: PickUp) {
        ride?.let { internalRide ->
            if (internalRide.started) {
                val find = internalRide.pickUps.find { it.user.userId == pickUp.user.userId }
                find?.let {
                    find.completed = true
                    find.pickUpTime = DateTime.getNow().asTimestamp()
                    find.location = mapHelper.currentLocation
                    repository.setPickUpCompleted(internalRide.rideId, find)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        localStorageService.updateOngoingRide(internalRide)
                                        onRideUpdated(internalRide, RideUpdateType.CHANGED)
                                    },
                                    onError = { throwable ->
                                        onError(throwable)
                                    })
                }
            } else {
                onError(Throwable("The ride has not been started!"))
            }
        }
    }

    fun completeRide() {
        ride?.let {
            var canComplete = true
            it.pickUps.forEach { pickUp: PickUp ->
                if (!pickUp.completed) {
                    canComplete = false
                }
            }
            if (canComplete) {
                it.completed = true
                repository.setRideCompleted(it)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeBy(
                                onComplete = {
                                    localStorageService.deleteOngoingRide()
                                    onRideUpdated(it, RideUpdateType.COMPLETED)
                                },
                                onError = { throwable ->
                                    onError(throwable)
                                })
            } else {
                onError(Throwable("Not all riders have been picked up!"))
            }
        }
    }

    fun cancelRide() {
        ride?.let {
            it.pickUps.clear()
            it.started = false
            it.completed = false
            ride = it
            localStorageService.deleteOngoingRide()
            onRideUpdated(it, RideUpdateType.CANCELLED)
        }
    }

    private fun initializeRide(offer: RequestOffer) {
        val rideId = UUID.randomUUID().toString()

        directionsHelper.getDirections(mapHelper.currentLocation.latLng, endLocation.latLng,
                { result ->
                    val pickUp = PickUp(offer.rider, 0, offer.riderLocation, false)
                    val localRide = LocalRide(rideId, vehicleNumber, mutableListOf(offer.rider), mapHelper.currentLocation, endLocation, mutableListOf(pickUp), result.routePolylineOpts)
                    repository.startRide(localRide, localStorageService.getUser()!!.id, vehicleNumber)
                            .andThen(repository.addPickup(localRide.rideId, pickUp))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onComplete = {
                                        ride = localRide
                                        localStorageService.updateOngoingRide(localRide)
                                        onRideUpdated(localRide, RideUpdateType.CHANGED)
                                    },
                                    onError = { throwable ->
                                        onError(throwable)
                                    })
                },
                { throwable ->
                    onError(throwable)
                }, mutableListOf(offer.riderLocation.latLng))
    }

}