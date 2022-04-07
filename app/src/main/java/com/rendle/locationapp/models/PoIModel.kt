package com.rendle.locationapp.models

import com.google.android.gms.maps.model.LatLng

class PoIModel(val uuid: String? = null, val name: String? = null, val location: LatLng? = null, val description: String? = null, var fav: Boolean? = false) {
    companion object {
        private var lastLocationId = 0

        /**
         * Creates a list of example locations
         * @param numLocations Number of PoIs to generate
         * @return list of PoIs
         */
        fun createLocationList(numLocations: Int): ArrayList<PoIModel> {
            val locations = ArrayList<PoIModel>()
            for (i in 1..numLocations) {
                locations.add(PoIModel(null,"Name " + ++lastLocationId, null ,"Desc " + ++lastLocationId))
            }
            return locations
        }

//        fun createLocationList2(): ArrayList<PoIModel> {
//            val locations = ArrayList<PoIModel>()
//            locations.add(PoIModel("Bay", "It's wet"))
//            locations.add(PoIModel("Castle", "It's old"))
//            locations.add(PoIModel("Mitch", "It's drunk"))
//            locations.add(PoIModel("Homeless Guy", "It's dead"))
//            return locations
//        }
    }
}