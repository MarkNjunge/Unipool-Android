package com.marknkamau.unipool.utils.mapping

import android.annotation.SuppressLint
import android.graphics.Color
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.marknkamau.unipool.utils.roundTo
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class DirectionsHelper(private val googleApiKey: String) {

    @SuppressLint("CheckResult")
    fun getDirections(origin: LatLng,
                      destination: LatLng,
                      onResult: (result: DirectionResult) -> Unit,
                      onError: (throwable: Throwable) -> Unit,
                      waypoints: MutableList<LatLng>) {

        val geoContext = GeoApiContext.Builder().apiKey(googleApiKey).build()

        // Convert to correct LatLng type
        val points = arrayOf<com.google.maps.model.LatLng>()
        waypoints.forEach {
            points.plus(it.toOtherType())
        }

        Timber.d("Getting directions from $origin to $destination via $points")

        DirectionsApi.newRequest(geoContext)
                .mode(TravelMode.DRIVING)
                .origin(origin.toOtherType())
                .waypoints(*points)
                .destination(destination.toOtherType())
                .units(com.google.maps.model.Unit.METRIC)
                .toSingle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { result ->
                            val route = result.routes.first()

                            val decoded = PolyUtil.decode(route.overviewPolyline.encodedPath)
                            val polylineOptions = PolylineOptions()
                                    .startCap(RoundCap())
                                    .endCap(RoundCap())
                                    .color(Color.parseColor("#03a57a"))
                                    .addAll(decoded)

                            val start = LatLng(route.legs[0].startLocation.lat, route.legs[0].startLocation.lng)
                            val distance = route.legs.first().distance.inMeters

                            val directionResult = DirectionResult(start, polylineOptions, distance)
                            onResult(directionResult)
                        },
                        onError = { throwable ->
                            Timber.e(throwable)
                            onError(throwable)
                        }
                )
    }

    private fun DirectionsApiRequest.toSingle(): Single<DirectionsResult> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(this.await())
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    data class DirectionResult(val routeStart: LatLng, val routePolylineOpts: PolylineOptions, val distance: Long) {
        val readableDistance: String
            get() = if (distance > 1000) {
                "${(distance.toDouble() / 1000).roundTo(1)} km"
            } else {
                "$distance m"
            }
    }
}
