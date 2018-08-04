package com.marknkamau.unipool.ui.riderMap

import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.LocalRideRequest
import com.marknkamau.unipool.domain.OfferResponseType
import com.marknkamau.unipool.domain.RequestOffer
import com.marknkamau.unipool.domain.UserSimple
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.utils.mapping.DirectionsHelper
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.mapping.MapHelper
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_rider_map.*
import kotlinx.android.synthetic.main.layout_rider_bottom_sheet.*

class RiderMapActivity : BaseActivity(), OnMapReadyCallback, RiderMapView {
    private var viewVisible = false
    private var isRequesting = false
    private lateinit var mapHelper: MapHelper
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var presenter: RiderMapPresenter
    private var offersAdapter: OffersAdapter? = null
    private var lastDirections: DirectionsHelper.DirectionResult? = null

    private val strathmoreLocation = GeoLocation("Strathmore University", -1.310087, 36.812528)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rider_map)

        // Initialize bottom sheet
        bottomSheetBehavior = BottomSheetBehavior.from(riderBottomSheet)
        bottomSheetBehavior.peekHeight = 170
        bottomSheetBehavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                imgOpenBottom.rotation = (180 * slideOffset)
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    viewVisible = false
                } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    viewVisible = true
                    if (!isRequesting) {
                        updateRouteDisplay()
                    }
                }
            }
        })

        // Initialize offers recyclerview
        rvOffers.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvOffers.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))

        // Set on click listeners
        imgOpenBottom.setOnClickListener {
            if (!isRequesting) {
                updateRouteDisplay()
            }

            if (viewVisible) {
                hideBottomSheet()
            } else {
                displayBottomSheet()
            }
        }

        btnConfirm.setOnClickListener {
            if (isRequesting) {
                mapHelper.clearRoute()
                presenter.removeRequest()
            } else {
                makeRequest()
            }
        }

        // Initialize presenter
        presenter = RiderMapPresenter(this, UnipoolApp.authService, UnipoolApp.localStorage, UnipoolApp.apiRepository, UnipoolApp.mqttHelper)

        // Initialize map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()
        presenter.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        presenter.activityPaused()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        // Set map style
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        // Initialize map helper
        mapHelper = MapHelper(UnipoolApp.googleApiClient, googleMap, this)
        mapHelper.initializePlacesSearch(fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment)
        mapHelper.directionsHelper = UnipoolApp.directionsHelper

        // Set universal error listener
        mapHelper.setOnError { throwable ->
            displayMessage(throwable.message ?: "An error has occurred")
        }

        // Set camera stop listener
        mapHelper.setOnCameraStop {
            if (viewVisible && !isRequesting) {
                updateRouteDisplay()
            }
        }

        // Set on camera stop
        mapHelper.setOnCameraMove {
            if (viewVisible && !isRequesting) {
                hideBottomSheet()
            }
        }

        // Add Strathmore maker
        mapHelper.addMarker(strathmoreLocation.extractLatLng(), "Strathmore University")

        // Look for an existing request
        presenter.checkForRequest()
    }

    override fun requestExists(request: LocalRideRequest) {
        // Set request display
        tvOrigin.text = request.origin.name
        tvDestination.text = request.destination.name
        btnConfirm.text = "Cancel request"

        // Hide views
        pbLoading.visibility = View.GONE
        cbSearch.visibility = View.GONE
        imgCenter.visibility = View.GONE

        // Modify map display
        mapHelper.displayRoute(request.origin, request.destination)
        mapHelper.moveToDisplay(request.origin.latLng, request.destination.latLng)

        // Initialize offers adapter
        offersAdapter = OffersAdapter(request.distance, mutableListOf()) { responseType, offer ->
            if (responseType == OfferResponseType.ACCEPTED) {
                acceptOffer(offer)
            } else {
                rejectOffer(offer)
            }
        }
        rvOffers.adapter = offersAdapter

        isRequesting = true
        displayBottomSheet()
    }

    override fun noRequestExists() {
        // Set route display
        tvOrigin.text = ""
        tvDestination.text = ""
        btnConfirm.text = "Confirm"

        // Show views
        cbSearch.visibility = View.VISIBLE
        imgCenter.visibility = View.VISIBLE

        // Hide views
        pbLoading.visibility = View.GONE

        isRequesting = false
        hideBottomSheet()
    }

    private fun acceptOffer(offer: RequestOffer) {
        presenter.respondToOffer(offer, true)
        offersAdapter?.removeItem(offer)
        offersAdapter?.notifyDataSetChanged()
    }

    private fun rejectOffer(offer: RequestOffer) {
        presenter.respondToOffer(offer, false)
        offersAdapter?.removeItem(offer)
        offersAdapter?.notifyDataSetChanged()
    }

    private fun makeRequest() {
        lastDirections?.let {
            presenter.makeRequest(mapHelper.selectedLocation, strathmoreLocation, it.distance)
        }
    }

    override fun displayMessage(message: String) {
        pbLoading.visibility = View.GONE
        toast(message)
    }

    override fun displayOffer(offer: RequestOffer) {
        offersAdapter?.addItem(offer)
        offersAdapter?.notifyDataSetChanged()
    }

    override fun setRideStarted(driver: UserSimple) {
        tvDriverComing.text = "${driver.fullname} is on their way."
        tvDriverComing.visibility = View.VISIBLE
        btnConfirm.visibility = View.GONE
    }

    override fun setRideCancelled() {
        tvDriverComing.visibility = View.GONE
        btnConfirm.visibility = View.VISIBLE
    }

    override fun setRideCompleted() {
        mapHelper.clearRoute()
        tvDriverComing.visibility = View.GONE
    }

    private fun updateRouteDisplay() {
        tvOrigin.text = mapHelper.selectedLocation.name
        tvDestination.text = strathmoreLocation.name

        UnipoolApp.directionsHelper.getDirections(mapHelper.selectedLocation.latLng, strathmoreLocation.latLng,
                { result ->
                    lastDirections = result
                    mapHelper.displayRoute(result)
                },
                { throwable ->
                    displayMessage(throwable.message ?: "An error has occurred getting directions")
                }, mutableListOf())
    }

    private fun displayBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        viewVisible = true
    }

    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        viewVisible = false
    }
}
