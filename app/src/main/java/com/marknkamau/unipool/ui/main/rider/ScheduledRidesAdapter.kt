package com.marknkamau.unipool.ui.main.rider

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.ScheduledRide
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.inflate
import kotlinx.android.synthetic.main.item_scheduled_ride.view.*

class ScheduledRidesAdapter(private val onClick: (ride: ScheduledRide) -> Unit) : RecyclerView.Adapter<ScheduledRidesAdapter.ViewHolder>() {
    private val rides = mutableListOf<ScheduledRide>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_scheduled_ride))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(rides[position], onClick)

    override fun getItemCount() = rides.size

    fun setList(rides: MutableList<ScheduledRide>){
        this.rides.clear()
        this.rides.addAll(rides)
        this.notifyDataSetChanged()
    }

    fun addItem(ride: ScheduledRide) {
        rides.add(ride)
    }

    fun removeItem(ride: ScheduledRide) {
        rides.remove(ride)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(ride: ScheduledRide, onClick: (ride: ScheduledRide) -> Unit) {
            itemView.startLocationTextView.text = ride.startLocation.name
            itemView.endLocationTextView.text = ride.endLocation.name
            itemView.dateTimeTextView.text = ride.dateTime.format("${DateTime.TIME_FORMAT}\n${DateTime.DATE_FORMAT}")

            if (ride.driver != null){
                itemView.tvRiderName.text = ride.driver.fullname
                itemView.visibility = View.VISIBLE
            }

            itemView.scheduledRideView.setOnLongClickListener {
                onClick(ride)
                true
            }
        }
    }
}