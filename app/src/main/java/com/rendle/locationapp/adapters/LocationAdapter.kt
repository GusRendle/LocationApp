package com.rendle.locationapp.adapters

import android.annotation.SuppressLint
import com.rendle.locationapp.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.rendle.locationapp.models.PoIModel


class LocationAdapter (poiList: List<PoIModel>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    private var poiList = mutableListOf<PoIModel>() + poiList

    //Provides access for the views in each list object
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Initializing variables in each view
        val nameTextView: TextView = itemView.findViewById<TextView>(R.id.tvName)
        val descriptionTextView: TextView = itemView.findViewById<TextView>(R.id.tvDescription)
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
        val tvName = viewHolder.nameTextView
        tvName.text = poi.name
        val tvDesc = viewHolder.descriptionTextView
        tvDesc.text = poi.description
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