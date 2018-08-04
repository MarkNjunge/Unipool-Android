package com.marknkamau.unipool.domain

import android.support.annotation.Keep
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.marknkamau.type.Gender
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.mapping.GeoLocation

@Keep
data class User(val id: String, val studentNumber: Int, val email: String, val fullName: String, val phone: Int, val gender: Gender, val vehicles: MutableList<Vehicle>)

@Keep
data class UserSimple(val userId: String, val fullname: String, var phone: Int = 0)

@Keep
class Vehicle(val registrationNumber: String, val make: String, val color: String, val capacity: Int)

data class PickUp(val user: UserSimple, var pickUpTime: Long = 0, var location: GeoLocation, var completed: Boolean)

data class Requesting(val status: Boolean, val locations: Pair<GeoLocation, GeoLocation>?)

data class LocalRideRequest(val origin: GeoLocation, val destination: GeoLocation, val distance: Long)

data class RideRequest(val userId: String, val userName: String, val origin: GeoLocation, val destination: GeoLocation)

@Keep
data class ScheduledRide(val rideId: String, val user: UserSimple, val startLocation: GeoLocation, val endLocation: GeoLocation, val dateTime: DateTime, val driver: UserSimple? = null)

enum class OfferResponseType {
    ACCEPTED,
    REJECTED
}

data class LocalRide(val rideId: String,
                     val vehicleNumber: String,
                     val riders: MutableList<UserSimple>,
                     val startLocation: GeoLocation,
                     val endLocation: GeoLocation,
                     val pickUps: MutableList<PickUp>,
                     var routeLine: PolylineOptions,
                     var started: Boolean = false,
                     var completed: Boolean = false) {

    override fun toString(): String {
        return Gson().toJson(this)
    }
}

@Keep
data class RequestOffer(val offerID: String,
                        val driver: UserSimple,
                        val rider: UserSimple,
                        val riderLocation: GeoLocation,
                        val pricing: Int,
                        var accepted: Boolean = false) {

    fun toJson(): String = Gson().toJson(this)

    override fun toString(): String {
        return "RequestOffer(offerID='$offerID', driver=$driver, rider=$rider, riderLocation=$riderLocation, pricing=$pricing, accepted=$accepted)"
    }

    companion object {
        fun fromJson(json: String): RequestOffer {
            return Gson().fromJson<RequestOffer>(json, RequestOffer::class.java)
        }
    }
}

data class PastRide(val startLocation: String,
                    val endLocation: String,
                    val depatureTime: DateTime,
                    val arrivalTime: DateTime?,
                    val vehicle: String,
                    val driver: String)

enum class RideUpdateType {
    STARTED,
    CHANGED,
    COMPLETED,
    CANCELLED;
}