package com.marknkamau.unipool.domain.data.local

import android.content.Context
import com.marknkamau.unipool.domain.*
import io.paperdb.Paper
import timber.log.Timber

class LocalStorageServiceImpl(context: Context) : LocalStorageService {
    private val book by lazy { Paper.book() }
    private val USER_KEY = "user_key"
    private val REQUESING = "requesting"
    private val REQUEST = "request"
    private val SCHEDULED_RIDE = "scheduled_ride"
    private val ONGOING_RIDE = "ongoing_ride"

    init {
        Paper.init(context)
    }

    override fun saveUser(user: User) {
        deleteUser()
        Timber.i("User saved: $user")
        book.write(USER_KEY, user)
    }

    override fun getUser(): User? {
        val user = book.read<User>(USER_KEY)
        Timber.i("User read: $user")
        return user
    }

    override fun deleteUser() {
        Timber.i("User deleted")
        book.delete(USER_KEY)
    }

    override fun updateRequestingStatus(requesting: Requesting) {
        book.delete(REQUESING)
        book.write(REQUESING, requesting)
    }

    override fun getRequesting(): Requesting {
        return book.read<Requesting>(REQUESING)
    }

    override fun saveRequest(request: LocalRideRequest) {
        book.write(REQUEST, request)
    }

    override fun getRequest(): LocalRideRequest? {
        return book.read<LocalRideRequest>(REQUEST)
    }

    override fun clearRequest() {
        book.delete(REQUEST)
    }

    override fun getScheduledRides(): MutableList<ScheduledRide> {
        val list = book.read<MutableList<ScheduledRide>>(SCHEDULED_RIDE)
        return if (list == null) {
            mutableListOf()
        } else {
            return list
        }
    }

    override fun setScheduledRides(rides: MutableList<ScheduledRide>) {
        book.delete(SCHEDULED_RIDE)
        book.write(SCHEDULED_RIDE, rides)
    }

    override fun saveScheduledRide(ride: ScheduledRide) {
        val rides = getScheduledRides()
        rides.add(ride)
        book.delete(SCHEDULED_RIDE)
        book.write(SCHEDULED_RIDE, rides)
    }

    override fun deleteScheduledRide(ride: ScheduledRide) {
        val rides = getScheduledRides()
        rides.remove(ride)
        book.delete(SCHEDULED_RIDE)
        book.write(SCHEDULED_RIDE, rides)
    }

    override fun getOngoingRide(): LocalRide? {
        return book.read<LocalRide>(ONGOING_RIDE)
    }

    override fun deleteOngoingRide() {
        book.delete(ONGOING_RIDE)
    }

    override fun updateOngoingRide(ride: LocalRide) {
        deleteOngoingRide()
        book.write(ONGOING_RIDE, ride)
    }

}
