package com.marknkamau.unipool

import android.support.multidex.MultiDexApplication
import com.crashlytics.android.Crashlytics
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.marknkamau.unipool.domain.authentication.AuthenticationService
import com.marknkamau.unipool.domain.authentication.AuthenticationServiceImpl
import com.marknkamau.unipool.domain.data.api.ApiRepository
import com.marknkamau.unipool.domain.data.api.ApiRepositoryImpl
import com.marknkamau.unipool.domain.data.api.ApolloHelper
import com.marknkamau.unipool.domain.data.local.LocalStorageService
import com.marknkamau.unipool.domain.data.local.LocalStorageServiceImpl
import com.marknkamau.unipool.domain.mqtt.MQTTHelper
import com.marknkamau.unipool.utils.mapping.DirectionsHelper
import io.fabric.sdk.android.Fabric
import timber.log.Timber
import java.util.*

class UnipoolApp : MultiDexApplication() {
    private val mqttBroker by lazy { getString(R.string.mqtt_broker) }
    private val requestTokenId by lazy { getString(R.string.google_app_id) }
    private val googleMapsApiKey by lazy { getString(R.string.google_api_key) }

    val authService: AuthenticationService by lazy { AuthenticationServiceImpl(googleApiClient) }
    val localStorage: LocalStorageService by lazy { LocalStorageServiceImpl(this) }
    val mqttHelper by lazy {
        MQTTHelper(this, mqttBroker, "app-${UUID.randomUUID()}")
    }
    val apiService: ApiRepository by lazy {
        ApiRepositoryImpl(ApolloHelper.apolloClient)
    }

    val directionsHelper by lazy { DirectionsHelper(googleMapsApiKey, this) }
    val googleApiClient: GoogleApiClient by lazy {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requestTokenId)
                .requestEmail()

        // If not in debug mode, only show Strathmore accounts
        if (!BuildConfig.DEBUG) {
            signInOptions.setHostedDomain("strathmore.edu")
        }

        GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions.build())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build()
    }

    override fun onCreate() {
        super.onCreate()

        googleApiClient.connect()

        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return "Timber/${element.fileName.substringBefore(".")}.${element.methodName}(Ln${element.lineNumber})"
            }
        })

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }
    }
}
