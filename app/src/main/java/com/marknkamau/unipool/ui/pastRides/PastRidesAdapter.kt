package com.marknkamau.unipool.ui.pastRides

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.PastRide
import com.marknkamau.unipool.utils.DateTime
import com.marknkamau.unipool.utils.inflate
import kotlinx.android.synthetic.main.item_past_ride.view.*

class PastRidesAdapter(val onClick: (PastRide) -> Unit) : RecyclerView.Adapter<PastRidesAdapter.ViewHolder>() {
    private val rides = mutableListOf<PastRide>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_past_ride))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(rides[position], onClick)

    override fun getItemCount() = rides.size

    fun setItems(rides: MutableList<PastRide>) {
        this.rides.clear()
        this.rides.addAll(rides)
        this.notifyDataSetChanged()
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        private var detailsVisible = false

        fun bind(pastRide: PastRide, onClick: (PastRide) -> Unit) {
            itemView.tvRideDate.text = pastRide.depatureTime.format(DateTime.DATE_FORMAT)
            itemView.tvStartLocation.text = pastRide.startLocation
            itemView.tvEndLocation.text = pastRide.endLocation
            itemView.tvArrivalTime.text = pastRide.arrivalTime?.format(DateTime.TIME_FORMAT)
            itemView.tvStartTime.text = pastRide.depatureTime.format(DateTime.TIME_FORMAT)
            itemView.tvDriverName.text = pastRide.driver
            itemView.tvVehicle.text = pastRide.vehicle

            itemView.rootLayout.setOnClickListener {
                onClick(pastRide)
                if (detailsVisible) {
                    itemView.viewExtraDetails.visibility = View.GONE
                } else {
                    itemView.viewExtraDetails.visibility = View.VISIBLE
                }
                detailsVisible = !detailsVisible
            }
        }
    }
}