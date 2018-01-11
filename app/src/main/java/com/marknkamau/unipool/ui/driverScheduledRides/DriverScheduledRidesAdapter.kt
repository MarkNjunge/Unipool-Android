package com.marknkamau.unipool.ui.driverScheduledRides

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.inflate
import kotlinx.android.synthetic.main.item_driver_schedueld_ride.view.*

class DriverScheduledRidesAdapter(private val onClick: (ride: ScheduledRide) -> Unit) : RecyclerView.Adapter<DriverScheduledRidesAdapter.ViewHolder>() {
    private val rides = mutableListOf<ScheduledRide>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_driver_schedueld_ride))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(rides[position], onClick)

    override fun getItemCount() = rides.size

    fun setList(rides: MutableList<ScheduledRide>) {
        this.rides.clear()
        this.rides.addAll(rides)
        this.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(ride: ScheduledRide, onClick: (ride: ScheduledRide) -> Unit) {
            itemView.startLocationTextView.text = ride.startLocation.name
            itemView.endLocationTextView.text = ride.endLocation.name
            itemView.dateTimeTextView.text = ride.dateTime.format("${DateTime.TIME_FORMAT}\n${DateTime.DATE_FORMAT}")
            itemView.tvRiderName.text = ride.user.fullname
            itemView.tvRiderPhone.text = "0${ride.user.phone}"

            itemView.scheduledRideView.setOnLongClickListener {
                onClick(ride)
                true
            }
        }
    }
}