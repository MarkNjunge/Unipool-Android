package com.marknkamau.unipool.utils.mapping

import android.content.Context
import android.graphics.Color
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.RoundCap
import com.google.gson.Gson
import com.google.maps.android.PolyUtil
import com.marknkamau.unipool.utils.roundTo
import timber.log.Timber

class DirectionsHelper(private val googleApiKey: String, context: Context) {
    private val requestQueue by lazy { Volley.newRequestQueue(context) }

    fun getDirections(origin: LatLng,
                      destination: LatLng,
                      onResult: (result: DirectionResult) -> Unit,
                      onError: (throwable: Throwable) -> Unit,
                      waypoints: MutableList<LatLng>) {

        val url = generateUrl(origin, destination, waypoints)

        val request = StringRequest(Request.Method.GET, url,
                { response ->
                    try {
                        val result: RawDirectionResult = Gson().fromJson<RawDirectionResult>(response, RawDirectionResult::class.java)
                        val polylineOptions = extractPolylineOptions(result.routes.first().overviewPolyline.points)

                        val route = result.routes.first()
                        val start = LatLng(route.legs[0].startLocation.lat.toDouble(), route.legs[0].startLocation.lng.toDouble())
                        val distance = route.legs.first().distance.value

                        val directionResult = DirectionResult(start, polylineOptions, distance.toLong())
                        onResult(directionResult)
                    } catch (e: Exception) {
                        Timber.e(e)
                        onError(e)
                    }
                },
                { error: VolleyError ->
                    Timber.e(error)
                    onError(error)
                })

        requestQueue.add(request)
    }

    private fun extractPolylineOptions(line: String?): PolylineOptions {
        val decode = PolyUtil.decode(line)

        return PolylineOptions()
                .clickable(true)
                .startCap(RoundCap())
                .endCap(RoundCap())
                .color(Color.parseColor("#03a57a"))
                .addAll(decode)
    }

    private fun generateUrl(origin: LatLng, destination: LatLng, waypoints: MutableList<LatLng>): String {
//        val url = "https://maps.googleapis.com/maps/api/directions/json?origin=-1.3033841556485521,36.80235721170902&destination=-1.310087,36.812528&key=AIzaSyD-KaHpnWAfhcub-aTkbFytonoQglUajbs"
        val stringBuilder = StringBuilder()
        stringBuilder.append("https://maps.googleapis.com/maps/api/directions/json")
        stringBuilder.append("?origin=${origin.latitude},${origin.longitude}")
        stringBuilder.append("&destination=${destination.latitude},${destination.longitude}")
        stringBuilder.append("&key=$googleApiKey")
        if (waypoints.size > 0) {
            stringBuilder.append("&waypoints=")
            for (i in waypoints.indices) {
                stringBuilder.append("${waypoints[i].latitude},${waypoints[i].longitude}")
                if (i + 1 != waypoints.size) {
                    stringBuilder.append("|")
                }
            }
        }
        Timber.i(stringBuilder.toString())

        return stringBuilder.toString()
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
