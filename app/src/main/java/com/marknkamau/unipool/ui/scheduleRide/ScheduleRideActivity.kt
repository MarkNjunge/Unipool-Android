package com.marknkamau.unipool.ui.scheduleRide

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.View
import com.marknkamau.unipool.R
import com.marknkamau.unipool.UnipoolApp
import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.ui.BaseActivity
import com.marknkamau.unipool.ui.selectLocation.SelectLocationActivity
import com.marknkamau.unipool.utils.DatePickerFragment
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.TimePickerFragment
import com.marknkamau.unipool.utils.mapping.GeoLocation
import com.marknkamau.unipool.utils.toast
import kotlinx.android.synthetic.main.activity_schedule_ride.*
import java.util.*

class ScheduleRideActivity : BaseActivity(), ScheduleRideView {
    private val selectedTime by lazy { DateTime.getNow() }
    private val START_LOCATION_RQ: Int = 1
    private val END_LOCATION_RQ: Int = 2
    private var startLocation: GeoLocation? = null
    private var endLocation: GeoLocation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule_ride)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val presenter = ScheduleRidePresenter(this, UnipoolApp.authService, UnipoolApp.localStorage, UnipoolApp.apiRepository)

        val timePickerFragment = TimePickerFragment()
        timePickerFragment.setListener { hourOfDay, minute ->
            selectedTime.hourOfDay = hourOfDay
            selectedTime.minute = minute
            tvTime.text = selectedTime.format(DateTime.TIME_FORMAT)
        }

        val datePickerFragment = DatePickerFragment()
        datePickerFragment.setListener { year, month, dayOfMonth ->
            selectedTime.year = year
            selectedTime.month = month
            selectedTime.dayOfMonth = dayOfMonth
            tvDate.text = selectedTime.format(DateTime.DATE_FORMAT)
        }

        tvTime.setOnClickListener {
            timePickerFragment.show(supportFragmentManager, "timepicker")
        }

        tvDate.setOnClickListener {
            datePickerFragment.show(supportFragmentManager, "datepicker")
        }

        startLocationTextView.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            startActivityForResult(intent, START_LOCATION_RQ)
        }

        endLocationTextView.setOnClickListener {
            val intent = Intent(this, SelectLocationActivity::class.java)
            startActivityForResult(intent, END_LOCATION_RQ)
        }

        btnSchedule.setOnClickListener {
            presenter.scheduleRide(startLocation!!, endLocation!!, selectedTime)
            pbLoading.visibility = View.VISIBLE
        }

        tvTime.text = selectedTime.format(DateTime.TIME_FORMAT)
        tvDate.text = selectedTime.format(DateTime.DATE_FORMAT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                START_LOCATION_RQ -> {
                    startLocation = data?.getParcelableExtra(SelectLocationActivity.SELECTED_LOCATION)
                    startLocationTextView.text = startLocation?.name
                    refreshScheduleButton()
                }
                END_LOCATION_RQ -> {
                    endLocation = data?.getParcelableExtra(SelectLocationActivity.SELECTED_LOCATION)
                    endLocationTextView.text = endLocation?.name
                    refreshScheduleButton()
                }
            }
        }
    }

    private fun refreshScheduleButton() {
        if (startLocation != null && endLocation != null) {
            btnSchedule.isEnabled = true
            btnSchedule.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
        }
    }

    override fun displayMessage(message: String) {
        toast(message)
        pbLoading.visibility = View.GONE
    }

    override fun onRideScheduled() {
        pbLoading.visibility = View.GONE
        displayMessage("Ride scheduled")
        finish()
    }

}
