package com.marknkamau.unipool.ui.selectLocation

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomSheetBehavior
import android.view.View
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.marknkamau.unipool.R
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.utils.app
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.mapping.MapHelper
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_select_location.*
import kotlinx.android.synthetic.main.layout_location_bottom_sheet.*

class SelectLocationActivity : BaseActivity(), OnMapReadyCallback {
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var selectedLocation: GeoLocation
    private var mapHelper: MapHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_location)

        btnConfirm.setOnClickListener {
            val intent = Intent()
            intent.putExtra(SELECTED_LOCATION, selectedLocation)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

        bottomSheetBehavior = BottomSheetBehavior.from(locationBottomSheet)
        bottomSheetBehavior.peekHeight = 0

        fabSU.setOnClickListener {
            mapHelper?.let {
                val intent = Intent()
                intent.putExtra(SELECTED_LOCATION, it.strathmoreLocation)
                setResult(Activity.RESULT_OK, intent)
                finish()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

        mapHelper = MapHelper(app.googleApiClient, googleMap, this, 15f)
        mapHelper?.let { helper ->
            helper.initializePlacesSearch(fragmentManager.findFragmentById(R.id.placeAutocompleteFragment) as PlaceAutocompleteFragment)

            helper.setOnCameraMove {
                if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                }
            }

            helper.setOnCameraStop {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                selectedLocation = helper.selectedLocation
                tvLocationName.text = selectedLocation.name
            }

            helper.setOnError { throwable ->
                toast(throwable.message ?: "An error has occurred")
            }
        }
    }

    companion object {
        val SELECTED_LOCATION = "selected location"
    }
}
