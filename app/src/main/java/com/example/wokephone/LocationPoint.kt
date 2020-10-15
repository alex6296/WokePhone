package com.example.wokephone

import com.google.android.gms.maps.model.LatLng

class LocationPoint(
    val TimestampInMS:Int,
    val gt_lat :Double,
    val gt_long : Double,
    val phone_lat : Double,
    val phone_long : Double)
{

    fun toLatLng(): LatLng {
        return LatLng(gt_lat,gt_long)
    }
}