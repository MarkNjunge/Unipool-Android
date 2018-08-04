package com.marknkamau.unipool.domain.mqtt

import android.content.Context
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*
import timber.log.Timber
import java.nio.charset.Charset

/**
 * Helper class for interacting with mqtt on android. Requires Paho Android Service.
 *
 * @author Mark Njung'e
 * @param context Android context
 * @param url Url of the mqtt broker
 * @param clientId (Optional) Identification for the client.
 * @see <a href="https://www.eclipse.org/paho/clients/android/">Paho Android Service<a/>
 * @see <a href="https://www.hivemq.com/blog/mqtt-client-library-enyclopedia-paho-android-service">MQTT Client Library Enyclopedia – Paho Android Service<a/>
 */
class MQTTHelper(context: Context, val url: String, private val clientId: String = MqttClient.generateClientId()) : MqttCallbackExtended {
    private val client = MqttAndroidClient(context, url, clientId)
    private var connectionListener: ConnectionListener? = null
    private var messageListener: MessageListener? = null
    private var savedSubscriptions = mutableSetOf<Pair<String, Int>>()
    private var pendingUnsubscribes = mutableListOf<String>()

    init {
        client.setCallback(this)
    }

    /**
     * Makes a connection to the broker.
     *
     * @param options (Optional) Configuration options for the connection.
     * AutomaticReconnection is enabled.
     * @see MqttConnectOptions
     */
    fun connect(options: MqttConnectOptions = MqttConnectOptions()) {
        Timber.i("Connecting to $url as $clientId")
        options.isAutomaticReconnect = true
        options.keepAliveInterval = 10000
        client.connect(options)
    }

    /**
     * Disconnects the client from the broker
     *
     * @param quiesceTimeout The amount of time in milliseconds to allow for existing work
     * to finish before disconnecting. A value of zero or less means the client will not quiesce.
     * @see MqttAndroidClient.disconnect(Long)
     */
    fun disconnect(quiesceTimeout: Long = 0) {
        if (client.isConnected) {
            client.disconnect(quiesceTimeout)
        }
    }

    /**
     * Subscribes to a given topic. The topic and qos value are stored to be subscribed to on
     * reconnect or if the subscription was attempted before the client had connected
     *
     * @param topic The fullname of the topic.
     * @param qos The quality of service level.
     * @see <a href="https://www.hivemq.com/blog/mqtt-essentials-part-6-mqtt-quality-of-service-levels">QOS levels (HiveMQ)<a/>
     */
    fun subscribe(topic: String, qos: Int) {
        if (client.isConnected) {
            client.subscribe(topic, qos)
            Timber.i("Subscribed to $topic")
        }
        // The subscription is still saved so that when a connection is made
        // it will be subscribed to
        savedSubscriptions.add(Pair(topic, qos))
    }

    /**
     * Unsubscribed from a given topic. If the client is not connected, the topic will be noted and
     * unsubscribed from when connection is reestablished.
     *
     * @param topic The fullname of the topic.
     */
    fun unsubscribe(topic: String) {
        if (client.isConnected) {
            client.unsubscribe(topic)
            Timber.i("Unsubscribed from $topic")
        } else {
            pendingUnsubscribes.add(topic)
        }
    }

    /**
     * Publishes a message to a topic.
     *
     * @param topic The topic to publish to.
     * @param message The message to be published.
     * @param qos The quality of service level.
     * @param retained
     * @see <a href="https://www.hivemq.com/blog/mqtt-essentials-part-6-mqtt-quality-of-service-levels">QOS levels (HiveMQ)<a/>
     */
    fun publish(topic: String, message: String, qos: Int = 0, retained: Boolean = false) {
        Timber.i("To publish $message")
        val bytes = message.toByteArray(Charset.forName("UTF-8"))
        client.publish(topic, bytes, qos, retained)
    }

    /**
     * Sets the listener to be notified of connection events.
     *
     * @param listener An instance of ConnectionListener.
     * @see ConnectionListener
     */
    fun setConnectionListener(listener: ConnectionListener) {
        connectionListener = listener
    }

    /**
     * Sets the listener to be notified of incoming messages
     *
     * @param listener An instance of MessageListener.
     * @see MessageListener
     */
    fun setMessageListener(listener: MessageListener) {
        Timber.i("Set listener")
        messageListener = listener
    }

    override fun connectComplete(reconnect: Boolean, serverURI: String?) {
        Timber.i("Connected to $serverURI, reconnect: $reconnect")

        savedSubscriptions.forEach {
            client.subscribe(it.first, it.second)
            Timber.i("Resubscribed to ${it.first}")
        }

        pendingUnsubscribes.forEach {
            client.unsubscribe(it)
            Timber.i("Unsubscribed from $it")
            pendingUnsubscribes.remove(it)
        }

        connectionListener?.onConnected(reconnect)
    }

    override fun connectionLost(cause: Throwable?) {
        Timber.e(cause?.message.toString())

        connectionListener?.onLost(cause ?: Throwable("Connection lost due to an unknown error"))
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        Timber.d("Topic: $topic, Message: ${message?.toString()}")
        messageListener?.onArrived(topic, message)
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        Timber.i("Delivery complete: $token")
        messageListener?.onDelivered(token)
    }

    /**
     * Listener for connection events.
     *
     * @author Mark Njung'e
     */
    interface ConnectionListener {
        /**
         * Called when the connection has been made successfully.
         *
         * @param reconnect Whether it was a first time connection or a reconnect.
         */
        fun onConnected(reconnect: Boolean)

        /**
         * Called when the connection has been lost.
         *
         * @param throwable The cause of the connection loss.
         */
        fun onLost(throwable: Throwable)
    }

    /**
     * Listener for message related events
     */
    interface MessageListener {
        /**
         * Called when a message has been received.
         *
         * @param topic The topic the message was from.
         * @param message The message received.
         * @see MqttMessage
         */
        fun onArrived(topic: String?, message: MqttMessage?)

        /**
         * Called when the message has been delivered.
         *
         * @param token A token for the delivery.
         * @see IMqttDeliveryToken
         */
        fun onDelivered(token: IMqttDeliveryToken?)
    }
}

