package com.rendle.locationapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rendle.locationapp.databinding.ActivityAddLocationBinding

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityAddLocationBinding

class AddLocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        b = ActivityAddLocationBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(b.root)

        //Sets the Tool Bar as a Support Action Bar
        setSupportActionBar(b.toolbarAddLocation)
        // Adds the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Setting the click event to the back button
        b.toolbarAddLocation.setNavigationOnClickListener {
            onBackPressed()
        }

    }
}