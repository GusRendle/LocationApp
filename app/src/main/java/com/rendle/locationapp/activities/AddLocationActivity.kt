package com.rendle.locationapp.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.rendle.locationapp.R
import com.rendle.locationapp.adapters.LocationAdapter
import com.rendle.locationapp.databinding.ActivityAddLocationBinding
import com.rendle.locationapp.models.PoIModel
import java.util.*

//Uses a Data Binding (b) to refer to objects by their XML IDs
private lateinit var b: ActivityAddLocationBinding

private lateinit var firebaseDb: FirebaseDatabase
private lateinit var dbRef: DatabaseReference
private lateinit var storageRef: StorageReference
private lateinit var poiImageUri: Uri
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
            storageRef = FirebaseStorage.getInstance("gs://locationapp-3c40b.appspot.com").reference
            //The location of this poi's image
            val poiImageRef = storageRef.child("images/${editUuid}")
            //Gets image URL from firebase storage
            poiImageRef.downloadUrl.addOnSuccessListener {
                //Uses the coil library to load the image from the URL
                b.ivAddImage.load(it)
                poiImageUri = it
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

        //Handles the launching of the get image intent
        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            //If image was received correctly
            if (result.resultCode == Activity.RESULT_OK) {
                //Get the image's Uri
                poiImageUri = result.data!!.data!!
                //Show it in the imageView
                b.ivAddImage.setImageURI(poiImageUri)
            }
        }

        b.tvAddImage.setOnClickListener {
            //Create a new intent to get the image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            //Launch this intent
            resultLauncher.launch(intent)
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
                } else {
                    val uuid = UUID.randomUUID().toString()
                    dbRef.child("POIs").child(uuid).setValue(poi)
                }

                //Set the reference to the image
                val imagePOIref: StorageReference = storageRef.child("images/${editUuid}")
                Toast.makeText(this, "Image uploading", Toast.LENGTH_LONG).show()
                //Upload image
                imagePOIref.putFile(poiImageUri).addOnFailureListener {
                    Toast.makeText(this, "Failed to upload", Toast.LENGTH_LONG).show()
                }.addOnSuccessListener {
                    onBackPressed()
                }
            }
        }

    }
}