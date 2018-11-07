package com.marknkamau.unipool

import android.app.Application
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

class UnipoolApp : Application() {
    companion object {
        lateinit var authService: AuthenticationService
        lateinit var localStorage: LocalStorageService
        lateinit var mqttHelper: MQTTHelper
        lateinit var apiRepository: ApiRepository
        lateinit var directionsHelper: DirectionsHelper
        lateinit var googleApiClient: GoogleApiClient
    }

    override fun onCreate() {
        super.onCreate()

        Timber.plant(object : Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String {
                return "Timber/${element.fileName.substringBefore(".")}.${element.methodName}(Ln${element.lineNumber})"
            }
        })

        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
        }

        initializeGoogleApiClient()

        authService = AuthenticationServiceImpl(googleApiClient)
        localStorage = LocalStorageServiceImpl(this)
        mqttHelper = MQTTHelper(this, getString(R.string.mqtt_broker), "app-${UUID.randomUUID().toString().split("-")[0]}")
        apiRepository = ApiRepositoryImpl( ApolloHelper().apolloClient)
        directionsHelper = DirectionsHelper(getString(R.string.google_api_key))
    }

    private fun initializeGoogleApiClient(){
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.request_token_id))
                .requestEmail()

        // If not in debug mode, only show Strathmore accounts
        if (!BuildConfig.DEBUG) {
            signInOptions.setHostedDomain("strathmore.edu")
        }

        googleApiClient = GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, signInOptions.build())
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build()

        googleApiClient.connect()
    }
}
