package com.marknkamau.unipool.ui.riderMap

import com.marknkamau.unipool.domain.LocalRideRequest
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.RideUpdateType
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.domain.mqtt.MQTTHelper
import com.marknkamau.unipool.utils.applyCompletableSchedulers
import com.marknkamau.unipool.utils.mapping.GeoLocation
import io.reactivex.rxkotlin.subscribeBy
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber

class RiderMapPresenter(private val view: RiderMapView,
                        private val authenticationService: AuthenticationService,
                        private val localStorageService: LocalStorageService,
                        private val apiRepository: ApiRepository,
                        private val mqttHelper: MQTTHelper) : MQTTHelper.MessageListener, MQTTHelper.ConnectionListener {

    private val LISTEN_TOPIC = "${authenticationService.currentUserId()}/offer"
    private var RIDE_TOPIC: String? = null
    private val user by lazy { localStorageService.getUser()!! }

    init {
        mqttHelper.setConnectionListener(this)
        mqttHelper.setMessageListener(this)
    }

    fun checkForRequest() {
        val request = localStorageService.getRequest()

        if (request == null) {
            view.noRequestExists()
        } else {
            view.requestExists(request)
            addRequestOnline(request)
        }
    }

    fun makeRequest(origin: GeoLocation, destination: GeoLocation, distance: Long) {
        apiRepository.addRequest(authenticationService.currentUserId(), origin, destination)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            val request = LocalRideRequest(origin, destination, distance)
                            localStorageService.saveRequest(request)
                            view.requestExists(request)
                            listenForOffers()
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "There was an error precessing your request")
                        })
    }

    fun activityResumed() {
        mqttHelper.connect()

        val request = localStorageService.getRequest()

        if (request != null) {
            addRequestOnline(request)
        }
    }

    fun activityPaused() {
        mqttHelper.disconnect()
        removeRequestOnline()
    }

    fun respondToOffer(offer: RequestOffer, accepted: Boolean) {
        offer.accepted = accepted
        offer.rider.phone = user.phone
        mqttHelper.publish("${offer.driver.userId}/offer", offer.toJson(), 2)

        if (accepted) {
            listenForRideUpdate(offer.driver.userId)
            view.setRideStarted(offer.driver)
            removeRequestOnline()
            stopListeningForOffers()
        }
    }

    fun removeRequest() {
        apiRepository.removeRequest(authenticationService.currentUserId())
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            localStorageService.clearRequest()
                            stopListeningForOffers()
                            view.noRequestExists()
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "There was an error precessing your request")
                        })
    }

    private fun listenForRideUpdate(driverId: String) {
        RIDE_TOPIC = "${user.id}/offer/$driverId"
        mqttHelper.subscribe(RIDE_TOPIC!!, 2)
    }

    private fun addRequestOnline(request: LocalRideRequest) {
        apiRepository.addRequest(authenticationService.currentUserId(), request.origin, request.destination)
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onComplete = {
                            listenForOffers()
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "There was an error precessing your request")
                        })
    }

    private fun removeRequestOnline(){
        apiRepository.removeRequest(authenticationService.currentUserId())
                .compose(applyCompletableSchedulers())
                .subscribeBy(
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "There was an error precessing your request")
                        })
    }

    private fun listenForOffers() {
        mqttHelper.subscribe(LISTEN_TOPIC, 2)
    }

    private fun stopListeningForOffers() {
        mqttHelper.unsubscribe(LISTEN_TOPIC)
    }

    override fun onConnected(reconnect: Boolean) {
        Timber.i("Connected to broker. Reconnect: $reconnect")
    }

    override fun onLost(throwable: Throwable) {
        Timber.e(throwable.message)
    }

    override fun onArrived(topic: String?, message: MqttMessage?) {
        Timber.i("Message arrived => $topic : $message")
        if (message != null) {
            if (topic == LISTEN_TOPIC) {
                val requestOffer = RequestOffer.fromJson(message.toString())
                view.displayOffer(requestOffer)
            } else if (topic == RIDE_TOPIC) {
                val updateType = RideUpdateType.values().find { it.name == message.toString() }!!
                if (updateType == RideUpdateType.CANCELLED) {
                    view.setRideCancelled()
                    checkForRequest()
                }else if(updateType == RideUpdateType.COMPLETED){
                    removeRequest()
                    view.setRideCompleted()
                }
            }
        }
    }

    override fun onDelivered(token: IMqttDeliveryToken?) {

    }
}