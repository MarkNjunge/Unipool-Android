package com.marknkamau.unipool.ui.pastRides

import android.os.Bundle
import android.support.transition.TransitionManager
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.LinearLayout
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.PastRide
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_past_ride.*

class PastRidesActivity : BaseActivity(), PastRidesView {
    private lateinit var pastRidesAdapter: PastRidesAdapter
    private lateinit var presenter: PastRidesPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_past_ride)

        rvPastRides.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        rvPastRides.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
        pastRidesAdapter = PastRidesAdapter { pastRide ->
            TransitionManager.beginDelayedTransition(sceneRoot)
        }
        rvPastRides.adapter = pastRidesAdapter

        presenter = PastRidesPresenter(this, UnipoolApp.localStorage, UnipoolApp.apiRepository)
    }

    override fun onResume() {
        super.onResume()
        presenter.getRides()
    }

    override fun onPause() {
        super.onPause()
        presenter.dispose()
    }

    override fun displayMessage(message: String) {
        pbLoading.visibility = View.GONE
        toast(message)
    }

    override fun setRides(rides: MutableList<PastRide>) {
        pastRidesAdapter.setItems(rides)
        pbLoading.visibility = View.GONE
        rvPastRides.visibility = View.VISIBLE
        tvNoPastRides.visibility = View.GONE
    }

    override fun noRides() {
        pbLoading.visibility = View.GONE
        rvPastRides.visibility = View.GONE
        tvNoPastRides.visibility = View.VISIBLE
    }
}
