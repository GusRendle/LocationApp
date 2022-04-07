package com.rendle.locationapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.rendle.locationapp.R
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityAddLocationBinding
import com.rendle.locationapp.models.PoIModel
import java.util.*

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityAddLocationBinding

private lateinit var firebaseDb: FirebaseDatabase
private lateinit var dbRef: DatabaseReference
//Current location on map
var currentLocation: LatLng? = null
//If this Activity is in edit mode, this is the uuid of the poi
var editUuid: String? = null

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

        //If the the intent has an extra, switch to edit mode
        if (intent.getStringExtra("uuid") != null) {
            editUuid = intent.getStringExtra("uuid")!!

            dbRef.child("POIs").child(editUuid!!).get().addOnSuccessListener { dbPoi ->
                b.etName.setText(dbPoi.child("name").value as String)
                b.etDescription.setText(dbPoi.child("description").value as String)

                val lat = dbPoi.child("location/latitude/").value as Double
                val lng = dbPoi.child("location/longitude/").value as Double
                currentLocation = LatLng(lat,lng)
            }.addOnFailureListener{
                Log.e("firebase", "POI not found", it)
            }

            //Firebase Storage location
            val storageRef = FirebaseStorage.getInstance("gs://locationapp-3c40b.appspot.com").reference
            //The location of this poi's image
            val poiImageRef = storageRef.child("images/${editUuid}/main.jpg")
            //Gets image URL from firebase storage
            poiImageRef.downloadUrl.addOnSuccessListener {
                //Uses the coil library to load the image from the URL
                b.ivAddImage.load(it)
            }.addOnFailureListener {
                Log.e("firebase", "POI Image not found", it)
            }

            b.toolbarAddLocation.title = "Edit POI"

        }

        //Gets the id of the map fragment
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        //Acquires the GoogleMap object
        mapFragment.getMapAsync { mMap ->
            if (currentLocation == null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(52.18288929882368, -2.225693857359504), 14F))
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15F))
            }
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
                //Firebase has no need for uuid or fav value, so they are set to null
                val poi = PoIModel(null, name, currentLocation, desc, null)

                if (editUuid != null) {
                    dbRef.child("POIs").child(editUuid!!).setValue(poi)
                    onBackPressed()
                } else {
                    val uuid = UUID.randomUUID().toString()
                    dbRef.child("POIs").child(uuid).setValue(poi)
                    onBackPressed()
                }
            }
        }

    }
}