package com.marknkamau.unipool.ui.preFindRequests

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.User
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.driverMap.DriverMapActivity
import com.marknkamau.unipool.ui.selectLocation.SelectLocationActivity
import com.marknkamau.unipool.utils.getTrimmedText
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_pre_find_requests.*
import timber.log.Timber

class PreFindRequestsActivity : BaseActivity(), PreFindRequestsView {
    private val LOCATION_RQ = 0
    private lateinit var presenter: PreFindRequestsPresenter
    private var endLocation: GeoLocation? = null
    private var vehicle: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_find_requests)

        presenter = PreFindRequestsPresenter(this, paperService)
        presenter.getUser()

        btnFindRequests.setOnClickListener {
            proceedToRequests()
        }

        tvEndLocation.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            startActivityForResult(intent, LOCATION_RQ)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                LOCATION_RQ -> {
                    endLocation = data?.getParcelableExtra(SelectLocationActivity.SELECTED_LOCATION)
                    tvEndLocation.text = endLocation?.name
                }
            }
        }
    }

    override fun displayMessage(message: String) {
        toast(message)
    }

    override fun onUserRetrieved(user: User) {
        val list = ArrayList<String>()
        user.vehicles.forEach {
            list.add(it.registrationNumber)
        }

        val dataAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spVehicle.adapter = dataAdapter

        spVehicle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                vehicle = list[position]
            }

        }
    }

    private fun proceedToRequests() {
        val cost = etCost.getTrimmedText()
        if (cost.isEmpty()) {
            displayMessage("Cost per km is required")
            return
        }

        if (endLocation == null) {
            displayMessage("End location is required")
            return
        }

        if (vehicle == null) {
            displayMessage("Please select a vehicle")
            return
        }

        val intent = Intent(this, DriverMapActivity::class.java)
        intent.putExtra(DriverMapActivity.VEHICLE, vehicle)
        intent.putExtra(DriverMapActivity.END_LOCATION, endLocation)
        intent.putExtra(DriverMapActivity.PRICING, cost.toInt())
        startActivity(intent)
        finish()
    }

}
