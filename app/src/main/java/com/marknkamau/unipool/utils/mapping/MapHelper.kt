package com.marknkamau.unipool.utils.mapping

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.Polyline
import com.marknkamau.unipool.domain.PickUp
import com.marknkamau.unipool.utils.runAfter
import timber.log.Timber

class MapHelper(private val googleApiClient: GoogleApiClient,
                private val googleMap: GoogleMap,
                private val activity: Activity,
                initialZoom: Float = 16.0f)
    : GoogleMap.OnCameraIdleListener, com.google.android.gms.location.LocationListener {
    private var originMarker: Marker? = null
    private var routePolyline: Polyline? = null
    private var markers = mutableListOf<Marker>()
    private val locationProviderApi by lazy { LocationServices.FusedLocationApi }

    var selectedLocation: GeoLocation = getLastLocation()
    var currentLocation = getLastLocation()
    var directionsHelper: DirectionsHelper? = null
    var onErr: ((throwable: Throwable) -> Unit)? = null
    var onStop: ((stoppedAt: GeoLocation) -> Unit)? = null
    val strathmoreLocation = GeoLocation("Strathmore University", -1.310087, 36.812528)

    init {
        googleMap.setOnCameraIdleListener(this)
        val userLocation = getLastLocation()
        moveCamera(LatLng(userLocation.latitude, userLocation.longitude), false, initialZoom)
    }

    @SuppressLint("MissingPermission")
    fun listenForLocationUpdates() {
        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 10000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        locationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this)
    }

    fun stopListeningForLocationUpdates() {
        locationProviderApi.removeLocationUpdates(googleApiClient, this)
    }

    fun initializePlacesSearch(placeFragment: PlaceAutocompleteFragment) {
        val filter = AutocompleteFilter.Builder().setCountry("KE").build()
        placeFragment.setFilter(filter)

        placeFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                moveCamera(place.latLng, true)
            }

            override fun onError(status: Status) {
                Timber.e(status.statusMessage)
                onErr?.invoke(Throwable(status.statusMessage))
            }
        })
    }

    fun setOnCameraMove(func: () -> Unit) {
        googleMap.setOnCameraMoveListener(func)
    }

    fun setOnCameraStop(func: (GeoLocation) -> Unit) {
        onStop = func
    }

    fun setOnError(func: (Throwable) -> Unit) {
        onErr = func
    }

    fun addMarker(latLng: LatLng, title: String): Marker {
        Timber.d("Added marker at $title")
        val markerOptions = MarkerOptions()
                .position(latLng)
                .title(title)
                .draggable(false)

        val marker = this.googleMap.addMarker(markerOptions)
        markers.add(marker)
        return marker
    }

    fun displayRoute(polylineOptions: PolylineOptions, points: MutableList<PickUp>) {
        Timber.i("Updated route")
        // Remove old marker and route line
        originMarker?.remove()
        routePolyline?.remove()

        points.forEach { point ->
            val markerTitle = point.user.fullname
            // Add new marker and route line
            originMarker = addMarker(point.location.latLng, markerTitle)
        }
        routePolyline = googleMap.addPolyline(polylineOptions)
    }

    fun displayRoute(directionResult: DirectionsHelper.DirectionResult) {
        // Remove old marker and route line
        originMarker?.remove()
        routePolyline?.remove()

        val markerTitle = "${getGeoLocation(directionResult.routeStart).name}: ${directionResult.readableDistance}"
        // Add new marker and route line
        originMarker = addMarker(directionResult.routeStart, markerTitle)
        routePolyline = googleMap.addPolyline(directionResult.routePolylineOpts)
    }

    fun displayRoute(origin: GeoLocation, destination: GeoLocation) {
        val originLatLng = origin.extractLatLng()
        val destinationLatLng = destination.extractLatLng()

        directionsHelper?.getDirections(originLatLng, destinationLatLng,
                { result: DirectionsHelper.DirectionResult ->
                    // Remove old marker and route line
                    originMarker?.remove()
                    routePolyline?.remove()

                    // Add new marker and route line
                    originMarker = addMarker(result.routeStart, "${getGeoLocation(result.routeStart).name}: $result.distance m")
                    routePolyline = googleMap.addPolyline(result.routePolylineOpts)
                },
                { throwable: Throwable ->
                    Timber.e(throwable)
                    onErr?.invoke(throwable)
                }, mutableListOf())
    }

    fun clearRoute() {
        originMarker?.remove()
        routePolyline?.remove()
        markers.forEach {
            it.remove()
        }
        markers.clear()

        addMarker(strathmoreLocation.extractLatLng(), "Strathmore University")
    }

    fun moveCamera(latLng: LatLng, animate: Boolean = true, zoom: Float = googleMap.cameraPosition.zoom) {
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))
        }
    }

    fun moveToDisplay(vararg points: LatLng) {
        if (points.isNotEmpty()) {
            val builder = LatLngBounds.builder()
            points.forEach {
                builder.include(it)
            }
            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(builder.build(), 200)// param2 = padding
            runAfter(1000) {
                googleMap.animateCamera(cameraUpdate)
            }
        }
    }

    @SuppressLint("MissingPermission") // Already checked
    fun getLastLocation(): GeoLocation {
        @Suppress("DEPRECATION") // See https://developer.android.com/training/location/retrieve-current.html
        val locationProviderApi = LocationServices.FusedLocationApi

        val lastLocation: Location? = locationProviderApi.getLastLocation(googleApiClient)
        lastLocation?.let {
            val lastLocationLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
            return getGeoLocation(lastLocationLatLng)
        }
        return GeoLocation("Strathmore University", -1.310087, 36.812528)
    }

    override fun onCameraIdle() {
        val stoppedAt = getGeoLocation(googleMap.cameraPosition.target)
        selectedLocation = stoppedAt
        onStop?.invoke(stoppedAt)
    }

    override fun onLocationChanged(location: Location?) {
        location?.let {
            currentLocation = getGeoLocation(LatLng(location.latitude, location.longitude))
        }
    }

    private fun getGeoLocation(latLng: LatLng): GeoLocation {
        return try {
            val addresses = Geocoder(activity).getFromLocation(latLng.latitude, latLng.longitude, 1)
            val address = addresses[0]

            GeoLocation(address.getAddressLine(0).split(",")[0], latLng.latitude, latLng.longitude)
        } catch (e: Exception) {
            Timber.e(e)
            onErr?.invoke(Throwable("Unable to get location. Please check your internet connection"))
            GeoLocation("Strathmore University", -1.310087, 36.812528)
        }
    }
}