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
import io.reactivex.Observable
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

        return Rx2Apollo.from(client.prefetch(addUserMutation))
    }

    override fun getUser(userId: String): Single<User> {
        val getUserQuery = GetUserQuery.builder()
                .userId(userId)
                .build()

        return Rx2Apollo.from(client.query(getUserQuery))
                .toSingle()
                .map { t ->
                    val getUser = t.data()?.user
                    if (getUser == null) {
                        null
                    } else {
                        val vehicles = getUser.vehicles().map { vehicle ->
                            Vehicle(
                                    vehicle.registrationNumber(),
                                    vehicle.make(),
                                    vehicle.color(),
                                    vehicle.capacity()
                            )
                        }.toMutableList()

                        User(
                                getUser._id(),
                                getUser.studentNumber(),
                                getUser.email(),
                                getUser.fullName(),
                                getUser.phone(),
                                getUser.gender(),
                                vehicles
                        )
                    }
                }
    }

    override fun updateUser(userId: String, fullName: String, gender: Gender, phone: Int): Completable {
        val updateUserMutation = UpdateUserMutation.builder()
                .id(userId)
                .fullName(fullName)
                .gender(gender)
                .phone(phone)
                .build()

        return Rx2Apollo.from(client.prefetch(updateUserMutation))
    }

    override fun addVehicle(userId: String, vehicle: Vehicle): Completable {
        val addVehicleMutation = AddVehicleMutation.builder()
                .userId(userId)
                .registrationNumber(vehicle.registrationNumber)
                .capacity(vehicle.capacity)
                .color(vehicle.color)
                .make(vehicle.make)
                .build()

        return Rx2Apollo.from(client.prefetch(addVehicleMutation))
    }

    override fun deleteVehicle(registrationNumber: String): Completable {
        val deleteVehicleMutation = DeleteVehicleMutation.builder()
                .registrationNumber(registrationNumber)
                .build()

        return Rx2Apollo.from(client.prefetch(deleteVehicleMutation))
    }

    override fun updateVehicle(registrationNumber: String, make: String, color: String, capacity: Int): Completable {
        val updateVehicleMutation = UpdateVehicleMutation.builder()
                .registrationNumber(registrationNumber)
                .make(make)
                .color(color)
                .capacity(capacity)
                .build()

        return Rx2Apollo.from(client.prefetch(updateVehicleMutation))
    }

    override fun addRequest(userId: String, startLocation: GeoLocation, endLocation: GeoLocation): Completable {
        val addRequestMutation = AddRequestMutation.builder()
                .userId(userId)
                .startLocation(startLocation.mapToInput())
                .endLocation(endLocation.mapToInput())
                .build()

        return Rx2Apollo.from(client.prefetch(addRequestMutation))
    }

    override fun removeRequest(userId: String): Completable {
        val removeRequestMutation = RemoveRequestMutation.builder()
                .userId(userId)
                .build()

        return Rx2Apollo.from(client.prefetch(removeRequestMutation))
    }

    override fun getRequest(userId: String): Single<Requesting> {
        val getRequestByUserQuery = GetRequestByUserQuery.builder()
                .userId(userId)
                .build()

        return Rx2Apollo.from(client.query(getRequestByUserQuery))
                .toSingle()
                .map { response ->
                    val data = response.data()?.requestsByUser
                    if (data == null) {
                        Requesting(false, null)
                    } else {
                        Requesting(true, Pair(data.startLocation().convert(), data.endLocation().convert()))

                    }
                }

    }

    override fun getAllRequests(): Single<MutableList<RideRequest>> {
        val getAllRequestsQuery = GetAllRequestsQuery.builder()
                .build()

        return Rx2Apollo.from(client.query(getAllRequestsQuery))
                .toSingle()
                .map { response ->
                    val mapped = mutableListOf<RideRequest>()

                    val allRequests = response.data()?.allRequests
                    allRequests?.forEach {
                        val request = RideRequest(it.user()._id(), it.user().fullName(), it.startLocation().convert(), it.endLocation().convert())
                        mapped.add(request)
                    }

                    mapped
                }

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

        return Rx2Apollo.from(client.prefetch(startRideMutation))
    }

    override fun addPickup(rideId: String, pickUp: PickUp): Completable {
        val addPickUpMutation = AddPickUpMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .locationName(pickUp.location.name)
                .latitude(pickUp.location.latitude)
                .longitude(pickUp.location.longitude)
                .build()

        return Rx2Apollo.from(client.prefetch(addPickUpMutation))
    }

    override fun removePickUp(rideId: String, pickUp: PickUp): Completable {
        val removePickUpMutation = RemovePickUpMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .build()

        return Rx2Apollo.from(client.prefetch(removePickUpMutation))
    }

    override fun setPickUpCompleted(rideId: String, pickUp: PickUp): Completable {
        val setPickUpCompletedMutation = SetPickUpCompletedMutation.builder()
                .rideId(rideId)
                .userId(pickUp.user.userId)
                .locationName(pickUp.location.name)
                .latitude(pickUp.location.latitude)
                .longitude(pickUp.location.longitude)
                .build()

        return Rx2Apollo.from(client.prefetch(setPickUpCompletedMutation))
    }

    override fun setRideCompleted(ride: LocalRide): Completable {
        val markRideAsCompletedMutation = MarkRideAsCompletedMutation.builder()
                .rideId(ride.rideId)
                .locationName(ride.endLocation.name)
                .latitude(ride.endLocation.latitude)
                .longitude(ride.endLocation.longitude)
                .build()

        return Rx2Apollo.from(client.prefetch(markRideAsCompletedMutation))
    }

    override fun addScheduledRide(scheduledRide: ScheduledRide, userId: String): Completable {
        val scheduleRideMutation = ScheduleRideMutation.builder()
                .rideId(scheduledRide.rideId)
                .userId(userId)
                .startLocation(scheduledRide.startLocation.mapToInput())
                .endLocation(scheduledRide.endLocation.mapToInput())
                .depatureTime(scheduledRide.dateTime.asTimestamp().toDouble())
                .build()

        return Rx2Apollo.from(client.prefetch(scheduleRideMutation))
    }

    override fun removeScheduledRide(scheduledRide: ScheduledRide): Completable {
        val deleteScheduledRideMutation = DeleteScheduledRideMutation.builder()
                .rideId(scheduledRide.rideId)
                .build()

        return Rx2Apollo.from(client.prefetch(deleteScheduledRideMutation))
    }

    override fun getScheduledRidesForUser(userId: String): Single<MutableList<ScheduledRide>> {
        val getScheduledRidesForUserQuery = GetScheduledRidesForUserQuery.builder()
                .userId(userId)
                .build()

        return Rx2Apollo.from(client.query(getScheduledRidesForUserQuery))
                .toSingle()
                .map { response ->
                    val result = mutableListOf<ScheduledRide>()
                    val data = response.data()?.scheduledRidesForUser
                    data?.forEach { item ->
                        val scheduledRideData = item.fragments().scheduledRideData()
                        val scheduledRide = ScheduledRide(
                                scheduledRideData.rideId(),
                                UserSimple(scheduledRideData.user()._id(), scheduledRideData.user().fullName(), scheduledRideData.user().phone()),
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

        return Rx2Apollo.from(client.prefetch(deleteScheduledRideMutation))
    }

    override fun getAllScheduledRides(): Single<MutableList<ScheduledRide>> {
        val getScheduledRidesQuery = GetScheduledRidesQuery.builder().build()

        return Rx2Apollo.from(client.query(getScheduledRidesQuery))
                .toSingle()
                .map { response ->
                    val result = mutableListOf<ScheduledRide>()
                    val data = response.data()?.allScheduledRides
                    data?.forEach { item ->
                        val scheduledRideData = item.fragments().scheduledRideData()
                        val scheduledRide = ScheduledRide(
                                scheduledRideData.rideId(),
                                UserSimple(scheduledRideData.user()._id(), scheduledRideData.user().fullName(), scheduledRideData.user().phone()),
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

        return Rx2Apollo.from(client.query(getUserRidesQuery))
                .toSingle()
                .map { response ->
                    val returnList = mutableListOf<PastRide>()
                    val data = response.data()?.ridesByUser

                    data?.forEach { ride ->
                        val decimalFormat = DecimalFormat("#.##")
                        val formattedDepartureTime = decimalFormat.format(ride.departureTime())
                        val departureTime = DateTime.fromTimestamp(formattedDepartureTime.toLong())
                        val arrivalTimeRaw = ride.arrivalTime()

                        val formattedArrivalTime = decimalFormat.format(arrivalTimeRaw)
                        val arrivalTime = DateTime.fromTimestamp(formattedArrivalTime.toLong())


                        val pastRide = PastRide(
                                ride.startLocation().name(),
                                ride.endLocation().name(),
                                departureTime,
                                arrivalTime,
                                ride.vehicle().registrationNumber(),
                                ride.driver().fullName())

                        returnList.add(pastRide)
                    }

                    returnList
                }
    }

    private fun ScheduledRideData.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun ScheduledRideData.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun GetRequestByUserQuery.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun GetRequestByUserQuery.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun GetAllRequestsQuery.StartLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun GetAllRequestsQuery.EndLocation.convert(): GeoLocation {
        return GeoLocation(this.name(), this.latitude(), this.longitude())
    }

    private fun GeoLocation.mapToInput(): GeoLocationInput = GeoLocationInput.builder()
            .name(this.name)
            .latitude(this.latitude)
            .longitude(this.longitude)
            .build()

    private fun <T> Observable<T>.toSingle(): Single<T> {
        return Single.fromObservable(this)
    }

}