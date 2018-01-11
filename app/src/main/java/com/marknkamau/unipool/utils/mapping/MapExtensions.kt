package com.marknkamau.unipool.utils.mapping

import com.google.android.gms.maps.model.LatLng

fun LatLng.toOtherType(): com.google.maps.model.LatLng {
    return com.google.maps.model.LatLng(this.latitude, this.longitude)
}

fun com.google.maps.model.LatLng.toOtherType(): LatLng {
    return LatLng(this.lat, this.lng)
}

