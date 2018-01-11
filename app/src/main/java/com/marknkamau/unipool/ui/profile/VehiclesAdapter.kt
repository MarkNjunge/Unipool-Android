package com.marknkamau.unipool.ui.profile

import android.support.transition.TransitionManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.Vehicle
import kotlinx.android.synthetic.main.item_vehicle.view.*

class VehiclesAdapter(
        private val vehicles: MutableList<Vehicle>,
        private val onEdit: (Vehicle, VehicleClickType) -> Unit)

    : RecyclerView.Adapter<VehiclesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_vehicle))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(vehicles[position], onEdit)

    override fun getItemCount() = vehicles.size

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(vehicle: Vehicle, onEdit: (Vehicle, VehicleClickType) -> Unit) {
            itemView.tvMake.text = vehicle.make
            itemView.tvRegNo.text = vehicle.registrationNumber
            itemView.tvPassengers.text = vehicle.capacity.toString()
            itemView.tvColor.text = vehicle.color

            itemView.vVehicle.setOnClickListener {
                TransitionManager.beginDelayedTransition(itemView.vVehicle)
                itemView.imgEdit.visibility = View.VISIBLE
                itemView.imgDelete.visibility = View.VISIBLE
            }

            itemView.imgEdit.setOnClickListener {
                TransitionManager.beginDelayedTransition(itemView.vVehicle)
                itemView.imgEdit.visibility = View.GONE
                itemView.imgDelete.visibility = View.GONE

                onEdit(vehicle, VehicleClickType.EDIT)
            }

            itemView.imgDelete.setOnClickListener {
                TransitionManager.beginDelayedTransition(itemView.vVehicle)
                itemView.imgEdit.visibility = View.GONE
                itemView.imgDelete.visibility = View.GONE

                onEdit(vehicle, VehicleClickType.DELETE)
            }
        }
    }

    enum class VehicleClickType {
        EDIT,
        DELETE
    }

    private fun ViewGroup.inflate(layoutRes: Int): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, false)
    }
}

