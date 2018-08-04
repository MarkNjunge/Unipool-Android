package com.marknkamau.unipool.ui.driverMap

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.PickUp
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.RideRequest
import com.marknkamau.unipool.domain.RideUpdateType
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.mapping.MapHelper
import com.marknkamau.unipool.utils.mapping.MarkerData
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_driver_map.*
import kotlinx.android.synthetic.main.layout_driver_bottom_sheet.*
import nz.co.trademe.mapme.annotations.OnMapAnnotationClickListener

class DriverMapActivity : BaseActivity(), DriverMapView, OnMapReadyCallback {
    private lateinit var adapter: MapAdapter
    private lateinit var pickUpsAdapter: PickUpsAdapter
    private lateinit var presenter: DriverMapPresenter
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var mapHelper: MapHelper
    private val markerData = mutableListOf<MarkerData>()
    private val requests = mutableListOf<RideRequest>()
    private lateinit var vehicleNumber: String
    private lateinit var endLocation: GeoLocation
    private lateinit var rideHelper: RideHelper
    private var pricing: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_map)

        vehicleNumber = intent.getStringExtra(VEHICLE)
        endLocation = intent.getParcelableExtra(END_LOCATION)
        pricing = intent.getIntExtra(PRICING, 0)

        presenter = DriverMapPresenter(this, UnipoolApp.apiRepository, UnipoolApp.localStorage, UnipoolApp.directionsHelper, UnipoolApp.mqttHelper)

        bottomSheetBehavior = BottomSheetBehavior.from(userDetailsBottomSheet)
        bottomSheetBehavior.peekHeight = 0

        // Initialize recyclerview for pickUps
        rvPickUps.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvPickUps.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        pickUpsAdapter = PickUpsAdapter { pickUp, clickType ->
            if (clickType == PickUpsAdapter.ClickType.PICKED) {
                handleRiderPicked(pickUp)
            } else if (clickType == PickUpsAdapter.ClickType.CANCELLED) {
                handlePickUpCancelled(pickUp)
            }
        }
        rvPickUps.adapter = pickUpsAdapter

        btnStartRide.setOnClickListener {
            if (rideHelper.ride?.started == false) {
                rideHelper.startRide()
            } else {
                rideHelper.completeRide()
            }
        }

        btnCancelRide.setOnClickListener {
            rideHelper.cancelRide()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.connect()
    }

    override fun onPause() {
        super.onPause()
        presenter.disconnect()
        mapHelper.stopListeningForLocationUpdates()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        mapHelper = MapHelper(UnipoolApp.googleApiClient, googleMap, this, 15f)
        mapHelper.listenForLocationUpdates()

        // Initialize ride helper
        rideHelper = RideHelper(mapHelper, UnipoolApp.directionsHelper, UnipoolApp.localStorage, UnipoolApp.apiRepository, vehicleNumber, endLocation,
                { throwable ->
                    displayMessage(throwable.message ?: "An error has occurred")
                },
                { localRide, updateType ->
                    localRide.riders.forEach {
                        presenter.notifyRider(it.userId, updateType.name)
                    }

                    when (updateType) {
                        RideUpdateType.STARTED -> {
                            btnStartRide.text = "Complete ride"
                        }
                        RideUpdateType.COMPLETED -> {
                            toast("Ride completed")
                            finish()
                        }
                        RideUpdateType.CANCELLED -> {
                            rideView.visibility = View.GONE
                            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            mapHelper.clearRoute()
                        }
                        else -> {
                            mapHelper.displayRoute(localRide.routeLine, localRide.pickUps)
                            mapHelper.addMarker(localRide.startLocation.latLng, localRide.startLocation.name)
                            mapHelper.addMarker(localRide.endLocation.latLng, localRide.endLocation.name)
                            var array = arrayOf<LatLng>()
                            localRide.pickUps.forEach {
                                array = array.plus(it.location.latLng)
                            }
                            mapHelper.moveToDisplay(localRide.startLocation.latLng, localRide.endLocation.latLng, *array)
                            pickUpsAdapter.updateList(localRide.pickUps)
                        }
                    }
                })

        val ongoingRide = UnipoolApp.localStorage.getOngoingRide()
        if (ongoingRide != null) {
            rideHelper.ride = ongoingRide
            rideView.visibility = View.VISIBLE
            if (ongoingRide.started) {
                btnStartRide.text = "Complete ride"
            }
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        } else {
            // Initialize adapter for requests
            adapter = MapAdapter(this, markerData)
            adapter.attach(findViewById(R.id.map), googleMap)
            adapter.notifyDataSetChanged()
            adapter.setOnAnnotationClickListener(OnMapAnnotationClickListener { annotation ->
                val item = markerData[annotation.position]

                val lastLocationLatLng = mapHelper.getLastLocation().latLng
                val markerLatLng = LatLng(item.latitude, item.longitude)

                handleMarkerClick(lastLocationLatLng, requests[annotation.position])

                mapHelper.moveCamera(markerLatLng, true)
                true
            })

            // Get existing requests
            presenter.getRequests()
        }
    }

    override fun displayMessage(message: String) {
        toast(message)
    }

    override fun requestReceived(rideRequests: MutableList<RideRequest>) {
        this.requests.clear()
        this.requests.addAll(rideRequests)

        val markerData = rideRequests.map { request ->
            MarkerData(request.userName, request.origin.latitude, request.origin.longitude)
        }

        this.markerData.addAll(markerData)

        adapter.notifyDataSetChanged()
    }

    override fun noRequests() {
        toast("There are no requests")
    }

    override fun requestAccepted(offer: RequestOffer) {
        rideHelper.addStop(offer)
        rideView.visibility = View.VISIBLE
        offerView.visibility = View.GONE
    }

    private fun handleMarkerClick(lastLocationLatLng: LatLng, request: RideRequest) {
        presenter.getDirections(lastLocationLatLng, request.origin.latLng,
                { result ->
                    offerView.visibility = View.VISIBLE
                    tvName.text = request.userName
                    tvDistance.text = result.readableDistance

                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

                    btnOffer.setOnClickListener { presenter.offerToUser(request, pricing) }
                },
                { throwable ->
                    toast(throwable.message ?: "An error has occurred")
                })
    }

    private fun handlePickUpCancelled(pickUp: PickUp) {
        rideHelper.removeStop(pickUp)
    }

    private fun handleRiderPicked(pickUp: PickUp) {
        rideHelper.pickUpUser(pickUp)
    }

    companion object {
        const val VEHICLE = "vehicle"
        const val END_LOCATION = "endLocation"
        const val PRICING = "pricing"
    }
}
