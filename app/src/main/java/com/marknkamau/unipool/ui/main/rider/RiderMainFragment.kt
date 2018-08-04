package com.marknkamau.unipool.ui.main.rider

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.ui.main.MainActivity
import com.marknkamau.unipool.ui.riderMap.RiderMapActivity
import com.marknkamau.unipool.ui.scheduleRide.ScheduleRideActivity
import com.marknkamau.unipool.utils.start
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.fragment_rider_main.*

class RiderMainFragment : Fragment(), RiderMainView {
    private lateinit var scheduledRidesAdapter: ScheduledRidesAdapter
    private lateinit var presenter: RiderMainPresenter
    private val mainActivity by lazy { activity as MainActivity }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_rider_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = RiderMainPresenter(this, UnipoolApp.localStorage, UnipoolApp.apiRepository)

        rvScheduledRides.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        rvScheduledRides.addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
        scheduledRidesAdapter = ScheduledRidesAdapter { ride ->
            showDeleteDialog(ride)
        }
        rvScheduledRides.adapter = scheduledRidesAdapter

        btnRequestRide.setOnClickListener {
            if (mainActivity.canViewMap()) {
                requireContext().start(RiderMapActivity::class.java)
            }
        }

        btnAddScheduledRide.setOnClickListener {
            if (mainActivity.canViewMap()) {
                requireContext().start(ScheduleRideActivity::class.java)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.getScheduledRides()
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    private fun showDeleteDialog(ride: ScheduledRide) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete ride?")
                .setMessage("Are you sure you want to delete this ride?")
                .setPositiveButton("Yes") { _, _ ->
                    presenter.deleteRide(ride)
                }
                .setNegativeButton("No") { _, _ -> }
                .show()
    }

    override fun displayMessage(message: String) {
        requireContext().toast(message)
    }

    override fun displayScheduledRides(rides: MutableList<ScheduledRide>) {
        scheduledRidesAdapter.setList(rides)
    }

}