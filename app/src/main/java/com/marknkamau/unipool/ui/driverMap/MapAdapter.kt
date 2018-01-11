package com.marknkamau.unipool.ui.driverMap

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.marknkamau.unipool.utils.mapping.MarkerData
import nz.co.trademe.mapme.LatLng
import nz.co.trademe.mapme.annotations.AnnotationFactory
import nz.co.trademe.mapme.annotations.MapAnnotation
import nz.co.trademe.mapme.annotations.MarkerAnnotation
import nz.co.trademe.mapme.googlemaps.GoogleMapMeAdapter

class MapAdapter(context: Context, private val markers: List<MarkerData>) : GoogleMapMeAdapter(context) {
    override fun onCreateAnnotation(factory: AnnotationFactory<GoogleMap>, position: Int, annotationType: Int): MapAnnotation {
        val item = markers[position]

        return factory.createMarker(LatLng(item.latitude, item.longitude), null, item.title)
    }

    override fun onBindAnnotation(annotation: MapAnnotation, position: Int, payload: Any?) {
        if (annotation is MarkerAnnotation) {
            val item = this.markers[position]
            annotation.title = item.title
        }
    }

    override fun getItemCount() = markers.size
}