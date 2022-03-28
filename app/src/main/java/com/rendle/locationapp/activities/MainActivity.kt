package com.rendle.locationapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rendle.locationapp.AddLocationActivity
import com.rendle.locationapp.databinding.ActivityMainBinding

//Uses a Data Binding to refer to objects by their XML IDs
private lateinit var binding: ActivityMainBinding

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        //Calls parent constructor
        super.onCreate(savedInstanceState)
        //Creates objects from XML file
        binding = ActivityMainBinding.inflate(layoutInflater)
        //Sets XML view of this class
        setContentView(binding.root)

        //Sets the Tool Bar as a Support Action Bar
        setSupportActionBar(binding.toolbarMain)
        // Adds the back button
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        binding.fabAddLocation.setOnClickListener {
            val intent = Intent(this, AddLocationActivity::class.java)
            startActivity(intent)
        }
    }
}