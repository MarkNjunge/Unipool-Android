package com.marknkamau.unipool.ui.driverScheduledRides

import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_driver_scheduled_rides.*

class DriverScheduledRidesActivity : BaseActivity(), DriverScheduledRidesView {
    private lateinit var presenter: DriverScheduledRidesPresenter
    private lateinit var scheduledRidesAdapter: DriverScheduledRidesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_scheduled_rides)

        presenter = DriverScheduledRidesPresenter(this, apiRepository)

        rvScheduledRides.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvScheduledRides.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        scheduledRidesAdapter = DriverScheduledRidesAdapter({ ride ->

        })
        rvScheduledRides.adapter = scheduledRidesAdapter
    }

    override fun onResume() {
        super.onResume()
        presenter.getRides()
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    override fun displayScheduledRides(rides: MutableList<ScheduledRide>) {
        pbLoading.visibility = View.GONE
        tvNoScheduledRides.visibility = View.GONE
        scheduledRidesAdapter.setList(rides)
    }

    override fun noScheduledRides() {
        pbLoading.visibility = View.GONE
        tvNoScheduledRides.visibility = View.VISIBLE
        scheduledRidesAdapter.setList(mutableListOf())
    }

    override fun displayMessage(message: String) {
        pbLoading.visibility = View.GONE
        toast(message)
    }

}
