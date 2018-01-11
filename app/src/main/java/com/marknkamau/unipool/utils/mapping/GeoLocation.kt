package com.marknkamau.unipool.utils.mapping

import android.os.Parcel
import android.os.Parcelable
import android.support.annotation.Keep
import com.google.android.gms.maps.model.LatLng

@Keep
data class GeoLocation(val name: String, val latitude: Double, val longitude: Double) : Parcelable {
    val latLng = LatLng(this.latitude, this.longitude)

    fun extractLatLng() = LatLng(this.latitude, this.longitude)

    override fun toString(): String {
        return "GeoLocation(fullname='$name', latitude=$latitude, longitude=$longitude)"
    }

    constructor(source: Parcel) : this(
            source.readString(),
            source.readDouble(),
            source.readDouble()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(name)
        writeDouble(latitude)
        writeDouble(longitude)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<GeoLocation> = object : Parcelable.Creator<GeoLocation> {
            override fun createFromParcel(source: Parcel): GeoLocation = GeoLocation(source)
            override fun newArray(size: Int): Array<GeoLocation?> = arrayOfNulls(size)
        }
    }
}