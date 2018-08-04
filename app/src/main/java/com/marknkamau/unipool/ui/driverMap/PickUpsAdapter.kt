package com.marknkamau.unipool.ui.driverMap

import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.PickUp
import com.marknkamau.unipool.utils.inflate
import kotlinx.android.synthetic.main.item_pick_up.view.*
import timber.log.Timber

class PickUpsAdapter(private val onClick: (pickUp: PickUp, clickType: ClickType) -> Unit) : RecyclerView.Adapter<PickUpsAdapter.ViewHolder>() {

    private val pickUps = mutableListOf<PickUp>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_pick_up))

    override fun getItemCount() = pickUps.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(pickUps[position], onClick)

    fun updateList(items: MutableList<PickUp>) {
        this.pickUps.clear()
        this.pickUps.addAll(items)
        this.notifyDataSetChanged()
        Timber.i("List updated: ${items.size}")
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(pickUp: PickUp, onClick: (pickUp: PickUp, clickType: ClickType) -> Unit) {
            itemView.tvFullName.text = pickUp.user.fullname
            itemView.tvPhone.text = "0${pickUp.user.phone}"
            itemView.tvLocation.text = pickUp.location.name

            if (pickUp.completed) {
                itemView.imgPickUp.visibility = View.GONE
                itemView.imgCancel.visibility = View.GONE
            }

            itemView.imgPickUp.setOnClickListener {
                onClick(pickUp, ClickType.PICKED)
            }

            itemView.imgCancel.setOnClickListener {
                onClick(pickUp, ClickType.CANCELLED)
            }
        }
    }

    enum class ClickType {
        CANCELLED,
        PICKED
    }
}