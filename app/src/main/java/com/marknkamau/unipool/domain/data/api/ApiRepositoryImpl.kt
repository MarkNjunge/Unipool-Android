package com.marknkamau.unipool.domain.data.api

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.rx2.Rx2Apollo
import com.marknkamau.*
import com.marknkamau.fragment.ScheduledRideData
import com.marknkamau.type.Gender
import com.marknkamau.type.GeoLocationInput
import com.marknkamau.unipool.domain.*
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.mapping.GeoLocation
import io.reactivex.Completable
import io.reactivex.Single
import java.text.DecimalFormat

class ApiRepositoryImpl(val client: ApolloClient) : ApiRepository {
    override fun addUser(user: User): Completable {
        val addUserMutation = AddUserMutation.builder()
                .id(user.id)
                .studentNumber(user.studentNumber)
                .fullName(user.fullName)
                .email(user.email)
                .gender(user.gender)
                .phone(user.phone)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(
                        client.mutate(addUserMutation)
                )
        )
    }

    override fun getUser(userId: String): Single<User> {
        val getUserQuery = GetUserQuery.builder().userId(userId).build()

        return Single.fromObservable(Rx2Apollo.from(client.query(getUserQuery)))
                .map { t ->
                    val getUser = t.data()?.user
                    if (getUser == null) {
                        null
                    } else {
                        User(
                                getUser._id() ?: "null",
                                getUser.studentNumber() ?: 0,
                                getUser.email() ?: "null",
                                getUser.fullName() ?: "null",
                                getUser.phone() ?: 0,
                                getUser.gender() ?: Gender.M,
                                getUser.vehicles()?.map { vehicle ->
                                    Vehicle(
                                            vehicle.registrationNumber() ?: "null",
                                            vehicle.make() ?: "null",
                                            vehicle.color() ?: "null",
                                            vehicle.capacity() ?: 0
                                    )
                                }?.toMutableList() ?: mutableListOf()
                        )
                    }
                }
    }

    override fun updateUser(userId: String, fullName: String, gender: Gender, phone: Int): Completable {
        val updateUserMutation = UpdateUserMutation.builder().id(userId).fullName(fullName).gender(gender).phone(phone).build()

        return Completable.fromObservable(
                Rx2Apollo.from(
                        client.mutate(updateUserMutation)
                )
        )
    }

    override fun addVehicle(userId: String, vehicle: Vehicle): Completable {
        val addVehicleMutation = AddVehicleMutation.builder()
                .userId(userId)
                .registrationNumber(vehicle.registrationNumber)
                .capacity(vehicle.capacity)
                .color(vehicle.color)
                .make(vehicle.make)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(
                        client.mutate(addVehicleMutation)
                )
        )
    }

    override fun deleteVehicle(registrationNumber: String): Completable {
        val deleteVehicleMutation = DeleteVehicleMutation.builder().registrationNumber(registrationNumber).build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(deleteVehicleMutation))
        )
    }

    override fun updateVehicle(registrationNumber: String, make: String, color: String, capacity: Int): Completable {
        val updateVehicleMutation = UpdateVehicleMutation.builder()
                .registrationNumber(registrationNumber)
                .make(make)
                .color(color)
                .capacity(capacity)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(updateVehicleMutation))
        )
    }

    override fun addRequest(userId: String, startLocation: GeoLocation, endLocation: GeoLocation): Completable {
        val addRequestMutation = AddRequestMutation.builder()
                .userId(userId)
                .startLocation(startLocation.mapToInput())
                .endLocation(endLocation.mapToInput())
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(addRequestMutation))
        )
    }

    override fun removeRequest(userId: String): Completable {
        val removeRequestMutation = RemoveRequestMutation.builder()
                .userId(userId)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(removeRequestMutation))
        )
    }

    override fun getRequest(userId: String): Single<Requesting> {
        val getRequestByUserQuery = GetRequestByUserQuery.builder()
                .userId(userId)
                .build()

        return Single.fromObservable(
                Rx2Apollo.from(client.query(getRequestByUserQuery))
                        .map { response ->
                            val data = response.data()?.requestsByUser
                            if (data == null) {
                                Requesting(false, null)
                            } else {
                                Requesting(true, Pair(data.startLocation()!!.convert(), data.endLocation()!!.convert()))

                            }
                        }
        )
    }

    override fun getAllRequests(): Single<MutableList<RideRequest>> {
        val getAllRequestsQuery = GetAllRequestsQuery.builder()
                .build()

        return Single.fromObservable(
                Rx2Apollo.from(client.query(getAllRequestsQuery))
                        .map { response ->
                            val mapped = mutableListOf<RideRequest>()

                            val allRequests = response.data()?.allRequests
                            allRequests?.forEach {
                                val request = RideRequest(it.user()?._id()!!, it.user()?.fullName()!!, it.startLocation()!!.convert(), it.endLocation()!!.convert())
                                mapped.add(request)
                            }

                            mapped
                        }
        )
    }

    override fun startRide(ride: LocalRide, driverId: String, vehicleRegNo: String): Completable {
        val startRideMutation = StartRideMutation.builder()
                .rideId(ride.rideId)
                .driverId(driverId)
                .vehicleRegNo(vehicleRegNo)
                .locationName(ride.startLocation.name)
                .latitude(ride.startLocation.latitude)
                .longitude(ride.startLocation.longitude)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(startRideMutation))
        )
    }

    override fun addPickup(rideId: String, pickUp: PickUp): Completable {
        val addPickUpMutation = AddPickUpMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .locationName(pickUp.location.name)
                .latitude(pickUp.location.latitude)
                .longitude(pickUp.location.longitude)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(addPickUpMutation))
        )
    }

    override fun removePickUp(rideId: String, pickUp: PickUp): Completable {
        val removePickUpMutation = RemovePickUpMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(removePickUpMutation))
        )
    }

    override fun setPickUpCompleted(rideId: String, pickUp: PickUp): Completable {
        val setPickUpCompletedMutation = SetPickUpCompletedMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .locationName(pickUp.location.name)
                .latitude(pickUp.location.latitude)
                .longitude(pickUp.location.longitude)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(setPickUpCompletedMutation))
        )
    }

    override fun setRideCompleted(ride: LocalRide): Completable {
        val markRideAsCompletedMutation = MarkRideAsCompletedMutation.builder()
                .rideId(ride.rideId)
                .locationName(ride.endLocation.name)
                .latitude(ride.endLocation.latitude)
                .longitude(ride.endLocation.longitude)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(markRideAsCompletedMutation))
        )
    }

    override fun addScheduledRide(scheduledRide: ScheduledRide, userId: String): Completable {
        val scheduleRideMutation = ScheduleRideMutation.builder()
                .rideId(scheduledRide.rideId)
                .userId(userId)
                .startLocation(scheduledRide.startLocation.mapToInput())
                .endLocation(scheduledRide.endLocation.mapToInput())
                .depatureTime(scheduledRide.dateTime.asTimestamp().toDouble())
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(scheduleRideMutation))
        )
    }

    override fun removeScheduledRide(scheduledRide: ScheduledRide): Completable {
        val deleteScheduledRideMutation = DeleteScheduledRideMutation.builder()
                .rideId(scheduledRide.rideId)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(deleteScheduledRideMutation))
        )
    }

    override fun getScheduledRidesForUser(userId: String): Single<MutableList<ScheduledRide>> {
        val getScheduledRidesForUserQuery = GetScheduledRidesForUserQuery.builder()
                .userId(userId)
                .build()

        return Single.fromObservable(Rx2Apollo.from(client.query(getScheduledRidesForUserQuery)))
                .map { response ->
                    val result = mutableListOf<ScheduledRide>()
                    val data = response.data()?.scheduledRidesForUser
                    data?.forEach { item ->
                        val scheduledRideData = item.fragments().scheduledRideData()
                        val scheduledRide = ScheduledRide(
                                scheduledRideData.rideId(),
                                UserSimple(scheduledRideData.user()._id()!!, scheduledRideData.user().fullName()!!, scheduledRideData.user().phone()!!),
                                scheduledRideData.startLocation().convert(),
                                scheduledRideData.endLocation().convert(),
                                DateTime.fromTimestamp(scheduledRideData.depatureTime().toLong())
                        )
                        result.add(scheduledRide)
                    }

                    result
                }
    }

    override fun deleteScheduledRide(scheduledRide: ScheduledRide): Completable {
        val deleteScheduledRideMutation = DeleteScheduledRideMutation.builder()
                .rideId(scheduledRide.rideId)
                .build()

        return Completable.fromObservable(
                Rx2Apollo.from(client.mutate(deleteScheduledRideMutation))
        )
    }

    override fun getAllScheduledRides(): Single<MutableList<ScheduledRide>> {
        val getScheduledRidesQuery = GetScheduledRidesQuery.builder().build()

        return Single.fromObservable(Rx2Apollo.from(client.query(getScheduledRidesQuery)))
                .map { response ->
                    val result = mutableListOf<ScheduledRide>()
                    val data = response.data()?.allScheduledRides
                    data?.forEach { item ->
                        val scheduledRideData = item.fragments().scheduledRideData()
                        val scheduledRide = ScheduledRide(
                                scheduledRideData.rideId(),
                                UserSimple(scheduledRideData.user()._id()!!, scheduledRideData.user().fullName()!!, scheduledRideData.user().phone()!!),
                                scheduledRideData.startLocation().convert(),
                                scheduledRideData.endLocation().convert(),
                                DateTime.fromTimestamp(scheduledRideData.depatureTime().toLong())
                        )
                        result.add(scheduledRide)
                    }

                    result
                }
    }

    override fun getUserPastRides(userId: String): Single<MutableList<PastRide>> {
        val getUserRidesQuery = GetUserRidesQuery.builder().userId(userId).build()

        return Single.fromObservable(Rx2Apollo.from(client.query(getUserRidesQuery)))
                .map { response ->
                    val returnList = mutableListOf<PastRide>()
                    val data = response.data()?.ridesByUser

                    data?.forEach { ride ->
                        val decimalFormat = DecimalFormat("#.##")
                        val formattedDepartureTime = decimalFormat.format(ride.departureTime())
                        val departureTime = DateTime.fromTimestamp(formattedDepartureTime.toLong())
                        val arrivalTimeRaw = ride.arrivalTime()

                        val arrivalTime = if (arrivalTimeRaw == null) {
                            null
                        } else {
                            val formattedArrivalTime = decimalFormat.format(arrivalTimeRaw.toDouble())
                            DateTime.fromTimestamp(formattedArrivalTime.toLong())
                        }

                        val pastRide = PastRide(
                                ride.startLocation()?.name().toString(),
                                ride.endLocation()?.name().toString(),
                                departureTime,
                                arrivalTime,
                                ride.vehicle()?.registrationNumber().toString(),
                                ride.driver()?.fullName().toString())

                        returnList.add(pastRide)
                    }

                    returnList
                }
    }

    private fun ScheduledRideData.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun ScheduledRideData.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun GetRequestByUserQuery.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun GetRequestByUserQuery.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun GetAllRequestsQuery.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun GetAllRequestsQuery.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name() ?: "null", this.latitude(), this.longitude())
    }

    private fun GeoLocation.mapToInput(): GeoLocationInput
            = GeoLocationInput.builder()
            .name(this.name)
            .latitude(this.latitude)
            .longitude(this.longitude)
            .build()

}