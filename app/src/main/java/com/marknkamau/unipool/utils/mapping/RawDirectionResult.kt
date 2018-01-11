package com.marknkamau.unipool.utils.mapping

import android.support.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class RawDirectionResult(
        @SerializedName("geocoded_waypoints") val geocodedWaypoints: MutableList<GeocodedWaypoints>,
        val status: String,
        val routes: MutableList<Routes>)

@Keep
data class Routes(
        val summary: String,
        val bounds: Bounds,
        val copyrights: String,
        @SerializedName("waypoint_order") val waypointOrder: MutableList<String>,
        val legs: MutableList<Legs>,
        val warnings: MutableList<String>,
        @SerializedName("overview_polyline")
        val overviewPolyline: OverviewPolyline)

@Keep
data class OverviewPolyline(val points: String)

@Keep
data class Legs(
        val duration: Duration,
        val distance: Distance,
        @SerializedName("end_location") val endLocation: EndLocation,
        @SerializedName("start_address") val startAddress: String,
        @SerializedName("end_address") val endAddress: String,
        @SerializedName("start_location") val startLocation: StartLocation,
        @SerializedName("traffic_speed_entry") val trafficSpeedEntry: MutableList<String>,
        @SerializedName("via_waypoint") val viaWaypoint: MutableList<String>,
        val steps: MutableList<Steps>)

@Keep
data class Polyline(val points: String)

@Keep
data class EndLocation(val lng: String, val lat: String)

@Keep
data class Distance(val text: String, val value: String)

@Keep
data class Duration(val text: String,
               val value: String)

@Keep
data class Steps(val html_instructions: String,
            val duration: Duration,
            val distance: Distance,
            val end_location: EndLocation,
            val polyline: Polyline,
            val start_location: StartLocation,
            val travel_mode: String)

@Keep
data class StartLocation(val lng: String,
                    val lat: String)

@Keep
data class Bounds(val southwest: Southwest,
             val northeast: Northeast)

@Keep
data class Southwest(val lng: String,
                val lat: String)

@Keep
data class Northeast(val lng: String,
                val lat: String)

@Keep
data class GeocodedWaypoints(val place_id: String,
                        val geocoder_status: String,
                        val types: Array<String>)
