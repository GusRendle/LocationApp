package com.rendle.locationapp.models

import com.google.android.gms.maps.model.LatLng

class PoIModel(
    val uuid: String? = null,
    val name: String? = null,
    val location: LatLng? = null,
    val description: String? = null,
    var fav: Boolean? = false)