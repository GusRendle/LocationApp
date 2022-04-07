package com.rendle.locationapp.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import com.rendle.locationapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.storage.FirebaseStorage
import com.rendle.locationapp.activities.PoiDetailActivity
import com.rendle.locationapp.models.PoIModel


class LocationAdapter (poiList: List<PoIModel>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    private var poiList = mutableListOf<PoIModel>() + poiList

    //Provides access for the views in each list object
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initializing variables in each view
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvDesc: TextView = itemView.findViewById(R.id.tvDescription)
        val ivPoi: ImageView = itemView.findViewById(R.id.iv_location_image)
    }

    // Constructor inflates the layout from XML, returns the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {
        // Creates inflater
        val inflater = LayoutInflater.from(parent.context)
        // Creates view by inflating the item's XML
        val contactView = inflater.inflate(R.layout.item_location, parent, false)
        // Return the ViewHolder containing the new view
        return ViewHolder(contactView)
    }

    // Populates data for each item
    override fun onBindViewHolder(viewHolder: LocationAdapter.ViewHolder, position: Int) {
        // Get the current poi based on position
        val poi: PoIModel = poiList[position]
        // Set item views based PoI data
        viewHolder.tvName.text = poi.name
        viewHolder.tvDesc.text = poi.description

        //Firebase Storage location
        val storageRef = FirebaseStorage.getInstance("gs://locationapp-3c40b.appspot.com").reference
        //The location of this poi's image
        val poiImageRef = storageRef.child("images/${poi.uuid}/main.jpg")
        //Gets image URL from firebase storage
        poiImageRef.downloadUrl.addOnSuccessListener {
            //Uses the coil library to load the image from the URL
            viewHolder.ivPoi.load(it)
        }.addOnFailureListener {
            Log.e("firebase", "POI Image not found", it)
        }

        //When item is clicked, go to PoiDetailActivity
        viewHolder.itemView.setOnClickListener{
            val intent = Intent(it.context, PoiDetailActivity::class.java)
            //Pass the uuid of the item clicked
            intent.putExtra("uuid", poi.uuid)
            it.context.startActivity(intent)
        }
    }

    // Returns the number of items in the list
    override fun getItemCount(): Int {
        return poiList.size
    }

    /**
     * Updates recycler view with a list
     * @param list to send to recycler view
     */
    @SuppressLint("NotifyDataSetChanged")
    fun addList(list: List<PoIModel>) {
        poiList = list
        notifyDataSetChanged()
    }
}