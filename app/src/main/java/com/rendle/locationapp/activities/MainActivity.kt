package com.rendle.locationapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityMainBinding
import com.rendle.locationapp.models.PoIModel


//Uses a Data Binding to refer to objects by their XML IDs
private lateinit var binding: ActivityMainBinding
//Creates a list of PoIs
lateinit var poiList: ArrayList<PoIModel>

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(binding.root)

        //Generates 20 PoIs
        poiList = PoIModel.createLocationList(20)

        val rvLocations = binding.rvLocationsList
        //Sends list to rv Adapter
        rvLocations.adapter = LocationAdapter(poiList)
        // Sets the RecyclerView's layoutManager to the created layout
        rvLocations.layoutManager = LinearLayoutManager(this)

        //Sets the Tool Bar as a Support Action Bar
        setSupportActionBar(binding.toolbarMain)
        // Adds the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        //Open AddLocationActivity when fab is pressed
        binding.fabAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }
    }
}