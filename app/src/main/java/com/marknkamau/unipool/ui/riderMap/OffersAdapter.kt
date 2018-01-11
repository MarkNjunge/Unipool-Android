package com.marknkamau.unipool.ui.riderMap

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.marknkamau.unipool.R
import com.marknkamau.unipool.domain.OfferResponseType
import com.marknkamau.unipool.domain.RequestOffer
import kotlinx.android.synthetic.main.item_offer.view.*
import timber.log.Timber

class OffersAdapter(
        private val distance: Long,
        private val offers: MutableList<RequestOffer>,
        private val onResponse: (OfferResponseType, RequestOffer) -> Unit)
    : RecyclerView.Adapter<OffersAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(parent.inflate(R.layout.item_offer))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(offers[position], distance, onResponse)

    override fun getItemCount() = offers.size

    fun addItem(offer: RequestOffer) {
        offers.customAdd(offer)
        this.notifyDataSetChanged()
    }

    fun removeItem(offer: RequestOffer) {
        offers.remove(offer)
    }

    private fun MutableList<RequestOffer>.customAdd(offer: RequestOffer) {
        val find = this.find { it.driver.userId == offer.driver.userId }
        if (find != null) {
            this.remove(find)
        }
        this.add(offer)
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bind(offer: RequestOffer, distance: Long, onResponse: (OfferResponseType, RequestOffer) -> Unit) {
            Timber.i("Binding: $offer")
            itemView.tvRiderName.text = offer.driver.fullname
            itemView.tvCostPer.text = "${offer.pricing} per km"
            itemView.tvTotalCost.text = "${offer.pricing * (distance / 1000).toDouble()}Ksh"

            itemView.imgAccept.setOnClickListener {
                onResponse(OfferResponseType.ACCEPTED, offer)
            }

            itemView.imgReject.setOnClickListener {
                onResponse(OfferResponseType.REJECTED, offer)
            }
        }
    }

    private fun ViewGroup.inflate(layoutRes: Int): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, false)
    }
}