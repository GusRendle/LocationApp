package com.rendle.locationapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rendle.locationapp.databinding.ActivityAddLocationBinding

private lateinit var binding: ActivityAddLocationBinding

class AddLocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        binding = ActivityAddLocationBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(binding.root)

        //Sets the Tool Bar as a Support Action Bar
        setSupportActionBar(binding.toolbarAddLocation)
        // Adds the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Setting the click event to the back button
        binding.toolbarAddLocation.setNavigationOnClickListener {
            onBackPressed()
        }

    }
}