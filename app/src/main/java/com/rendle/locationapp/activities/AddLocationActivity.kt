package com.rendle.locationapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.rendle.locationapp.R
import com.rendle.locationapp.databinding.ActivityAddLocationBinding
import com.rendle.locationapp.models.PoIModel
import java.util.*

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityAddLocationBinding

private lateinit var firebaseDb: FirebaseDatabase
private lateinit var dbRef: DatabaseReference

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

        firebaseDb = FirebaseDatabase.getInstance("https://locationapp-3c40b-default-rtdb.europe-west1.firebasedatabase.app/")
        dbRef = firebaseDb.reference

        //Gets the id of the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //Current location on map
        var currentLocation: LatLng? = null
        //Acquires the GoogleMap object
        mapFragment.getMapAsync { mMap ->
            mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.0, 151.0)))
            mMap.setOnCameraMoveListener {
                currentLocation = mMap.cameraPosition.target
            }
        }

        b.btnSave.setOnClickListener {
            //Get email and password strings
            val name: String = b.etName.text.toString()
            val desc: String = b.etDescription.text.toString()

            if(TextUtils.isEmpty(name) || TextUtils.isEmpty(desc)) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_LONG).show()
            } else if (currentLocation == null) {
                Toast.makeText(this, "Drag the marker to your location", Toast.LENGTH_LONG).show()
            } else {
                val uuid = UUID.randomUUID().toString()
                //Firebase has no need for uuid or fav value, so they are set to null
                val poi = PoIModel(null, name, currentLocation, desc, null)
                dbRef.child("POIs").child(uuid).setValue(poi)
                onBackPressed()
            }
        }

    }
}