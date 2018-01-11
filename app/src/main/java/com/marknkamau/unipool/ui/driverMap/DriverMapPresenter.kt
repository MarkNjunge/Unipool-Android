package com.marknkamau.unipool.ui.driverMap

import com.google.android.gms.maps.model.LatLng
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.RideRequest
import com.marknkamau.unipool.domain.UserSimple
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.domain.mqtt.MQTTHelper
import com.marknkamau.unipool.ui.BasePresenter
import com.marknkamau.unipool.utils.applySingleSchedulers
import com.marknkamau.unipool.utils.mapping.DirectionsHelper
import io.reactivex.rxkotlin.subscribeBy
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttMessage
import timber.log.Timber
import java.util.*

class DriverMapPresenter(private val view: DriverMapView,
                         private val apiRepository: ApiRepository,
                         private val localStorageService: LocalStorageService,
                         private val directionsHelper: DirectionsHelper,
                         private val mqttHelper: MQTTHelper)
    : BasePresenter(), MQTTHelper.ConnectionListener, MQTTHelper.MessageListener {

    private val RESPONSE_TOPIC: String
        get() = "${user.id}/offer"
    private val user by lazy { localStorageService.getUser()!! }

    init {
        mqttHelper.setConnectionListener(this)
        mqttHelper.setMessageListener(this)
    }

    fun getRequests() {
        disposables.add(apiRepository.getAllRequests()
                .compose(applySingleSchedulers())
                .subscribeBy(
                        onSuccess = { requests ->
                            if (requests.size == 0) {
                                view.noRequests()
                            } else {
                                view.requestReceived(requests)
                            }
                        },
                        onError = { throwable ->
                            view.displayMessage(throwable.message ?: "An error has occurred")
                        }))
    }

    fun getDirections(origin: LatLng, destination: LatLng,
                      onResult: (result: DirectionsHelper.DirectionResult) -> Unit,
                      onError: (throwable: Throwable) -> Unit) {

        directionsHelper.getDirections(origin, destination, onResult, onError, mutableListOf())
    }

    fun offerToUser(rideRequest: RideRequest, pricing: Int) {
        val offerID = UUID.randomUUID().toString()
        val requestOffer = RequestOffer(offerID, UserSimple(user.id, user.fullName, user.phone), UserSimple(rideRequest.userId, rideRequest.userName), rideRequest.origin, pricing)
        mqttHelper.publish("${rideRequest.userId}/offer", requestOffer.toJson(), 2)
        listenForResponse()
    }

    fun notifyRider(riderId: String, updateType: String){
        mqttHelper.publish("$riderId/offer/${user.id}", updateType, 2)
    }

    fun listenForResponse() {
        mqttHelper.subscribe(RESPONSE_TOPIC, 2)
    }

    fun connect() {
        mqttHelper.connect()
    }

    fun disconnect() {
        mqttHelper.disconnect()
    }

    override fun onConnected(reconnect: Boolean) {
        Timber.i("Connected to broker. Reconnect: $reconnect")
    }

    override fun onLost(throwable: Throwable) {
        Timber.e(throwable.message)
    }

    override fun onArrived(topic: String?, message: MqttMessage?) {
        Timber.i("Message arrived: $message")
        if (topic == RESPONSE_TOPIC && message != null) {
            val requestOffer = RequestOffer.fromJson(message.toString())
            if (requestOffer.accepted) {
                view.requestAccepted(requestOffer)
            } else {
                view.displayMessage("Offer rejected")
            }
        }
    }

    override fun onDelivered(token: IMqttDeliveryToken?) {
        Timber.i("Message delivered ${token?.message}")
    }
}